package com.arcao.geocaching4locus.import_gc.task;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.arcao.geocaching.api.GeocachingApi;
import com.arcao.geocaching.api.GeocachingApiFactory;
import com.arcao.geocaching.api.data.Geocache;
import com.arcao.geocaching.api.exception.GeocachingApiException;
import com.arcao.geocaching.api.exception.InvalidSessionException;
import com.arcao.geocaching.api.filter.CacheCodeFilter;
import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.authentication.task.GeocachingApiLoginTask;
import com.arcao.geocaching4locus.authentication.util.AccountManager;
import com.arcao.geocaching4locus.base.constants.AppConstants;
import com.arcao.geocaching4locus.base.constants.PrefConstants;
import com.arcao.geocaching4locus.base.task.UserTask;
import com.arcao.geocaching4locus.error.exception.CacheNotFoundException;
import com.arcao.geocaching4locus.error.exception.IntendedException;
import com.arcao.geocaching4locus.error.exception.LocusMapRuntimeException;
import com.arcao.geocaching4locus.error.handler.ExceptionHandler;
import com.arcao.geocaching4locus.import_gc.ImportActivity;
import com.arcao.geocaching4locus.search_nearest.parcel.ParcelFile;
import com.arcao.wherigoservice.api.WherigoApiFactory;
import com.arcao.wherigoservice.api.WherigoService;

import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import locus.api.android.ActionDisplayPointsExtended;
import locus.api.android.objects.PackWaypoints;
import locus.api.mapper.DataMapper;
import locus.api.objects.extra.Waypoint;
import locus.api.utils.StoreableWriter;
import locus.api.utils.Utils;
import timber.log.Timber;

public class ImportTask extends UserTask<String, Integer, Intent> {
    private static final String PACK_WAYPOINTS_NAME = "IMPORT";

    public interface TaskListener {
        void onTaskFinished(@Nullable Intent intent);

        void onTaskError(@NonNull Intent errorIntent);

        void onProgressUpdate(int current, int count);
    }

    private final Context context;
    private final WeakReference<TaskListener> taskListenerRef;

    public ImportTask(Context context, TaskListener listener) {
        this.taskListenerRef = new WeakReference<>(listener);
        this.context = context.getApplicationContext();
    }

