package com.arcao.geocaching4locus.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import locus.api.android.ActionDisplayPointsExtended;
import locus.api.android.objects.PackWaypoints;
import locus.api.mapper.LocusDataMapper;
import locus.api.objects.extra.Waypoint;
import locus.api.utils.StoreableDataOutputStream;
import locus.api.utils.Utils;

import org.acra.ErrorReporter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.arcao.geocaching.api.GeocachingApi;
import com.arcao.geocaching.api.data.SimpleGeocache;
import com.arcao.geocaching.api.data.type.CacheType;
import com.arcao.geocaching.api.data.type.ContainerType;
import com.arcao.geocaching.api.exception.GeocachingApiException;
import com.arcao.geocaching.api.exception.InvalidCredentialsException;
import com.arcao.geocaching.api.exception.InvalidSessionException;
import com.arcao.geocaching.api.impl.LiveGeocachingApiFactory;
import com.arcao.geocaching.api.impl.live_geocaching_api.filter.BookmarksExcludeFilter;
import com.arcao.geocaching.api.impl.live_geocaching_api.filter.DifficultyFilter;
import com.arcao.geocaching.api.impl.live_geocaching_api.filter.Filter;
import com.arcao.geocaching.api.impl.live_geocaching_api.filter.GeocacheContainerSizeFilter;
import com.arcao.geocaching.api.impl.live_geocaching_api.filter.GeocacheExclusionsFilter;
import com.arcao.geocaching.api.impl.live_geocaching_api.filter.GeocacheTypeFilter;
import com.arcao.geocaching.api.impl.live_geocaching_api.filter.NotFoundByUsersFilter;
import com.arcao.geocaching.api.impl.live_geocaching_api.filter.NotHiddenByUsersFilter;
import com.arcao.geocaching.api.impl.live_geocaching_api.filter.PointRadiusFilter;
import com.arcao.geocaching.api.impl.live_geocaching_api.filter.TerrainFilter;
import com.arcao.geocaching4locus.Geocaching4LocusApplication;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.SearchNearestActivity;
import com.arcao.geocaching4locus.UpdateActivity;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.constants.PrefConstants;

public class SearchGeocacheService extends AbstractService {
	private static final String TAG = "G4L|SearchGeocacheService";

	public static final String PARAM_LATITUDE = "LATITUDE";
	public static final String PARAM_LONGITUDE = "LONGITUDE";

	private static SearchGeocacheService instance = null;

	private int current;
	private int count;

