package com.arcao.geocaching4locus.live_map;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.arcao.geocaching.api.GeocachingApi;
import com.arcao.geocaching.api.GeocachingApiFactory;
import com.arcao.geocaching.api.data.Geocache;
import com.arcao.geocaching.api.data.type.ContainerType;
import com.arcao.geocaching.api.data.type.GeocacheType;
import com.arcao.geocaching.api.exception.GeocachingApiException;
import com.arcao.geocaching.api.exception.InvalidCredentialsException;
import com.arcao.geocaching.api.exception.InvalidSessionException;
import com.arcao.geocaching.api.exception.NetworkException;
import com.arcao.geocaching.api.filter.BookmarksExcludeFilter;
import com.arcao.geocaching.api.filter.DifficultyFilter;
import com.arcao.geocaching.api.filter.GeocacheContainerSizeFilter;
import com.arcao.geocaching.api.filter.GeocacheExclusionsFilter;
import com.arcao.geocaching.api.filter.GeocacheTypeFilter;
import com.arcao.geocaching.api.filter.NotFoundByUsersFilter;
import com.arcao.geocaching.api.filter.NotHiddenByUsersFilter;
import com.arcao.geocaching.api.filter.PointRadiusFilter;
import com.arcao.geocaching.api.filter.TerrainFilter;
import com.arcao.geocaching.api.filter.ViewportFilter;
import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.authentication.task.GeocachingApiLoginTask;
import com.arcao.geocaching4locus.base.constants.AppConstants;
import com.arcao.geocaching4locus.base.util.ResourcesUtil;
import com.arcao.geocaching4locus.update.UpdateActivity;
import com.arcao.geocaching4locus.authentication.util.AccountManager;
import com.arcao.geocaching4locus.base.constants.PrefConstants;
import com.arcao.geocaching4locus.error.exception.LocusMapRuntimeException;
import com.arcao.geocaching4locus.live_map.util.LiveMapNotificationManager;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import locus.api.android.ActionDisplayPoints;
import locus.api.android.objects.PackWaypoints;
import locus.api.android.utils.exceptions.RequiredVersionMissingException;
import locus.api.mapper.DataMapper;
import locus.api.objects.extra.Waypoint;
import timber.log.Timber;

public class LiveMapService extends IntentService {
	private static final String PARAM_LATITUDE = "LATITUDE";
	private static final String PARAM_LONGITUDE = "LONGITUDE";
	private static final String PARAM_TOP_LEFT_LATITUDE = "TOP_LEFT_LATITUDE";
	private static final String PARAM_TOP_LEFT_LONGITUDE = "TOP_LEFT_LONGITUDE";
	private static final String PARAM_BOTTOM_RIGHT_LATITUDE = "BOTTOM_RIGHT_LATITUDE";
	private static final String PARAM_BOTTOM_RIGHT_LONGITUDE = "BOTTOM_RIGHT_LONGITUDE";

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
	private GeocacheType[] cacheTypes;
	private ContainerType[] containerTypes;
	private Boolean excludeIgnoreList;
	private boolean liveMapDownloadHints;

	private SharedPreferences prefs;

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

		double latitude = intent.getDoubleExtra(PARAM_LATITUDE, 0D);
		double longitude = intent.getDoubleExtra(PARAM_LONGITUDE, 0D);
		double topLeftLatitude = intent.getDoubleExtra(PARAM_TOP_LEFT_LATITUDE, 0D);
		double topLeftLongitude = intent.getDoubleExtra(PARAM_TOP_LEFT_LONGITUDE, 0D);
		double bottomRightLatitude = intent.getDoubleExtra(PARAM_BOTTOM_RIGHT_LATITUDE, 0D);
		double bottomRightLongitude = intent.getDoubleExtra(PARAM_BOTTOM_RIGHT_LONGITUDE, 0D);

