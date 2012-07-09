package com.arcao.geocaching4locus.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import menion.android.locus.addon.publiclib.DisplayData;
import menion.android.locus.addon.publiclib.LocusDataMapper;
import menion.android.locus.addon.publiclib.geoData.Point;
import menion.android.locus.addon.publiclib.geoData.PointsData;
import menion.android.locus.addon.publiclib.utils.RequiredVersionMissingException;
import android.accounts.OperationCanceledException;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.arcao.geocaching.api.GeocachingApi;
import com.arcao.geocaching.api.data.SimpleGeocache;
import com.arcao.geocaching.api.data.type.CacheType;
import com.arcao.geocaching.api.data.type.ContainerType;
import com.arcao.geocaching.api.exception.GeocachingApiException;
import com.arcao.geocaching.api.exception.InvalidCredentialsException;
import com.arcao.geocaching.api.exception.InvalidSessionException;
import com.arcao.geocaching.api.impl.LiveGeocachingApi;
import com.arcao.geocaching.api.impl.live_geocaching_api.filter.DifficultyFilter;
import com.arcao.geocaching.api.impl.live_geocaching_api.filter.Filter;
import com.arcao.geocaching.api.impl.live_geocaching_api.filter.GeocacheContainerSizeFilter;
import com.arcao.geocaching.api.impl.live_geocaching_api.filter.GeocacheExclusionsFilter;
import com.arcao.geocaching.api.impl.live_geocaching_api.filter.GeocacheTypeFilter;
import com.arcao.geocaching.api.impl.live_geocaching_api.filter.NotFoundByUsersFilter;
import com.arcao.geocaching.api.impl.live_geocaching_api.filter.NotHiddenByUsersFilter;
import com.arcao.geocaching.api.impl.live_geocaching_api.filter.PointRadiusFilter;
import com.arcao.geocaching.api.impl.live_geocaching_api.filter.TerrainFilter;
import com.arcao.geocaching.api.impl.live_geocaching_api.filter.ViewportFilter;
import com.arcao.geocaching4locus.Geocaching4LocusApplication;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.UpdateActivity;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.constants.PrefConstants;

public class LiveMapService extends IntentService {
	private static final String TAG = "G4L|LiveMapService";
	
	public static final String PARAM_LATITUDE = "LATITUDE";
	public static final String PARAM_LONGITUDE = "LONGITUDE";
	public static final String PARAM_TOP_LEFT_LATITUDE = "TOP_LEFT_LATITUDE";
	public static final String PARAM_TOP_LEFT_LONGITUDE = "TOP_LEFT_LONGITUDE";
	public static final String PARAM_BOTTOM_RIGHT_LATITUDE = "BOTTOM_RIGHT_LATITUDE";
	public static final String PARAM_BOTTOM_RIGHT_LONGITUDE = "BOTTOM_RIGHT_LONGITUDE";
	
	private static final int CACHES_PER_REQUEST = 50;
	private static final int CACHES_COUNT = CACHES_PER_REQUEST;
	
	private final AtomicInteger countOfJobs = new AtomicInteger(0);
	
	private boolean showFound;
	private boolean showOwn;
	private boolean showDisabled;
	private float difficultyMin;
	private float difficultyMax;
	private float terrainMin;
	private float terrainMax;
	private CacheType[] cacheTypes;
	private ContainerType[] containerTypes;

	public LiveMapService() {
		super(TAG);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// increase count of jobs
		countOfJobs.incrementAndGet();
		Log.d(TAG, "New job, count=" + countOfJobs.get());
		
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "Handling job, count=" + countOfJobs.get());
		
