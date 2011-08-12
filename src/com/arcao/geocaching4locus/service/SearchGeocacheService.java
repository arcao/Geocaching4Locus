package com.arcao.geocaching4locus.service;

import geocaching.api.AbstractGeocachingApiV2;
import geocaching.api.data.Geocache;
import geocaching.api.data.SimpleGeocache;
import geocaching.api.data.type.CacheType;
import geocaching.api.exception.GeocachingApiException;
import geocaching.api.exception.InvalidCredentialsException;
import geocaching.api.exception.InvalidSessionException;
import geocaching.api.impl.LiveGeocachingApi;
import geocaching.api.impl.live_geocaching_api.filter.CacheFilter;
import geocaching.api.impl.live_geocaching_api.filter.GeocacheExclusionsFilter;
import geocaching.api.impl.live_geocaching_api.filter.GeocacheTypeFilter;
import geocaching.api.impl.live_geocaching_api.filter.NotFoundByUsersFilter;
import geocaching.api.impl.live_geocaching_api.filter.PointRadiusFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import menion.android.locus.addon.publiclib.DisplayData;
import menion.android.locus.addon.publiclib.geoData.PointsData;
import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.provider.DataStorageProvider;
import com.arcao.geocaching4locus.util.Account;

public class SearchGeocacheService extends IntentService {
	private static final String TAG = "SearchGeocacheService";
	
	public static final String ACTION_PROGRESS_UPDATE = "com.arcao.geocaching4locus.intent.action.PROGRESS_UPDATE";
	public static final String ACTION_PROGRESS_COMPLETE = "com.arcao.geocaching4locus.intent.action.PROGRESS_COMPLETE";
	public static final String ACTION_ERROR = "com.arcao.geocaching4locus.intent.action.ERROR";

	public static final String PARAM_COUNT = "COUNT";
	public static final String PARAM_CURRENT = "CURRENT";
	public static final String PARAM_RESOURCE_ID = "RESOURCE_ID";
	public static final String PARAM_ADDITIONAL_MESSAGE = "ADDITIONAL_MESSAGE";
	
	public static final String PARAM_LATITUDE = "LATITUDE";
	public static final String PARAM_LONGITUDE = "LONGITUDE";
	
	private final static int MAX_PER_PAGE = 10;
	
	private boolean skipFound;
	private boolean simpleCacheData;
	private double distance;
	private int count = 0;
	private int current = 0;
	private Account account;
	
	protected SharedPreferences prefs;
	
	private static SearchGeocacheService instance = null;
	
	public SearchGeocacheService() {
		super(TAG);		
	}
	
	public static SearchGeocacheService getInstance() {
		return instance;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		instance = this;
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		loadConfiguration();
		sendProgressUpdate();
			
		double latitude = intent.getDoubleExtra(PARAM_LATITUDE, 0D);
		double longitude = intent.getDoubleExtra(PARAM_LONGITUDE, 0D);
		
		try {
			List<SimpleGeocache> caches = downloadCaches(latitude, longitude);
			if (caches != null)
				callLocus(caches);
		} catch (InvalidCredentialsException e) {
			sendError(R.string.error_credentials, null);
		} catch (Exception e) {
			String message = e.getMessage();
			if (message == null)
				message = "";
			
			sendError(R.string.error, String.format("<br>%s<br> <br>Exception: %s<br>File: %s<br>Line: %d", message, e.getClass().getSimpleName(), e.getStackTrace()[0].getFileName(), e.getStackTrace()[0].getLineNumber()));
		}

	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		sendProgressComplete();
		instance = null;
	}

	protected void loadConfiguration() {
		skipFound = prefs.getBoolean("filter_skip_found", false);
		simpleCacheData = prefs.getBoolean("simple_cache_data", false);
		
		distance = prefs.getFloat("distance", 160.9344F);
		if (!prefs.getBoolean("imperial_units", false)) {
			distance = distance * 1.609344;
		}

		count = prefs.getInt("filter_count_of_caches", 50);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String userName = prefs.getString("username", "");
		String password = prefs.getString("password", "");
		String session = prefs.getString("session", null);

		account = new Account(userName, password, session);
	}