		try {
			startForeground(AppConstants.NOTIFICATION_ID_LIVEMAP, LiveMapNotificationManager.get(this).createNotification().build());

			sendCaches(latitude, longitude, topLeftLatitude, topLeftLongitude, bottomRightLatitude, bottomRightLongitude);
		} catch (LocusMapRuntimeException e) {
			Timber.e(e, e.getMessage());
			showMessage("Locus Map Error: " + e.getMessage());

			// disable live map
			prefs.edit().putBoolean(PrefConstants.LIVE_MAP, false).apply();
		} catch (InvalidCredentialsException e) {
			Timber.e(e, e.getMessage());
			showMessage(getString(R.string.error_no_account));

			// disable live map
			prefs.edit().putBoolean(PrefConstants.LIVE_MAP, false).apply();
		} catch (NetworkException e) {
			Timber.e(e, e.getMessage());
			showMessage(getString(R.string.error_network_unavailable));
		} catch (Exception e) {
			Timber.e(e, e.getMessage());
		} finally {
			stopForeground(false);

			Timber.d("Job finished.");
		}
	}

	private void loadConfiguration(SharedPreferences prefs) {
		showFound = prefs.getBoolean(PrefConstants.FILTER_SHOW_FOUND, false);
		showOwn = prefs.getBoolean(PrefConstants.FILTER_SHOW_OWN, false);
		showDisabled = prefs.getBoolean(PrefConstants.FILTER_SHOW_DISABLED, false);
		liveMapDownloadHints = prefs.getBoolean(PrefConstants.LIVE_MAP_DOWNLOAD_HINTS, false);

		// default values for basic member
		difficultyMin = 1;
		difficultyMax = 5;

		terrainMin = 1;
		terrainMax = 5;

		// Premium member feature?
		if (App.get(this).getAccountManager().isPremium()) {
			difficultyMin = Float.parseFloat(prefs.getString(PrefConstants.FILTER_DIFFICULTY_MIN, "1"));
			difficultyMax = Float.parseFloat(prefs.getString(PrefConstants.FILTER_DIFFICULTY_MAX, "5"));

			terrainMin = Float.parseFloat(prefs.getString(PrefConstants.FILTER_TERRAIN_MIN, "1"));
			terrainMax = Float.parseFloat(prefs.getString(PrefConstants.FILTER_TERRAIN_MAX, "5"));

			cacheTypes = getSelectedGeocacheTypes(prefs);
			containerTypes = getSelectedContainerTypes(prefs);
			excludeIgnoreList = true;
		}
	}

	private GeocacheType[] getSelectedGeocacheTypes(SharedPreferences prefs) {
		List<GeocacheType> filter = new Vector<>();

		final int len = GeocacheType.values().length;
		for (int i = 0; i < len; i++) {
			if (prefs.getBoolean(PrefConstants.FILTER_CACHE_TYPE_PREFIX + i, true)) {
				filter.add(GeocacheType.values()[i]);
			}
		}

		return filter.toArray(new GeocacheType[filter.size()]);
	}

	private ContainerType[] getSelectedContainerTypes(SharedPreferences prefs) {
		List<ContainerType> filter = new Vector<>();

		final int len = ContainerType.values().length;
		for (int i = 0; i < len; i++) {
			if (prefs.getBoolean(PrefConstants.FILTER_CONTAINER_TYPE_PREFIX + i, true)) {
				filter.add(ContainerType.values()[i]);
			}
		}

		return filter.toArray(new ContainerType[filter.size()]);
	}

	private void sendCaches(double latitude, double longitude, double topLeftLatitude, double topLeftLongitude, double bottomRightLatitude, double bottomRightLongitude) throws GeocachingApiException, RequiredVersionMissingException {
		AccountManager accountManager = App.get(this).getAccountManager();
		LiveMapNotificationManager notificationManager = LiveMapNotificationManager.get(this);
		DataMapper mapper = new DataMapper(this);

		int current = 0;
		int requests = 0;
		try {
			GeocachingApi api = GeocachingApiFactory.create();
			GeocachingApiLoginTask.create(this, api).perform();

			loadConfiguration(prefs);

			//noinspection ConstantConditions
			String username = accountManager.getAccount().name();

			notificationManager.setDownloadingProgress(0, CACHES_COUNT);

			while (current < CACHES_COUNT) {
				int perPage = (CACHES_COUNT - current < CACHES_PER_REQUEST) ? CACHES_COUNT - current : CACHES_PER_REQUEST;

				if (countOfJobs.get() > 0) {
					Timber.d("New job found, skipped downloading next caches ...");
					break;
				}

				GeocachingApi.ResultQuality resultQuality = (liveMapDownloadHints) ? GeocachingApi.ResultQuality.SUMMARY : GeocachingApi.ResultQuality.LITE;

				List<Geocache> caches;

				if (current == 0) {
					caches = api.searchForGeocaches(resultQuality, perPage, 0, 0, Arrays.asList(
							new PointRadiusFilter(latitude, longitude, LIVEMAP_DISTANCE),
							new GeocacheTypeFilter(cacheTypes),
							new GeocacheContainerSizeFilter(containerTypes),
							new GeocacheExclusionsFilter(false, showDisabled ? null : true, null, null, null, null),
							new NotFoundByUsersFilter(showFound ? null : username),
							new NotHiddenByUsersFilter(showOwn ? null : username),
							new DifficultyFilter(difficultyMin, difficultyMax),
							new TerrainFilter(terrainMin, terrainMax),
							new ViewportFilter(topLeftLatitude, topLeftLongitude, bottomRightLatitude, bottomRightLongitude),
							new BookmarksExcludeFilter(excludeIgnoreList)
					), null);
				} else {
					caches = api.getMoreGeocaches(resultQuality, current, perPage, 0, 0);
				}

				if (caches.isEmpty())
					break;

				if (!notificationManager.isLiveMapEnabled())
					break;

				current += caches.size();

				requests++;

				PackWaypoints pw = new PackWaypoints(PACK_WAYPOINT_PREFIX + requests);
				for (Geocache cache : caches) {
					Waypoint wpt = mapper.createLocusWaypoint(cache);
					if (wpt == null)
						continue;

					wpt.setExtraOnDisplay(getPackageName(), UpdateActivity.class.getName(), UpdateActivity.PARAM_SIMPLE_CACHE_ID, cache.code());
					pw.addWaypoint(wpt);
				}

				try {
					ActionDisplayPoints.sendPackSilent(this, pw, false);
				} catch (Throwable t) {
					throw new LocusMapRuntimeException(t);
				}

				notificationManager.setDownloadingProgress(current, CACHES_COUNT);

				if (caches.size() != perPage)
					break;
			}
		} catch (InvalidSessionException e) {
			Timber.e(e, e.getMessage());
			accountManager.invalidateOAuthToken();

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

	private void showMessage(final String message) {
		new Handler(Looper.getMainLooper()).post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), ResourcesUtil.getText(getApplicationContext(), R.string.error_live_map, message), Toast.LENGTH_LONG).show();}
		});
	}

	public static void cleanLiveMapItems(Context context) {
		try {
			for (int i = 1; i < REQUESTS; i++) {
				PackWaypoints pw = new PackWaypoints(PACK_WAYPOINT_PREFIX + i);
				ActionDisplayPoints.sendPackSilent(context, pw, false);
			}
		} catch (Throwable t) {
			t = new LocusMapRuntimeException(t);
			Timber.e(t, t.getMessage());
		}
	}
}
