package com.arcao.geocaching4locus.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.widget.Toast;
import com.arcao.geocaching.api.GeocachingApi;
import com.arcao.geocaching.api.data.SimpleGeocache;
import com.arcao.geocaching.api.data.type.CacheType;
import com.arcao.geocaching.api.data.type.ContainerType;
import com.arcao.geocaching.api.exception.GeocachingApiException;
import com.arcao.geocaching.api.exception.InvalidCredentialsException;
import com.arcao.geocaching.api.exception.InvalidSessionException;
import com.arcao.geocaching.api.exception.NetworkException;
import com.arcao.geocaching.api.impl.LiveGeocachingApiFactory;
import com.arcao.geocaching.api.impl.live_geocaching_api.filter.*;
import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.UpdateActivity;
import com.arcao.geocaching4locus.authentication.helper.AuthenticatorHelper;
import com.arcao.geocaching4locus.constants.PrefConstants;
import com.arcao.geocaching4locus.util.LiveMapNotificationManager;
import locus.api.android.ActionDisplayPoints;
import locus.api.android.objects.PackWaypoints;
import locus.api.android.utils.exceptions.RequiredVersionMissingException;
import locus.api.mapper.LocusDataMapper;
import locus.api.objects.extra.Waypoint;
import timber.log.Timber;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

public class LiveMapService extends IntentService {
	public static final String PARAM_LATITUDE = "LATITUDE";
	public static final String PARAM_LONGITUDE = "LONGITUDE";
	public static final String PARAM_TOP_LEFT_LATITUDE = "TOP_LEFT_LATITUDE";
	public static final String PARAM_TOP_LEFT_LONGITUDE = "TOP_LEFT_LONGITUDE";
	public static final String PARAM_BOTTOM_RIGHT_LATITUDE = "BOTTOM_RIGHT_LATITUDE";
	public static final String PARAM_BOTTOM_RIGHT_LONGITUDE = "BOTTOM_RIGHT_LONGITUDE";

	private static final int REQUESTS = 5;
	private static final int CACHES_PER_REQUEST = 50;
	private static final int CACHES_COUNT = REQUESTS * CACHES_PER_REQUEST;

	private static final String PACK_WAYPOINT_PREFIX = "LiveMap|";
	private static final int LIVEMAP_DISTANCE = 60000;

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
	private Boolean excludeIgnoreList;

	protected SharedPreferences prefs;

	public LiveMapService() {
		super("LiveMapService");
	}

	@Override
	public void onCreate() {
		super.onCreate();

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// increase count of jobs
		countOfJobs.incrementAndGet();
		Timber.d("New job, count=" + countOfJobs.get());

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		int jobId = countOfJobs.get();
		Timber.d("Handling job, count=" + jobId);

		// we skip all jobs before last one
		if (countOfJobs.getAndDecrement() > 1)
			return;

		loadConfiguration(prefs);

		double latitude = intent.getDoubleExtra(PARAM_LATITUDE, 0D);
		double longitude = intent.getDoubleExtra(PARAM_LONGITUDE, 0D);
		double topLeftLatitude = intent.getDoubleExtra(PARAM_TOP_LEFT_LATITUDE, 0D);
		double topLeftLongitude = intent.getDoubleExtra(PARAM_TOP_LEFT_LONGITUDE, 0D);
		double bottomRightLatitude = intent.getDoubleExtra(PARAM_BOTTOM_RIGHT_LATITUDE, 0D);
		double bottomRightLongitude = intent.getDoubleExtra(PARAM_BOTTOM_RIGHT_LONGITUDE, 0D);

		try {
			sendCaches(latitude, longitude, topLeftLatitude, topLeftLongitude, bottomRightLatitude, bottomRightLongitude);
		} catch (RequiredVersionMissingException e) {
			Timber.e(e.getMessage(), e);
			showMessage("Error: " + e.getMessage());

			// disable live map
			prefs.edit().putBoolean(PrefConstants.LIVE_MAP, false).apply();
		} catch (InvalidCredentialsException e) {
			Timber.e(e.getMessage(), e);
			showMessage(getString(R.string.error_credentials));

			// disable live map
			prefs.edit().putBoolean(PrefConstants.LIVE_MAP, false).apply();
		} catch (NetworkException e) {
			Timber.e(e.getMessage(), e);
			showMessage(getString(R.string.error_network));
		} catch (Exception e) {
			Timber.e(e.getMessage(), e);
		} finally {
			Timber.d("Job finished.");
		}
	}