	private void callLocus(List<SimpleGeocache> caches) {
		boolean importCaches = prefs.getBoolean("import_caches", false);
		
		try {
			ArrayList<PointsData> pointDataCollection = new ArrayList<PointsData>();
			
			// beware there is row limit in DataStorageProvider (1MB per row - serialized PointsData is one row)
			// so we split points into several PointsData object with same uniqueName - Locus merge it automatically
			// since version 1.9.5
			PointsData points = new PointsData("Geocaching");
			for (SimpleGeocache cache : caches) {
				if (cache instanceof Geocache) {
					Geocache geocache = (Geocache) cache;
					Log.i(TAG, geocache.getGeoCode() + ": " + geocache.getLongDescription());
				}
				
				if (points.getPoints().size() >= 50) {
					pointDataCollection.add(points);
					points = new PointsData("Geocaching");
				}
				// convert SimpleGeocache to Point
				points.addPoint(cache.toPoint());
			}
			
			if (!points.getPoints().isEmpty())
				pointDataCollection.add(points);
			
			DisplayData.sendDataCursor(this, pointDataCollection, DataStorageProvider.URI, importCaches);
		} catch (Exception e) {
			Log.e(TAG, "callLocus()", e);
		}
	}
	
	protected CacheType[] getCacheTypeFilterResult() {
		Vector<CacheType> filter = new Vector<CacheType>();

		for (int i = 0; i < CacheType.values().length; i++) {
			if (prefs.getBoolean("filter_" + i, true)) {
				filter.add(CacheType.values()[i]);
			}
		}

		return filter.toArray(new CacheType[0]);
	}
	
	protected List<SimpleGeocache> downloadCaches(double latitude, double longitude) throws GeocachingApiException {
		final List<SimpleGeocache> caches = new ArrayList<SimpleGeocache>();
					
		if (account.getUserName() == null || account.getUserName().length() == 0 || account.getPassword() == null || account.getPassword().length() == 0)
			throw new InvalidCredentialsException("Username or password is empty.");
				
		AbstractGeocachingApiV2 api = new LiveGeocachingApi();
		login(api, account);
				
		try {
			current = 0;
			while (current < count) {
				int perPage = (count - current < MAX_PER_PAGE) ? count - current : MAX_PER_PAGE;
				
				List<SimpleGeocache> cachesToAdd = api.searchForGeocachesJSON(simpleCacheData, current, perPage, -1, -1, new CacheFilter[] {
						new PointRadiusFilter(latitude, longitude, (long) (distance * 1000)),
						new GeocacheTypeFilter(getCacheTypeFilterResult()),
						new GeocacheExclusionsFilter(false, true, null),
						new NotFoundByUsersFilter(skipFound ? account.getUserName() : null)
				});
				
				if (cachesToAdd.size() == 0)
					break;
								
				caches.addAll(cachesToAdd);
				
				current = current + perPage;
				
				sendProgressUpdate();
			}
			int count = caches.size();
			
			Log.i(TAG, "found caches: " + count);

			return caches;
		} catch (InvalidSessionException e) {
			account.setSession(null);
			
			Editor edit = prefs.edit();
			edit.remove("session");
			edit.commit();
			
			return downloadCaches(latitude, longitude);
		} finally {
			account.setSession(api.getSession());
			if (account.getSession() != null && account.getSession().length() > 0) {
				Editor edit = prefs.edit();
				edit.putString("session", account.getSession());
				edit.commit();
			}
		}
	}

	private void login(AbstractGeocachingApiV2 api, Account account) throws GeocachingApiException, InvalidCredentialsException {
		try {
			if (account.getSession() == null || account.getSession().length() == 0) {
				api.openSession(account.getUserName(), account.getPassword());
			} else {
				api.openSession(account.getSession());
			}
		} catch (InvalidCredentialsException e) {
			Log.e(TAG, "Creditials not valid.", e);
			throw e;
		}
	}
	
	public void sendProgressUpdate() {
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(ACTION_PROGRESS_UPDATE);
		//broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
		broadcastIntent.putExtra(PARAM_COUNT, count);
		broadcastIntent.putExtra(PARAM_CURRENT, current);
		sendBroadcast(broadcastIntent);
		Log.i(TAG, "Progress update sent.");
	}
	
	protected void sendProgressComplete() {
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(ACTION_PROGRESS_COMPLETE);
		//broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
		broadcastIntent.putExtra(PARAM_COUNT, count);
		broadcastIntent.putExtra(PARAM_CURRENT, count);
		sendBroadcast(broadcastIntent);
		Log.i(TAG, "Progress complete sent.");
	}
	
	protected void sendError(int error, String additionalMessage) {
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(ACTION_ERROR);
		//broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
		broadcastIntent.putExtra(PARAM_RESOURCE_ID, error);
		if (additionalMessage != null)
			broadcastIntent.putExtra(PARAM_ADDITIONAL_MESSAGE, additionalMessage);
		sendBroadcast(broadcastIntent);
		
		Log.i(TAG, "Error message sent.");
	}


}
