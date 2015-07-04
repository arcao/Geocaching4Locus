package com.arcao.geocaching4locus.service;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.arcao.geocaching.api.GeocachingApi;
import com.arcao.geocaching.api.GeocachingApiFactory;
import com.arcao.geocaching.api.data.Geocache;
import com.arcao.geocaching.api.data.coordinates.Coordinates;
import com.arcao.geocaching.api.data.coordinates.CoordinatesFormatter;
import com.arcao.geocaching.api.data.type.ContainerType;
import com.arcao.geocaching.api.data.type.GeocacheType;
import com.arcao.geocaching.api.exception.GeocachingApiException;
import com.arcao.geocaching.api.exception.InvalidCredentialsException;
import com.arcao.geocaching.api.exception.InvalidSessionException;
import com.arcao.geocaching.api.impl.live_geocaching_api.filter.BookmarksExcludeFilter;
import com.arcao.geocaching.api.impl.live_geocaching_api.filter.DifficultyFilter;
import com.arcao.geocaching.api.impl.live_geocaching_api.filter.GeocacheContainerSizeFilter;
import com.arcao.geocaching.api.impl.live_geocaching_api.filter.GeocacheExclusionsFilter;
import com.arcao.geocaching.api.impl.live_geocaching_api.filter.GeocacheTypeFilter;
import com.arcao.geocaching.api.impl.live_geocaching_api.filter.NotFoundByUsersFilter;
import com.arcao.geocaching.api.impl.live_geocaching_api.filter.NotHiddenByUsersFilter;
import com.arcao.geocaching.api.impl.live_geocaching_api.filter.PointRadiusFilter;
import com.arcao.geocaching.api.impl.live_geocaching_api.filter.TerrainFilter;
import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.SearchNearestActivity;
import com.arcao.geocaching4locus.UpdateActivity;
import com.arcao.geocaching4locus.authentication.helper.AuthenticatorHelper;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.constants.PrefConstants;
import com.arcao.geocaching4locus.exception.LocusMapRuntimeException;
import com.arcao.geocaching4locus.exception.NoResultFoundException;

import org.acra.ACRA;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import locus.api.android.ActionDisplayPointsExtended;
import locus.api.android.objects.PackWaypoints;
import locus.api.mapper.LocusDataMapper;
import locus.api.objects.extra.Waypoint;
import locus.api.utils.StoreableListFileOutput;
import locus.api.utils.Utils;
import timber.log.Timber;

public class SearchGeocacheService extends AbstractService {
	public static final String PARAM_LATITUDE = "LATITUDE";
	public static final String PARAM_LONGITUDE = "LONGITUDE";
	private static final String PACK_WAYPOINT = "SearchGeocacheService";

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
	private GeocacheType[] cacheTypes;
	private ContainerType[] containerTypes;
	private Boolean excludeIgnoreList;

	public SearchGeocacheService() {
		super("SearchGeocacheService", R.string.downloading, R.string.downloading);
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
		return new Intent(this, SearchNearestActivity.class).addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
	}

	public void sendProgressUpdate() {
		sendProgressUpdate(current, count);
	}

	@Override
	protected void run(Intent intent) throws Exception {
		double latitude = intent.getDoubleExtra(PARAM_LATITUDE, 0D);
		double longitude = intent.getDoubleExtra(PARAM_LONGITUDE, 0D);

		sendProgressUpdate();
		File file = downloadCaches(new Coordinates(latitude, longitude));
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
			distanceString = prefs.getString(PrefConstants.FILTER_DISTANCE, "50");
		} else {
			distanceString = prefs.getString(PrefConstants.FILTER_DISTANCE, "31.0685596");
		}

		try {
			distance = Float.parseFloat(distanceString);
		} catch (NumberFormatException e) {
			Timber.e(e, e.getMessage());
			distance = 100;
		}

		if (prefs.getBoolean(PrefConstants.IMPERIAL_UNITS, false)) {
			// get kilometers from miles
			distance *= AppConstants.MILES_PER_KILOMETER;
		}

		// fix for min and max distance error in Geocaching Live API
		distance = Math.max(Math.min(distance, 50), 0.1);

