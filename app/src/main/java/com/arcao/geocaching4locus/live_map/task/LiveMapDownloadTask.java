package com.arcao.geocaching4locus.live_map.task;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import com.arcao.geocaching.api.GeocachingApi;
import com.arcao.geocaching.api.GeocachingApi.ResultQuality;
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
import com.arcao.geocaching.api.filter.Filter;
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
import com.arcao.geocaching4locus.authentication.util.AccountManager;
import com.arcao.geocaching4locus.base.constants.PrefConstants;
import com.arcao.geocaching4locus.base.util.PreferenceUtil;
import com.arcao.geocaching4locus.error.exception.LocusMapRuntimeException;
import com.arcao.geocaching4locus.live_map.util.LiveMapNotificationManager;
import com.arcao.geocaching4locus.update.UpdateActivity;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import locus.api.android.ActionDisplayPoints;
import locus.api.android.objects.PackWaypoints;
import locus.api.android.utils.exceptions.RequiredVersionMissingException;
import locus.api.mapper.DataMapper;
import locus.api.objects.extra.Waypoint;
import timber.log.Timber;

import static com.arcao.geocaching4locus.live_map.LiveMapService.PARAM_BOTTOM_RIGHT_LATITUDE;
import static com.arcao.geocaching4locus.live_map.LiveMapService.PARAM_BOTTOM_RIGHT_LONGITUDE;
import static com.arcao.geocaching4locus.live_map.LiveMapService.PARAM_LATITUDE;
import static com.arcao.geocaching4locus.live_map.LiveMapService.PARAM_LONGITUDE;
import static com.arcao.geocaching4locus.live_map.LiveMapService.PARAM_TOP_LEFT_LATITUDE;
import static com.arcao.geocaching4locus.live_map.LiveMapService.PARAM_TOP_LEFT_LONGITUDE;

/**
 * Created by Arcao on 13.10.2017.
 */

public class LiveMapDownloadTask extends Thread {
  private static final int REQUESTS = 5;
  private static final int CACHES_PER_REQUEST = 50;
  private static final int CACHES_COUNT = REQUESTS * CACHES_PER_REQUEST;

  private static final String PACK_WAYPOINT_PREFIX = "LiveMap|";
  private static final int LIVEMAP_DISTANCE = 60000;

  private final Context context;
  private final SharedPreferences preferences;
  private final AccountManager accountManager;
  private final LiveMapNotificationManager notificationManager;
  private final DataMapper mapper;

  private final Queue<Intent> taskQueue = new LinkedList<>();
  private boolean terminated = false;

  public LiveMapDownloadTask(Context context, LiveMapNotificationManager notificationManager) {
    this.context = context;
    this.notificationManager = notificationManager;

    preferences = PreferenceManager.getDefaultSharedPreferences(context);
    accountManager = App.get(context).getAccountManager();
    mapper = new DataMapper(context);
  }

  @UiThread
  public void addTask(Intent intent) {
    synchronized (taskQueue) {
      taskQueue.add(intent);
      taskQueue.notify();
    }
  }

  @UiThread
  public void destroy() {
    synchronized (taskQueue) {
      terminated = true;
      taskQueue.notify();
    }
  }

  @WorkerThread
  public void onTaskFinished(Intent task) {
    // do nothing
  }

