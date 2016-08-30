package com.arcao.geocaching4locus.import_bookmarks.task;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.arcao.geocaching.api.GeocachingApi;
import com.arcao.geocaching.api.GeocachingApiFactory;
import com.arcao.geocaching.api.data.Geocache;
import com.arcao.geocaching.api.data.bookmarks.Bookmark;
import com.arcao.geocaching.api.exception.GeocachingApiException;
import com.arcao.geocaching.api.impl.live_geocaching_api.filter.CacheCodeFilter;
import com.arcao.geocaching.api.impl.live_geocaching_api.filter.Filter;
import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.authentication.util.AccountManager;
import com.arcao.geocaching4locus.authentication.task.GeocachingApiLoginTask;
import com.arcao.geocaching4locus.base.constants.AppConstants;
import com.arcao.geocaching4locus.base.constants.PrefConstants;
import com.arcao.geocaching4locus.base.task.UserTask;
import com.arcao.geocaching4locus.error.exception.LocusMapRuntimeException;
import com.arcao.geocaching4locus.error.exception.NoResultFoundException;
import com.arcao.geocaching4locus.error.handler.ExceptionHandler;
import com.arcao.geocaching4locus.update.UpdateActivity;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import locus.api.android.ActionDisplayPointsExtended;
import locus.api.android.objects.PackWaypoints;
import locus.api.mapper.LocusDataMapper;
import locus.api.objects.extra.Waypoint;
import locus.api.utils.StoreableWriter;
import locus.api.utils.Utils;
import timber.log.Timber;

public class BookmarkImportTask extends UserTask<String, Void, Boolean> {
  public interface TaskListener {
    void onTaskFinished(boolean result);
    void onTaskFailed(Intent errorIntent);
    void onProgressUpdate(int count, int max);
  }

  private final Context mContext;
  private final WeakReference<TaskListener> mTaskListenerRef;
  private int progress = 0;
  private int max = 0;

  public BookmarkImportTask(Context context, TaskListener listener) {
    mContext = context.getApplicationContext();
    mTaskListenerRef = new WeakReference<>(listener);
  }

  @Override
  protected Boolean doInBackground(String... params) throws Exception {
    AccountManager accountManager = App.get(mContext).getAccountManager();
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
    LocusDataMapper mapper = new LocusDataMapper(mContext);

    GeocachingApi api = GeocachingApiFactory.create();
    GeocachingApiLoginTask.create(mContext, api).perform();

    boolean simpleCacheData = prefs.getBoolean(PrefConstants.DOWNLOADING_SIMPLE_CACHE_DATA, false);
    int logCount = prefs.getInt(PrefConstants.DOWNLOADING_COUNT_OF_LOGS, 5);

    final List<String> geocacheCodes = new ArrayList<>();
    if (params.length == 1 && isGuid(params[0])) {
      String guid = params[0];

      Timber.d("source: import_from_bookmark;guid=" + guid);

      // retrieve Bookmark list geocaches
      List<Bookmark> bookmarks = api.getBookmarkListByGuid(guid);

      for (Bookmark bookmark : bookmarks)
        geocacheCodes.add(bookmark.getCacheCode());

    } else {
      Collections.addAll(geocacheCodes, params);
    }

    Timber.d("source: import_from_bookmark;gccodes=" + geocacheCodes);

    max = geocacheCodes.size();
    if (max <= 0)
      throw new NoResultFoundException();

    GeocachingApi.ResultQuality resultQuality = GeocachingApi.ResultQuality.FULL;
    if (simpleCacheData) {
      resultQuality = GeocachingApi.ResultQuality.LITE;
      logCount = 0;
    }

    StoreableWriter writer = null;

    try {
      File dataFile = ActionDisplayPointsExtended.getCacheFileName(mContext);
      writer = new StoreableWriter(ActionDisplayPointsExtended.getCacheFileOutputStream(mContext));

      publishProgress();

      progress = 0;
      int cachesPerRequest = AppConstants.CACHES_PER_REQUEST;
      while (progress < max) {
        long startTime = System.currentTimeMillis();

        List<String> requestedCaches = geocacheCodes.subList(progress,
            Math.min(max, progress + cachesPerRequest));

        List<Geocache> cachesToAdd = api.searchForGeocaches(resultQuality, cachesPerRequest, logCount, 0, Collections.singletonList(
            (Filter) new CacheCodeFilter(requestedCaches.toArray(new String[requestedCaches.size()]))
        ), null);

        if (!simpleCacheData)
          accountManager.getRestrictions().updateLimits(api.getLastGeocacheLimits());

        if (isCancelled())
          return false;

        if (cachesToAdd.size() == 0)
          break;

        PackWaypoints pw = new PackWaypoints("BookmarkImport");
        List<Waypoint> waypoints = mapper.toLocusPoints(cachesToAdd);

        for (Waypoint wpt : waypoints) {
          if (simpleCacheData) {
            wpt.setExtraOnDisplay(mContext.getPackageName(), UpdateActivity.class.getName(), UpdateActivity.PARAM_SIMPLE_CACHE_ID, wpt.gcData.getCacheID());
          }

          pw.addWaypoint(wpt);
        }

        writer.write(pw);

        progress += cachesToAdd.size();
        publishProgress();

        long requestDuration = System.currentTimeMillis() - startTime;
        cachesPerRequest = computeCachesPerRequest(cachesPerRequest, requestDuration);
      }

      Timber.i("found caches: " + progress);

      if (progress > 0) {
        try {
          ActionDisplayPointsExtended.sendPacksFile(mContext, dataFile, true, false, Intent.FLAG_ACTIVITY_NEW_TASK);
          return true;
        } catch (Throwable t) {
          throw new LocusMapRuntimeException(t);
        }
      } else {
        throw new NoResultFoundException();
      }
    } catch (IOException e) {
      Timber.e(e, e.getMessage());
      throw new GeocachingApiException(e.getMessage(), e);
    } finally {
      Utils.closeStream(writer);
    }
  }

  private boolean isGuid(String value) {
    return value.indexOf('-') >= 0;
  }

  @Override
  protected void onPostExecute(Boolean result) {
    super.onPostExecute(result);

    TaskListener listener = mTaskListenerRef.get();
    if (listener != null)
      listener.onTaskFinished(result);
  }

  @Override
  protected void onProgressUpdate(Void... values) {
    TaskListener listener = mTaskListenerRef.get();
    if (listener != null)
      listener.onProgressUpdate(progress, max);
  }


  @Override
  protected void onException(Throwable t) {
    super.onException(t);

    if (isCancelled())
      return;

    Timber.e(t, t.getMessage());

    Intent intent = new ExceptionHandler(mContext).handle(t);

    TaskListener listener = mTaskListenerRef.get();
    if (listener != null) {
      listener.onTaskFailed(intent);
    }
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