    @Override
    protected void onPostExecute(Intent result) {
        super.onPostExecute(result);

        TaskListener listener = taskListenerRef.get();
        if (listener != null) {
            listener.onTaskFinished(result);
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        TaskListener listener = taskListenerRef.get();
        if (listener != null)
            listener.onProgressUpdate(values[0], values[1]);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();

        TaskListener listener = taskListenerRef.get();
        if (listener != null) {
            listener.onTaskFinished(null);
        }
    }

    private Exception handleException(@NonNull Exception e, @Nullable StoreableWriter writer, @Nullable File dataFile) {
        if (e instanceof InvalidSessionException)
            App.get(context).getAccountManager().invalidateOAuthToken();

        if (e instanceof IOException)
            e = new GeocachingApiException(e.getMessage(), e);

        if (writer == null || dataFile == null || writer.getSize() == 0)
            return e;

        return new IntendedException(e, ActionDisplayPointsExtended.createSendPacksIntent(dataFile, true, true));
    }

    @Override
    protected Intent doInBackground(String... geocacheIds) throws Exception {
        AccountManager accountManager = App.get(context).getAccountManager();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        DataMapper mapper = new DataMapper(context);

        ParcelFile dataFile = new ParcelFile(ActionDisplayPointsExtended.getCacheFileName());
        StoreableWriter writer = null;

        // if it's guid we need to convert to cache code
        if (!ImportActivity.CACHE_CODE_PATTERN.matcher(geocacheIds[0]).find()) {
            WherigoService wherigoService = WherigoApiFactory.create();
            geocacheIds[0] = wherigoService.getCacheCodeFromGuid(geocacheIds[0]);
        }

        int count = geocacheIds.length;
        int current = 0;

        List<String> notFoundCacheIds = new ArrayList<>(0);

        try {
            GeocachingApi api = GeocachingApiFactory.create();
            GeocachingApiLoginTask.create(context, api).perform();

            int logCount = prefs.getInt(PrefConstants.DOWNLOADING_COUNT_OF_LOGS, 5);

            GeocachingApi.ResultQuality resultQuality = GeocachingApi.ResultQuality.FULL;
            if (!accountManager.isPremium()) {
                resultQuality = GeocachingApi.ResultQuality.LITE;
                logCount = 0;
            }

            writer = new StoreableWriter(ActionDisplayPointsExtended.getCacheFileOutputStream());

            int geocachesPerRequest = AppConstants.CACHES_PER_REQUEST;
            while (current < count) {
                long startTime = System.currentTimeMillis();

                String[] requestedCacheIds = getRequestedGeocacheIds(geocacheIds, current, geocachesPerRequest);

                List<Geocache> cachesToAdd = api.searchForGeocaches(
                        resultQuality,
                        geocachesPerRequest,
                        logCount,
                        0,
                        Collections.singletonList(new CacheCodeFilter(requestedCacheIds)),
                        null
                );

                accountManager.getRestrictions().updateLimits(api.getLastGeocacheLimits());

                if (isCancelled())
                    return null;

                addNotFoundCaches(notFoundCacheIds, requestedCacheIds, cachesToAdd);

                if (!cachesToAdd.isEmpty()) {
                    PackWaypoints pw = new PackWaypoints(PACK_WAYPOINTS_NAME);
                    List<Waypoint> waypoints = mapper.createLocusWaypoints(cachesToAdd);

                    for (Waypoint wpt : waypoints) {
                        pw.addWaypoint(wpt);
                    }

                    writer.write(pw);
                }

                current += requestedCacheIds.length;
                publishProgress(current, count);

                long requestDuration = System.currentTimeMillis() - startTime;
                geocachesPerRequest = computeCachesPerRequest(geocachesPerRequest, requestDuration);
            }

            Timber.i("found geocaches: " + current);
            Timber.i("not found geocaches: " + notFoundCacheIds);

            // throw error if some geocache was not found
            if (!notFoundCacheIds.isEmpty()) {
                throw new CacheNotFoundException(notFoundCacheIds.toArray(new String[notFoundCacheIds.size()]));
            }
        } catch (Exception e) {
            throw handleException(e, writer, dataFile);
        } finally {
            Utils.closeStream(writer);
        }

        if (current > 0) {
            try {
                return ActionDisplayPointsExtended.createSendPacksIntent(dataFile, true, true);
            } catch (Throwable t) {
                throw new LocusMapRuntimeException(t);
            }
        } else {
            return null;
        }
    }

    private void addNotFoundCaches(List<String> notFoundCacheIds, final String[] requestedCacheIds, final List<Geocache> cachesToAdd) {
        if (requestedCacheIds.length == cachesToAdd.size()) {
            return;
        }

        String[] foundCacheIds = new String[cachesToAdd.size()];
        for (int i = 0; i < cachesToAdd.size(); i++) {
            foundCacheIds[i] = cachesToAdd.get(i).code();
        }

        for (String cacheId : requestedCacheIds) {
            if (!ArrayUtils.contains(foundCacheIds, cacheId)) {
                notFoundCacheIds.add(cacheId);
            }
        }
    }

    private String[] getRequestedGeocacheIds(String[] cacheIds, int current, int cachesPerRequest) {
        int count = Math.min(cacheIds.length - current, cachesPerRequest);

        return Arrays.copyOfRange(cacheIds, current, current + count);
    }

    @Override
    protected void onException(Throwable t) {
        super.onException(t);

        if (isCancelled())
            return;

        Timber.e(t, t.getMessage());

        Intent intent = new ExceptionHandler(context).handle(t);

        TaskListener listener = taskListenerRef.get();
        if (listener != null)
            listener.onTaskError(intent);
    }

    private int computeCachesPerRequest(int currentCachesPerRequest, long requestDuration) {
        int cachesPerRequest = currentCachesPerRequest;

        // keep the request time between ADAPTIVE_DOWNLOADING_MIN_TIME_MS and ADAPTIVE_DOWNLOADING_MAX_TIME_MS
        if (requestDuration < AppConstants.ADAPTIVE_DOWNLOADING_MIN_TIME_MS)
            cachesPerRequest += AppConstants.ADAPTIVE_DOWNLOADING_STEP;

        if (requestDuration > AppConstants.ADAPTIVE_DOWNLOADING_MAX_TIME_MS)
            cachesPerRequest -= AppConstants.ADAPTIVE_DOWNLOADING_STEP;

        // keep the value in a range
        cachesPerRequest = Math.max(cachesPerRequest, AppConstants.ADAPTIVE_DOWNLOADING_MIN_CACHES);
        cachesPerRequest = Math.min(cachesPerRequest, AppConstants.ADAPTIVE_DOWNLOADING_MAX_CACHES);

        return cachesPerRequest;
    }

}
