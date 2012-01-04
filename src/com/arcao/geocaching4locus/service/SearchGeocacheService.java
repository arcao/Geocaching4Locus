package com.arcao.geocaching4locus.service;

import geocaching.api.AbstractGeocachingApiV2;
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
import geocaching.api.impl.live_geocaching_api.filter.NotHiddenByUsersFilter;
import geocaching.api.impl.live_geocaching_api.filter.PointRadiusFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import menion.android.locus.addon.publiclib.DisplayDataExtended;
import menion.android.locus.addon.publiclib.geoData.Point;
import menion.android.locus.addon.publiclib.geoData.PointsData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.arcao.geocaching4locus.MainActivity;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.UpdateActivity;
import com.arcao.geocaching4locus.provider.DataStorageProvider;
import com.arcao.geocaching4locus.util.Account;

public class SearchGeocacheService extends AbstractService {
	private static final String TAG = "SearchGeocacheService";

	public static final String PARAM_LATITUDE = "LATITUDE";
	public static final String PARAM_LONGITUDE = "LONGITUDE";

	private final static int MAX_PER_PAGE = 10;

	private static SearchGeocacheService instance = null;

	private int current;
	private int count;

	private Account account;
	private boolean showFound;
	private boolean showOwn;
	private boolean showDisabled;
	private boolean simpleCacheData;
	private double distance;
	private boolean importCaches;
	private int logCount;
	private int trackableCount;
	private CacheType[] cacheTypes;

	public SearchGeocacheService() {
		super(TAG, R.string.downloading, R.string.downloading);
	}

	public static SearchGeocacheService getInstance() {
		return instance;
	}

	@Override
	protected void setInstance() {
		instance = this;
	}

	@Override
	protected void removeInstance() {
		instance = null;
	}

	@Override
	protected Intent createOngoingEventIntent() {
		return new Intent(this, MainActivity.class);
	}

	public void sendProgressUpdate() {
		sendProgressUpdate(current, count);
	}

	@Override
	protected void run(Intent intent) throws Exception {
		double latitude = intent.getDoubleExtra(PARAM_LATITUDE, 0D);
		double longitude = intent.getDoubleExtra(PARAM_LONGITUDE, 0D);

		sendProgressUpdate();
		List<SimpleGeocache> caches = downloadCaches(latitude, longitude);
		sendProgressComplete(count);
		if (caches != null)
			callLocus(caches);
	}

	@Override
	protected void loadConfiguration(SharedPreferences prefs) {
		showFound = prefs.getBoolean("filter_show_found", false);
		showOwn = prefs.getBoolean("filter_show_own", false);
		showDisabled = prefs.getBoolean("filter_show_disabled", false);
		simpleCacheData = prefs.getBoolean("simple_cache_data", false);

		distance = prefs.getFloat("distance", 160.9344F);
		if (prefs.getBoolean("imperial_units", false)) {
			distance = distance / 1.609344;
		}

		current = 0;
		count = prefs.getInt("filter_count_of_caches", 20);

		logCount = prefs.getInt("downloading_count_of_logs", 5);
		trackableCount = prefs.getInt("downloading_count_of_trackabless", 10);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String userName = prefs.getString("username", "");
		String password = prefs.getString("password", "");
		String session = prefs.getString("session", null);

		importCaches = prefs.getBoolean("import_caches", false);
		cacheTypes = getCacheTypeFilterResult(prefs);

		account = new Account(userName, password, session);
	}

	private void callLocus(List<SimpleGeocache> caches) {
		try {
			ArrayList<PointsData> pointDataCollection = new ArrayList<PointsData>();

			// beware there is row limit in DataStorageProvider (1MB per row -
			// serialized PointsData is one row)
			// so we split points into several PointsData object with same uniqueName
			// - Locus merge it automatically
			// since version 1.9.5
			PointsData points = new PointsData("Geocaching");
			for (SimpleGeocache cache : caches) {
				if (points.getPoints().size() >= 50) {
					pointDataCollection.add(points);
					points = new PointsData("Geocaching");
				}
				// convert SimpleGeocache to Point
				Point p = cache.toPoint();
				p.setExtraCallback(getResources().getString(R.string.locus_update_cache), "com.arcao.geocaching4locus", UpdateActivity.class.getName(), "cacheId",
						cache.getGeoCode());

				if (simpleCacheData) {
					p.setExtraOnDisplay("com.arcao.geocaching4locus", UpdateActivity.class.getName(), "simpleCacheId", cache.getGeoCode());
				}

				points.addPoint(p);
			}

			if (!points.getPoints().isEmpty())
				pointDataCollection.add(points);

			// set data
			Intent intent = DisplayDataExtended.prepareDataCursor(pointDataCollection, DataStorageProvider.URI);

			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			DisplayDataExtended.sendData(getApplication(), intent, importCaches);
		} catch (Exception e) {
			Log.e(TAG, "callLocus()", e);
		}
	}

	protected CacheType[] getCacheTypeFilterResult(SharedPreferences prefs) {
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

		if (isCanceled())
			return null;

		AbstractGeocachingApiV2 api = new LiveGeocachingApi();
		login(api, account);

		sendProgressUpdate();
		try {
			current = 0;
			while (current < count) {
				int perPage = (count - current < MAX_PER_PAGE) ? count - current : MAX_PER_PAGE;

				List<SimpleGeocache> cachesToAdd = api.searchForGeocaches(simpleCacheData, current, perPage, logCount, trackableCount, new CacheFilter[] {
						new PointRadiusFilter(latitude, longitude, (long) (distance * 1000)),
						new GeocacheTypeFilter(cacheTypes),
						new GeocacheExclusionsFilter(false, showDisabled ? null : true, null),
						new NotFoundByUsersFilter(showFound ? null : account.getUserName()),
						new NotHiddenByUsersFilter(showOwn ? null : account.getUserName())
				});

				if (isCanceled())
					return null;

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
			removeSession();

			return downloadCaches(latitude, longitude);
		} finally {
			account.setSession(api.getSession());
			if (account.getSession() != null && account.getSession().length() > 0) {
				storeSession(account.getSession());
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
}