  public static void cleanMapItems(Context context) {
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


  @WorkerThread
  @Override
  public void run() {
    try {
      while (!terminated) {
        Intent task = null;

        synchronized (taskQueue) {
          // chose latest
          while (!taskQueue.isEmpty()) {
            if (task != null)
              onTaskFinished(task);

            task = taskQueue.poll();
          }

          // if nothing in queue, wait
          if (task == null) {
            taskQueue.wait();
            continue;
          }
        }

        try {
          downloadTask(task);
        } catch (Exception e) {
          handleTaskException(e);
        } finally {
          onTaskFinished(task);
        }
      }
    } catch (InterruptedException e) {
      Timber.e(e);
    }
  }

  private void handleTaskException(@NonNull Exception e) {
    if (e instanceof LocusMapRuntimeException) {
      Timber.e(e, e.getMessage());
      notificationManager.showLiveMapError("Locus Map Error: " + e.getMessage());

      // disable live map
      preferences.edit().putBoolean(PrefConstants.LIVE_MAP, false).apply();
    } else if (e instanceof InvalidCredentialsException) {
      Timber.e(e, e.getMessage());
      notificationManager.showLiveMapError(R.string.error_no_account);

      // disable live map
      preferences.edit().putBoolean(PrefConstants.LIVE_MAP, false).apply();
    } else if (e instanceof NetworkException) {
      Timber.e(e, e.getMessage());
      notificationManager.showLiveMapError(R.string.error_network_unavailable);
    } else {
      Timber.e(e, e.getMessage());
    }
  }

  @WorkerThread
  private void downloadTask(@NonNull Intent task)
      throws GeocachingApiException, RequiredVersionMissingException {

    boolean downloadHints = preferences.getBoolean(PrefConstants.LIVE_MAP_DOWNLOAD_HINTS, false);

    int current = 0;
    int requests = 0;
    try {
      GeocachingApi api = GeocachingApiFactory.create();
      GeocachingApiLoginTask.create(context, api).perform();

      notificationManager.setDownloadingProgress(0, CACHES_COUNT);

      while (current < CACHES_COUNT) {
        int perPage = (CACHES_COUNT - current < CACHES_PER_REQUEST) ? CACHES_COUNT - current
            : CACHES_PER_REQUEST;

        if (!taskQueue.isEmpty()) {
          Timber.d("New task found, skipped downloading next caches ...");
          return;
        }

        ResultQuality resultQuality = downloadHints ? ResultQuality.SUMMARY : ResultQuality.LITE;

        List<Geocache> caches;

        if (current == 0) {
          List<Filter> filters = createFilters(task);
          caches = api.searchForGeocaches(resultQuality, perPage, 0, 0, filters, null);
        } else {
          caches = api.getMoreGeocaches(resultQuality, current, perPage, 0, 0);
        }

        if (caches.isEmpty()) break;

        if (!notificationManager.isLiveMapEnabled()) break;

        current += caches.size();
        requests++;

        PackWaypoints pw = new PackWaypoints(PACK_WAYPOINT_PREFIX + requests);
        for (Geocache cache : caches) {
          Waypoint wpt = mapper.createLocusWaypoint(cache);
          if (wpt == null) continue;

          wpt.setExtraOnDisplay(context.getPackageName(), UpdateActivity.class.getName(),
              UpdateActivity.PARAM_SIMPLE_CACHE_ID, cache.code());
          pw.addWaypoint(wpt);
        }

        try {
          ActionDisplayPoints.sendPackSilent(context, pw, false);
        } catch (Throwable t) {
          throw new LocusMapRuntimeException(t);
        }

        notificationManager.setDownloadingProgress(current, CACHES_COUNT);

        if (caches.size() != perPage) break;
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
      ActionDisplayPoints.sendPackSilent(context, pw, false);
    }
  }

  @NonNull
  private List<Filter> createFilters(@NonNull Intent task) {
    List<Filter> filters = new ArrayList<>(10);

    @SuppressWarnings("ConstantConditions")
    String userName = accountManager.getAccount().name();
    boolean premiumMember = accountManager.isPremium();

    double latitude = task.getDoubleExtra(PARAM_LATITUDE, 0D);
    double longitude = task.getDoubleExtra(PARAM_LONGITUDE, 0D);
    double topLeftLatitude = task.getDoubleExtra(PARAM_TOP_LEFT_LATITUDE, 0D);
    double topLeftLongitude = task.getDoubleExtra(PARAM_TOP_LEFT_LONGITUDE, 0D);
    double bottomRightLatitude = task.getDoubleExtra(PARAM_BOTTOM_RIGHT_LATITUDE, 0D);
    double bottomRightLongitude = task.getDoubleExtra(PARAM_BOTTOM_RIGHT_LONGITUDE, 0D);

    filters.add(new PointRadiusFilter(latitude, longitude, LIVEMAP_DISTANCE));
    filters.add(new ViewportFilter(topLeftLatitude, topLeftLongitude, bottomRightLatitude, bottomRightLongitude));

    boolean showDisabled = preferences.getBoolean(PrefConstants.FILTER_SHOW_DISABLED, false);
    filters.add(
        new GeocacheExclusionsFilter(false, showDisabled ? null : true, null, null, null, null));

    boolean showFound = preferences.getBoolean(PrefConstants.FILTER_SHOW_FOUND, false);
    if (!showFound) {
      filters.add(new NotFoundByUsersFilter(userName));
    }

    boolean showOwn = preferences.getBoolean(PrefConstants.FILTER_SHOW_OWN, false);
    if (!showOwn) {
      filters.add(new NotHiddenByUsersFilter(userName));
    }

    if (premiumMember) {
      filters.add(new GeocacheTypeFilter(getSelectedGeocacheTypes()));
      filters.add(new GeocacheContainerSizeFilter(getSelectedContainerTypes()));

      float difficultyMin = PreferenceUtil.getParsedFloat(preferences, PrefConstants.FILTER_DIFFICULTY_MIN, 1);
      float difficultyMax = PreferenceUtil.getParsedFloat(preferences, PrefConstants.FILTER_DIFFICULTY_MAX, 5);
      if (difficultyMin > 1 || difficultyMax < 5) {
        filters.add(new DifficultyFilter(difficultyMin, difficultyMax));
      }

      float terrainMin = PreferenceUtil.getParsedFloat(preferences, PrefConstants.FILTER_TERRAIN_MIN, 1);
      float terrainMax = PreferenceUtil.getParsedFloat(preferences, PrefConstants.FILTER_TERRAIN_MAX, 5);
      if (terrainMin > 1 || terrainMax < 5) {
        filters.add(new TerrainFilter(terrainMin, terrainMax));
      }

      // TODO: 3. 9. 2015 Move it to configuration
      filters.add(new BookmarksExcludeFilter(true));
    }

    return filters;
  }

  private GeocacheType[] getSelectedGeocacheTypes() {
    List<GeocacheType> filter = new ArrayList<>(GeocacheType.values().length);

    final int len = GeocacheType.values().length;
    for (int i = 0; i < len; i++) {
      if (preferences.getBoolean(PrefConstants.FILTER_CACHE_TYPE_PREFIX + i, true)) {
        filter.add(GeocacheType.values()[i]);
      }
    }

    return filter.toArray(new GeocacheType[filter.size()]);
  }

  private ContainerType[] getSelectedContainerTypes() {
    List<ContainerType> filter = new ArrayList<>(ContainerType.values().length);

    final int len = ContainerType.values().length;
    for (int i = 0; i < len; i++) {
      if (preferences.getBoolean(PrefConstants.FILTER_CONTAINER_TYPE_PREFIX + i, true)) {
        filter.add(ContainerType.values()[i]);
      }
    }

    return filter.toArray(new ContainerType[filter.size()]);
  }
}