		// we skip all jobs before last one
		if (countOfJobs.getAndDecrement() != 1)
			return;
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this); 
		loadConfiguration(prefs);
		
		double latitude = intent.getDoubleExtra(PARAM_LATITUDE, 0D);
		double longitude = intent.getDoubleExtra(PARAM_LONGITUDE, 0D);
		double topLeftLatitude = intent.getDoubleExtra(PARAM_TOP_LEFT_LATITUDE, 0D);
		double topLeftLongitude = intent.getDoubleExtra(PARAM_TOP_LEFT_LONGITUDE, 0D);
		double bottomRightLatitude = intent.getDoubleExtra(PARAM_BOTTOM_RIGHT_LATITUDE, 0D);
		double bottomRightLongitude = intent.getDoubleExtra(PARAM_BOTTOM_RIGHT_LONGITUDE, 0D);
		
		try {			
			List<SimpleGeocache> caches = downloadCaches(latitude, longitude, topLeftLatitude, topLeftLongitude, bottomRightLatitude, bottomRightLongitude);
			
			PointsData pd = new PointsData(TAG);
			for (SimpleGeocache cache : caches) {
				Point p = LocusDataMapper.toLocusPoint(this, cache);
				p.setExtraOnDisplay(getPackageName(), UpdateActivity.class.getName(), UpdateActivity.PARAM_SIMPLE_CACHE_ID, cache.getCacheCode());
				pd.addPoint(p);
			}

			DisplayData.sendDataSilent(this, pd);
		} catch (RequiredVersionMissingException e) {
			Log.e(TAG, e.getMessage(), e);
		} catch (InvalidCredentialsException e) {
			e.printStackTrace();
			Toast.makeText(this, getString(R.string.error_credentials), Toast.LENGTH_LONG).show();

			// disable live map
			prefs.edit().putBoolean(PrefConstants.LIVE_MAP, false).commit();
		} catch (GeocachingApiException e) {
			Log.e(TAG, e.getMessage(), e);
			Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
		} finally {
			Log.d(TAG, "Job finished.");
		}
	}
	
	protected void loadConfiguration(SharedPreferences prefs) {
		showFound = prefs.getBoolean(PrefConstants.FILTER_SHOW_FOUND, false);
		showOwn = prefs.getBoolean(PrefConstants.FILTER_SHOW_OWN, false);
		showDisabled = prefs.getBoolean(PrefConstants.FILTER_SHOW_DISABLED, false);

		difficultyMin = Float.parseFloat(prefs.getString(PrefConstants.FILTER_DIFFICULTY_MIN, "1"));
		difficultyMax = Float.parseFloat(prefs.getString(PrefConstants.FILTER_DIFFICULTY_MAX, "5"));
		
		terrainMin = Float.parseFloat(prefs.getString(PrefConstants.FILTER_TERRAIN_MIN, "1"));
		terrainMax = Float.parseFloat(prefs.getString(PrefConstants.FILTER_TERRAIN_MAX, "5"));
		
		cacheTypes = getCacheTypeFilterResult(prefs);
		containerTypes = getContainerTypeFilterResult(prefs);
	}
	
	protected CacheType[] getCacheTypeFilterResult(SharedPreferences prefs) {
		Vector<CacheType> filter = new Vector<CacheType>();

		for (int i = 0; i < CacheType.values().length; i++) {
			if (prefs.getBoolean(PrefConstants.FILTER_CACHE_TYPE_PREFIX + i, true)) {
				filter.add(CacheType.values()[i]);
			}
		}

		return filter.toArray(new CacheType[0]);
	}
	
	protected ContainerType[] getContainerTypeFilterResult(SharedPreferences prefs) {
		Vector<ContainerType> filter = new Vector<ContainerType>();

		for (int i = 0; i < ContainerType.values().length; i++) {
			if (prefs.getBoolean(PrefConstants.FILTER_CONTAINER_TYPE_PREFIX + i, true)) {
				filter.add(ContainerType.values()[i]);
			}
		}

		return filter.toArray(new ContainerType[0]);
	}
	
	protected List<SimpleGeocache> downloadCaches(double latitude, double longitude, double topLeftLatitude, double topLeftLongitude, double bottomRightLatitude, double bottomRightLongitude) throws GeocachingApiException {
		final List<SimpleGeocache> caches = new ArrayList<SimpleGeocache>();

		if (!Geocaching4LocusApplication.getAuthenticatorHelper().hasAccount())
			throw new InvalidCredentialsException("Account not found.");

		GeocachingApi api = new LiveGeocachingApi(AppConstants.CONSUMER_KEY, AppConstants.LICENCE_KEY);
		
		int attempt = 0;
		
		while (++attempt <= 2) {
			try {
				login(api);
				
				String username = Geocaching4LocusApplication.getAuthenticatorHelper().getAccount().name;
		
				int current = 0;
				int perPage = CACHES_PER_REQUEST;
				
				while (current < CACHES_COUNT) {
					perPage = (CACHES_COUNT - current < CACHES_PER_REQUEST) ? CACHES_COUNT - current : CACHES_PER_REQUEST;
	
					List<SimpleGeocache> cachesToAdd;
					
					if (current == 0) {
						cachesToAdd = api.searchForGeocaches(true, perPage, 0, 0, new Filter[] {
								new PointRadiusFilter(latitude, longitude, 60000),
								new GeocacheTypeFilter(cacheTypes),
								new GeocacheContainerSizeFilter(containerTypes),
								new GeocacheExclusionsFilter(false, showDisabled ? null : true, null),
								new NotFoundByUsersFilter(showFound ? null : username),
								new NotHiddenByUsersFilter(showOwn ? null : username),
								new DifficultyFilter(difficultyMin, difficultyMax),
								new TerrainFilter(terrainMin, terrainMax),
								new ViewportFilter(topLeftLatitude, topLeftLongitude, bottomRightLatitude, bottomRightLongitude)
						});
					} else {
						cachesToAdd = api.getMoreGeocaches(true, current, perPage, 0, 0);
					}
									
					caches.addAll(cachesToAdd);
					
					if (cachesToAdd.size() != perPage)
						break;
	
					current = current + perPage;
				}
				int count = caches.size();
	
				Log.i(TAG, "found caches: " + count);
	
				return caches;
			} catch (InvalidSessionException e) {
				Log.e(TAG, e.getMessage(), e);
				Geocaching4LocusApplication.getAuthenticatorHelper().invalidateAuthToken();
				
				if (attempt == 1)
					continue;
				
				throw e;
			} catch (OperationCanceledException e) {
				Log.e(TAG, e.getMessage(), e);
				
				throw new InvalidCredentialsException("Log in operation cancelled");
			}
		}
		
		return caches; 
	}
	
	private void login(GeocachingApi api) throws GeocachingApiException, OperationCanceledException {
		String token = Geocaching4LocusApplication.getAuthenticatorHelper().getAuthToken();
		if (token == null) {
			Geocaching4LocusApplication.getAuthenticatorHelper().removeAccount();
			throw new InvalidCredentialsException("Account not found.");
		}
			
		api.openSession(token);
  }

	public static Intent createIntent(Context context, double latitude, double longitude, double topLeftLatitude, double topLeftLongitude, double bottomRightLatitude, double bottomRightLongitude) {
		Intent i = new Intent(context, LiveMapService.class);
		
		i.putExtra(PARAM_LATITUDE, latitude);
		i.putExtra(PARAM_LONGITUDE, longitude);
		i.putExtra(PARAM_TOP_LEFT_LATITUDE, topLeftLatitude);
		i.putExtra(PARAM_TOP_LEFT_LONGITUDE, topLeftLongitude);
		i.putExtra(PARAM_BOTTOM_RIGHT_LATITUDE, bottomRightLatitude);
		i.putExtra(PARAM_BOTTOM_RIGHT_LONGITUDE, bottomRightLongitude);
		
		return i;
	}
}