	private boolean showFound;
	private boolean showOwn;
	private boolean showDisabled;
	private float difficultyMin;
	private float difficultyMax;
	private float terrainMin;
	private float terrainMax;
	private boolean simpleCacheData;
	private double distance;
	private int logCount;
	private CacheType[] cacheTypes;
	private ContainerType[] containerTypes;
	private Boolean excludeIgnoreList;

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
		return new Intent(this, SearchNearestActivity.class);
	}

	public void sendProgressUpdate() {
		sendProgressUpdate(current, count);
	}

	@Override
	protected void run(Intent intent) throws Exception {
		double latitude = intent.getDoubleExtra(PARAM_LATITUDE, 0D);
		double longitude = intent.getDoubleExtra(PARAM_LONGITUDE, 0D);

		sendProgressUpdate();
		File file = downloadCaches(latitude, longitude);
		if (file != null) {
			sendProgressComplete(count);
			callLocus(file);
		}
	}

	@Override
	protected void loadConfiguration(SharedPreferences prefs) {
		showFound = prefs.getBoolean(PrefConstants.FILTER_SHOW_FOUND, false);
		showOwn = prefs.getBoolean(PrefConstants.FILTER_SHOW_OWN, false);
		showDisabled = prefs.getBoolean(PrefConstants.FILTER_SHOW_DISABLED, false);
		simpleCacheData = prefs.getBoolean(PrefConstants.DOWNLOADING_SIMPLE_CACHE_DATA, false);

		String distanceString;
		if (prefs.getBoolean(PrefConstants.IMPERIAL_UNITS, false)) {
			distanceString = prefs.getString(PrefConstants.FILTER_DISTANCE, "100");
		} else {
			distanceString = prefs.getString(PrefConstants.FILTER_DISTANCE, "160.9344");
		}

		try {
			distance = Float.parseFloat(distanceString);
		} catch (NumberFormatException e) {
			Log.e(TAG, e.getMessage(), e);
			distance = 100;
		}

		if (prefs.getBoolean(PrefConstants.IMPERIAL_UNITS, false)) {
			// get kilometers from miles
			distance = distance * 1.609344F;
		}

		current = 0;
		count = prefs.getInt(PrefConstants.DOWNLOADING_COUNT_OF_CACHES, 20);

		logCount = prefs.getInt(PrefConstants.DOWNLOADING_COUNT_OF_LOGS, 5);

		// default values for basic member
		difficultyMin = 1;
		difficultyMax = 5;

		terrainMin = 1;
		terrainMax = 5;

		cacheTypes = null;
		containerTypes = null;
		excludeIgnoreList = null;

		// Premium member feature?
		if (Geocaching4LocusApplication.getAuthenticatorHelper().getRestrictions().isPremiumMember()) {
			difficultyMin = Float.parseFloat(prefs.getString(PrefConstants.FILTER_DIFFICULTY_MIN, "1"));
			difficultyMax = Float.parseFloat(prefs.getString(PrefConstants.FILTER_DIFFICULTY_MAX, "5"));

			terrainMin = Float.parseFloat(prefs.getString(PrefConstants.FILTER_TERRAIN_MIN, "1"));
			terrainMax = Float.parseFloat(prefs.getString(PrefConstants.FILTER_TERRAIN_MAX, "5"));

			cacheTypes = getCacheTypeFilterResult(prefs);
			containerTypes = getContainerTypeFilterResult(prefs);
			excludeIgnoreList = true;
		}
	}

	private void callLocus(File file) {
		try {
			if (file != null) {
				ActionDisplayPointsExtended.sendPacksFile(getApplication(), file, true, Intent.FLAG_ACTIVITY_NEW_TASK);
			}
		} catch (Exception e) {
			Log.e(TAG, "callLocus()", e);
		}
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

	@SuppressWarnings("resource")
	protected File downloadCaches(double latitude, double longitude) throws GeocachingApiException {
		if (!Geocaching4LocusApplication.getAuthenticatorHelper().hasAccount())
			throw new InvalidCredentialsException("Account not found.");

		if (isCanceled())
			return null;

		ErrorReporter.getInstance().putCustomData("source", "search;" + latitude + ";" + longitude);

		GeocachingApi api = LiveGeocachingApiFactory.create();

		StoreableDataOutputStream sdos = null;

		try {
			File dataFile = ActionDisplayPointsExtended.getCacheFileName(Geocaching4LocusApplication.getAppContext());

			login(api);

			String username = Geocaching4LocusApplication.getAuthenticatorHelper().getAccount().name;

			sdos = new StoreableDataOutputStream(new FileOutputStream(dataFile));
			sdos.beginList();

			sendProgressUpdate();

			current = 0;
			int perPage = AppConstants.CACHES_PER_REQUEST;

			while (current < count) {
				perPage = (count - current < AppConstants.CACHES_PER_REQUEST) ? count - current : AppConstants.CACHES_PER_REQUEST;

				List<SimpleGeocache> cachesToAdd;

				if (current == 0) {
					cachesToAdd = api.searchForGeocaches(simpleCacheData, perPage, logCount, 0, new Filter[] {
							new PointRadiusFilter(latitude, longitude, (long) (distance * 1000)),
							new GeocacheTypeFilter(cacheTypes),
							new GeocacheContainerSizeFilter(containerTypes),
							new GeocacheExclusionsFilter(false, showDisabled ? null : true, null),
							new NotFoundByUsersFilter(showFound ? null : username),
							new NotHiddenByUsersFilter(showOwn ? null : username),
							new DifficultyFilter(difficultyMin, difficultyMax),
							new TerrainFilter(terrainMin, terrainMax),
							new BookmarksExcludeFilter(excludeIgnoreList)
					});
				} else {
					cachesToAdd = api.getMoreGeocaches(simpleCacheData, current, perPage, logCount, 0);
				}

				if (!simpleCacheData)
					Geocaching4LocusApplication.getAuthenticatorHelper().getRestrictions().updateLimits(api.getLastCacheLimits());

				if (isCanceled())
					return null;

				if (cachesToAdd.size() == 0)
					break;

				// FIX for not working distance filter
				if (computeDistance(latitude, longitude, cachesToAdd.get(cachesToAdd.size() - 1)) > distance) {
					removeCachesOverDistance(cachesToAdd, latitude, longitude, distance);

					if (cachesToAdd.size() == 0)
						break;
				}

				PackWaypoints pw = new PackWaypoints(TAG);
				List<Waypoint> waypoints = LocusDataMapper.toLocusPoints(Geocaching4LocusApplication.getAppContext(), cachesToAdd);

				for (Waypoint wpt : waypoints) {
					if (simpleCacheData) {
						wpt.setExtraOnDisplay(getPackageName(), UpdateActivity.class.getName(), UpdateActivity.PARAM_SIMPLE_CACHE_ID, wpt.gcData.getCacheID());
					}

					pw.addWaypoint(wpt);
				}

				sdos.write(pw);

				current = current + cachesToAdd.size();

				// force memory clean
				cachesToAdd = null;
				waypoints = null;
				pw = null;

				sendProgressUpdate();
			}

			sdos.endList();

			Log.i(TAG, "found caches: " + current);

			if (current > 0) {
				return dataFile;
			} else {
				return null;
			}
		} catch (InvalidSessionException e) {
			Log.e(TAG, e.getMessage(), e);
			Geocaching4LocusApplication.getAuthenticatorHelper().invalidateAuthToken();

			throw e;
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new GeocachingApiException(e.getMessage(), e);
		} finally {
			Utils.closeStream(sdos);
		}
	}

	protected void removeCachesOverDistance(List<SimpleGeocache> caches, double latitude, double longitude, double maxDistance) {
		while (caches.size() > 0) {
			SimpleGeocache cache = caches.get(caches.size() - 1);
			double distance = computeDistance(latitude, longitude, cache);

			if (distance > maxDistance) {
				Log.i(TAG, "Cache " + cache.getCacheCode() + " is over distance.");
				caches.remove(cache);
			} else {
				return;
			}
		}
	}

	protected double computeDistance(double latitude, double longitude, SimpleGeocache cache) {
		final double r = 6366.707;

		// convert to radians
		double latitudeFrom = Math.toRadians(latitude);
		double longitudeFrom = Math.toRadians(longitude);
		double latitudeTo = Math.toRadians(cache.getLatitude());
		double longitudeTo = Math.toRadians(cache.getLongitude());

		return Math.acos(Math.sin(latitudeFrom) * Math.sin(latitudeTo) + Math.cos(latitudeFrom) * Math.cos(latitudeTo) * Math.cos(longitudeTo - longitudeFrom)) * r;
	}

	private void login(GeocachingApi api) throws GeocachingApiException {
		String token = Geocaching4LocusApplication.getAuthenticatorHelper().getAuthToken();
		if (token == null) {
			Geocaching4LocusApplication.getAuthenticatorHelper().removeAccount();
			throw new InvalidCredentialsException("Account not found.");
		}

		api.openSession(token);
	}
}