		current = 0;
		count = prefs.getInt(PrefConstants.DOWNLOADING_COUNT_OF_CACHES, AppConstants.DOWNLOADING_COUNT_OF_CACHES_DEFAULT);

		logCount = prefs.getInt(PrefConstants.DOWNLOADING_COUNT_OF_LOGS, 5);

		// default values for basic member
		difficultyMin = 1;
		difficultyMax = 5;

		terrainMin = 1;
		terrainMax = 5;

		// Premium member feature?
		if (App.get(this).getAuthenticatorHelper().getRestrictions().isPremiumMember()) {
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
				ActionDisplayPointsExtended.sendPacksFile(getApplication(), file, true, false, Intent.FLAG_ACTIVITY_NEW_TASK);
			}
		} catch (Throwable t) {
			throw new LocusMapRuntimeException(t);
		}
	}

	private GeocacheType[] getCacheTypeFilterResult(@NonNull SharedPreferences prefs) {
		Vector<GeocacheType> filter = new Vector<>();

		for (int i = 0; i < GeocacheType.values().length; i++) {
			if (prefs.getBoolean(PrefConstants.FILTER_CACHE_TYPE_PREFIX + i, true)) {
				filter.add(GeocacheType.values()[i]);
			}
		}

		return filter.toArray(new GeocacheType[filter.size()]);
	}

	private ContainerType[] getContainerTypeFilterResult(@NonNull SharedPreferences prefs) {
		Vector<ContainerType> filter = new Vector<>();

		for (int i = 0; i < ContainerType.values().length; i++) {
			if (prefs.getBoolean(PrefConstants.FILTER_CONTAINER_TYPE_PREFIX + i, true)) {
				filter.add(ContainerType.values()[i]);
			}
		}

		return filter.toArray(new ContainerType[filter.size()]);
	}

	private File downloadCaches(@NonNull Coordinates coordinates) throws GeocachingApiException, NoResultFoundException {
		AuthenticatorHelper authenticatorHelper = App.get(this).getAuthenticatorHelper();
		if (!authenticatorHelper.hasAccount())
			throw new InvalidCredentialsException("Account not found.");

		if (isCanceled())
			return null;

		ACRA.getErrorReporter().putCustomData("source", "search;" + coordinates.toString(CoordinatesFormatter.LAT_LON_DECDEGREE_COMMA));

		GeocachingApi api = GeocachingApiFactory.create();


		GeocachingApi.ResultQuality resultQuality = authenticatorHelper.getRestrictions().isPremiumMember() ?
						GeocachingApi.ResultQuality.FULL : GeocachingApi.ResultQuality.SUMMARY;
		if (simpleCacheData) {
			resultQuality = GeocachingApi.ResultQuality.LITE;
			logCount = 0;
		}

		StoreableListFileOutput slfo = null;

		try {
			File dataFile = ActionDisplayPointsExtended.getCacheFileName(this);

			login(api);

			String username = authenticatorHelper.getAccount().name;

			slfo = new StoreableListFileOutput(ActionDisplayPointsExtended.getCacheFileOutputStream(this));
			slfo.beginList();

			sendProgressUpdate();

			current = 0;
			int cachesPerRequest = AppConstants.CACHES_PER_REQUEST;

			while (current < count) {
				long startTime = System.currentTimeMillis();

				List<Geocache> cachesToAdd;

				if (current == 0) {
					cachesToAdd = api.searchForGeocaches(resultQuality, Math.min(cachesPerRequest, count - current), logCount, 0, Arrays.asList(
							new PointRadiusFilter(coordinates, (long) (distance * 1000)),
							new GeocacheTypeFilter(cacheTypes),
							new GeocacheContainerSizeFilter(containerTypes),
							new GeocacheExclusionsFilter(false, showDisabled ? null : true, null),
							new NotFoundByUsersFilter(showFound ? null : username),
							new NotHiddenByUsersFilter(showOwn ? null : username),
							new DifficultyFilter(difficultyMin, difficultyMax),
							new TerrainFilter(terrainMin, terrainMax),
							new BookmarksExcludeFilter(excludeIgnoreList)
					), null);
				} else {
					cachesToAdd = api.getMoreGeocaches(resultQuality, current, Math.min(cachesPerRequest, count - current), logCount, 0);
				}

				if (!simpleCacheData)
					authenticatorHelper.getRestrictions().updateLimits(api.getLastGeocacheLimits());

				if (isCanceled())
					return null;

				if (cachesToAdd.size() == 0)
					break;

				// FIX for not working distance filter
				if (computeDistance(coordinates, cachesToAdd.get(cachesToAdd.size() - 1)) > distance) {
					removeCachesOverDistance(cachesToAdd, coordinates, distance);

					if (cachesToAdd.size() == 0)
						break;
				}

				PackWaypoints pw = new PackWaypoints(PACK_WAYPOINT);
				List<Waypoint> waypoints = LocusDataMapper.toLocusPoints(this, cachesToAdd);

				for (Waypoint wpt : waypoints) {
					if (simpleCacheData) {
						wpt.setExtraOnDisplay(getPackageName(), UpdateActivity.class.getName(), UpdateActivity.PARAM_SIMPLE_CACHE_ID, wpt.gcData.getCacheID());
					}

					pw.addWaypoint(wpt);
				}

				slfo.write(pw);

				current += cachesToAdd.size();

				sendProgressUpdate();
				long requestDuration = System.currentTimeMillis() - startTime;
				cachesPerRequest = computeCachesPerRequest(cachesPerRequest, requestDuration);
			}

			slfo.endList();

			Timber.i("found caches: " + current);

			if (current > 0) {
				return dataFile;
			} else {
				throw new NoResultFoundException();
			}
		} catch (InvalidSessionException e) {
			Timber.e(e, e.getMessage());
			authenticatorHelper.invalidateAuthToken();

			throw e;
		} catch (IOException e) {
			Timber.e(e, e.getMessage());
			throw new GeocachingApiException(e.getMessage(), e);
		} finally {
			Utils.closeStream(slfo);
		}
	}

	private void removeCachesOverDistance(@NonNull List<Geocache> caches, @NonNull Coordinates coordinates, double maxDistance) {
		while (caches.size() > 0) {
			Geocache cache = caches.get(caches.size() - 1);
			double distance = computeDistance(coordinates, cache);

			if (distance > maxDistance) {
				Timber.i("Cache " + cache.getCode() + " is over distance.");
				caches.remove(cache);
			} else {
				return;
			}
		}
	}

	private double computeDistance(@NonNull Coordinates coordinates, @NonNull Geocache cache) {
		return cache.getCoordinates().distanceTo(coordinates) / 1000;
	}

	private void login(@NonNull GeocachingApi api) throws GeocachingApiException {
		AuthenticatorHelper authenticatorHelper = App.get(this).getAuthenticatorHelper();

		String token = authenticatorHelper.getAuthToken();
		if (token == null) {
			authenticatorHelper.removeAccount();
			throw new InvalidCredentialsException("Account not found.");
		}

		api.openSession(token);
	}

	private int computeCachesPerRequest(int currentCachesPerRequest, long requestDuration) {
		int cachesPerRequest = currentCachesPerRequest;

		// keep the request time between ADAPTIVE_DOWNLOADING_MIN_TIME_MS and ADAPTIVE_DOWNLOADING_MAX_TIME_MS
		if (requestDuration < AppConstants.ADAPTIVE_DOWNLOADING_MIN_TIME_MS)
			cachesPerRequest+= AppConstants.ADAPTIVE_DOWNLOADING_STEP;

		if (requestDuration > AppConstants.ADAPTIVE_DOWNLOADING_MAX_TIME_MS)
			cachesPerRequest-= AppConstants.ADAPTIVE_DOWNLOADING_STEP;

		// keep the value in a range
		cachesPerRequest = Math.max(cachesPerRequest, AppConstants.ADAPTIVE_DOWNLOADING_MIN_CACHES);
		cachesPerRequest = Math.min(cachesPerRequest, AppConstants.ADAPTIVE_DOWNLOADING_MAX_CACHES);

		return cachesPerRequest;
	}
}