	protected void loadConfiguration(SharedPreferences prefs) {
		showFound = prefs.getBoolean(PrefConstants.FILTER_SHOW_FOUND, false);
		showOwn = prefs.getBoolean(PrefConstants.FILTER_SHOW_OWN, false);
		showDisabled = prefs.getBoolean(PrefConstants.FILTER_SHOW_DISABLED, false);

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

	protected CacheType[] getCacheTypeFilterResult(SharedPreferences prefs) {
		List<CacheType> filter = new Vector<>();

		for (int i = 0; i < CacheType.values().length; i++) {
			if (prefs.getBoolean(PrefConstants.FILTER_CACHE_TYPE_PREFIX + i, true)) {
				filter.add(CacheType.values()[i]);
			}
		}

		return filter.toArray(new CacheType[filter.size()]);
	}

	protected ContainerType[] getContainerTypeFilterResult(SharedPreferences prefs) {
		List<ContainerType> filter = new Vector<>();

		for (int i = 0; i < ContainerType.values().length; i++) {
			if (prefs.getBoolean(PrefConstants.FILTER_CONTAINER_TYPE_PREFIX + i, true)) {
				filter.add(ContainerType.values()[i]);
			}
		}

		return filter.toArray(new ContainerType[filter.size()]);
	}

	protected void sendCaches(double latitude, double longitude, double topLeftLatitude, double topLeftLongitude, double bottomRightLatitude, double bottomRightLongitude) throws GeocachingApiException, RequiredVersionMissingException {
		AuthenticatorHelper authenticatorHelper = App.get(this).getAuthenticatorHelper();

		if (!authenticatorHelper.hasAccount())
			throw new InvalidCredentialsException("Account not found.");

		GeocachingApi api = LiveGeocachingApiFactory.getLiveGeocachingApi();
		LiveMapNotificationManager notificationManager = LiveMapNotificationManager.get(this);

		int current = 0;
		int perPage;

		int requests = 0;

		try {
			login(api);

			String username = authenticatorHelper.getAccount().name;

			notificationManager.setDownloadingProgress(0, CACHES_COUNT);

			while (current < CACHES_COUNT) {
				perPage = (CACHES_COUNT - current < CACHES_PER_REQUEST) ? CACHES_COUNT - current : CACHES_PER_REQUEST;

				if (countOfJobs.get() > 0) {
					Timber.d("New job found, skipped downloading next caches ...");
					break;
				}

				List<SimpleGeocache> caches;

				if (current == 0) {
					caches = api.searchForGeocaches(true, perPage, 0, 0, new Filter[] {
							new PointRadiusFilter(latitude, longitude, LIVEMAP_DISTANCE),
							new GeocacheTypeFilter(cacheTypes),
							new GeocacheContainerSizeFilter(containerTypes),
							new GeocacheExclusionsFilter(false, showDisabled ? null : true, null),
							new NotFoundByUsersFilter(showFound ? null : username),
							new NotHiddenByUsersFilter(showOwn ? null : username),
							new DifficultyFilter(difficultyMin, difficultyMax),
							new TerrainFilter(terrainMin, terrainMax),
							new ViewportFilter(topLeftLatitude, topLeftLongitude, bottomRightLatitude, bottomRightLongitude),
							new BookmarksExcludeFilter(excludeIgnoreList)
					});
				} else {
					caches = api.getMoreGeocaches(true, current, perPage, 0, 0);
				}

				if (caches.size() == 0)
					break;

				if (!prefs.getBoolean(PrefConstants.LIVE_MAP, false))
					break;

				current += caches.size();

				requests++;

				PackWaypoints pw = new PackWaypoints(PACK_WAYPOINT_PREFIX + requests);
				for (SimpleGeocache cache : caches) {
					Waypoint wpt = LocusDataMapper.toLocusPoint(getApplicationContext(), cache);
					wpt.setExtraOnDisplay(getPackageName(), UpdateActivity.class.getName(), UpdateActivity.PARAM_SIMPLE_CACHE_ID, cache.getCacheCode());
					pw.addWaypoint(wpt);
				}

				ActionDisplayPoints.sendPackSilent(this, pw, false);

				notificationManager.setDownloadingProgress(current, CACHES_COUNT);

				if (caches.size() != perPage)
					break;
			}
		} catch (InvalidSessionException e) {
			Timber.e(e.getMessage(), e);
			authenticatorHelper.invalidateAuthToken();

			throw e;
		} finally {
			Timber.i("Count of caches sent to Locus: " + current);
		}

		notificationManager.setDownloadingProgress(CACHES_COUNT, CACHES_COUNT);

		// HACK we must remove old PackWaypoints from the map
		for (int i = requests + 1; i < REQUESTS; i++) {
			PackWaypoints pw = new PackWaypoints(PACK_WAYPOINT_PREFIX + i);
			ActionDisplayPoints.sendPackSilent(this, pw, false);
		}
	}

	private void login(GeocachingApi api) throws GeocachingApiException {
		AuthenticatorHelper authenticatorHelper = App.get(this).getAuthenticatorHelper();

		String token = authenticatorHelper.getAuthToken();
		if (token == null) {
			authenticatorHelper.removeAccount();
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

	protected void showMessage(final String message) {
		new Handler(Looper.getMainLooper()).post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.livemap_error, message), Toast.LENGTH_LONG).show();}
		});
	}
}
