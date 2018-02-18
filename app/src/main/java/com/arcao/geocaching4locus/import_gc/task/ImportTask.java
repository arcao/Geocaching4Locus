package com.arcao.geocaching4locus.import_gc.task;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.arcao.geocaching.api.GeocachingApi;
import com.arcao.geocaching.api.GeocachingApi.ResultQuality;
import com.arcao.geocaching.api.GeocachingApiFactory;
import com.arcao.geocaching.api.data.Geocache;
import com.arcao.geocaching.api.data.SearchForGeocachesRequest;
import com.arcao.geocaching.api.exception.GeocachingApiException;
import com.arcao.geocaching.api.exception.InvalidSessionException;
import com.arcao.geocaching.api.filter.CacheCodeFilter;
import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.authentication.task.GeocachingApiLoginTask;
import com.arcao.geocaching4locus.authentication.util.AccountManager;
import com.arcao.geocaching4locus.base.constants.AppConstants;
import com.arcao.geocaching4locus.base.constants.PrefConstants;
import com.arcao.geocaching4locus.base.task.UserTask;
import com.arcao.geocaching4locus.base.util.DownloadingUtil;
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
import java.util.List;

import locus.api.android.ActionDisplayPointsExtended;
import locus.api.android.objects.PackWaypoints;
import locus.api.mapper.DataMapper;
import locus.api.objects.extra.Waypoint;
import locus.api.utils.StoreableWriter;
import timber.log.Timber;

public class ImportTask extends UserTask<String, Integer, Intent> {
    private static final String PACK_WAYPOINTS_NAME = "IMPORT";
    private final AccountManager accountManager;
    private final SharedPreferences preferences;

    public interface TaskListener {
        void onTaskFinish(@Nullable Intent intent);

        void onTaskError(@NonNull Intent intent);

        void onProgressUpdate(int current);
    }

    private final Context context;
    private final WeakReference<TaskListener> taskListenerRef;

    public ImportTask(Context context, TaskListener listener) {
        this.taskListenerRef = new WeakReference<>(listener);
        this.context = context.getApplicationContext();

        accountManager = App.get(context).getAccountManager();
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    protected void onPostExecute(Intent result) {
        super.onPostExecute(result);

        TaskListener listener = taskListenerRef.get();
        if (listener != null) listener.onTaskFinish(result);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        TaskListener listener = taskListenerRef.get();
        if (listener != null) listener.onProgressUpdate(values[0]);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();

        TaskListener listener = taskListenerRef.get();
        if (listener != null) listener.onTaskFinish(null);
    }

    private Exception handleException(@NonNull Exception e, @NonNull File dataFile, int count) {
        if (e instanceof InvalidSessionException)
            App.get(context).getAccountManager().invalidateOAuthToken();

        if (e instanceof IOException)
            e = new GeocachingApiException(e.getMessage(), e);

        if (count == 0) return e;

        return new IntendedException(e, ActionDisplayPointsExtended.createSendPacksIntent(dataFile, true, true));
    }

    @Override
    protected Intent doInBackground(String... geocacheCodes) throws Exception {
        DataMapper mapper = new DataMapper(context);
        ParcelFile dataFile = new ParcelFile(ActionDisplayPointsExtended.getCacheFileName());

        // if it's guid we need to convert to cache code
        if (!ImportActivity.CACHE_CODE_PATTERN.matcher(geocacheCodes[0]).find()) {
            WherigoService wherigoService = WherigoApiFactory.create();
            geocacheCodes[0] = wherigoService.getCacheCodeFromGuid(geocacheCodes[0]);
        }

        int count = geocacheCodes.length;
        int current = 0;

        List<String> notFoundGeocacheCodes = new ArrayList<>(0);

        try (StoreableWriter writer = new StoreableWriter(ActionDisplayPointsExtended.getCacheFileOutputStream())) {
            GeocachingApi api = GeocachingApiFactory.create();
            GeocachingApiLoginTask.create(context, api).perform();

            int logCount = preferences.getInt(PrefConstants.DOWNLOADING_COUNT_OF_LOGS, 5);

            ResultQuality resultQuality = ResultQuality.FULL;
            if (!accountManager.isPremium()) {
                resultQuality = ResultQuality.LITE;
                logCount = 0;
            }

            int itemsPerRequest = AppConstants.ITEMS_PER_REQUEST;
            while (current < count) {
                long startTimeMillis = System.currentTimeMillis();

                String[] requestedCacheIds = getRequestedGeocacheIds(geocacheCodes, current, itemsPerRequest);

                List<Geocache> cachesToAdd = api.searchForGeocaches(SearchForGeocachesRequest.builder()
                        .resultQuality(resultQuality)
                        .maxPerPage(itemsPerRequest)
                        .geocacheLogCount(logCount)
                        .addFilter(new CacheCodeFilter(requestedCacheIds))
                        .build()
                );

                accountManager.getRestrictions().updateLimits(api.getLastGeocacheLimits());

                if (isCancelled())
                    return null;

                addNotFoundCaches(notFoundGeocacheCodes, requestedCacheIds, cachesToAdd);

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

                itemsPerRequest = DownloadingUtil.computeItemsPerRequest(itemsPerRequest, startTimeMillis);
            }

            Timber.i("found geocaches: %d", current);
            Timber.i("not found geocaches: %s", notFoundGeocacheCodes);

            // throw error if some geocache was not found
            if (!notFoundGeocacheCodes.isEmpty()) {
                throw new CacheNotFoundException(notFoundGeocacheCodes.toArray(new String[notFoundGeocacheCodes.size()]));
            }

            try {
                return ActionDisplayPointsExtended.createSendPacksIntent(dataFile, true, true);
            } catch (Throwable t) {
                throw new LocusMapRuntimeException(t);
            }
        } catch (Exception e) {
            throw handleException(e, dataFile, current);
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

        Intent intent = new ExceptionHandler(context).handle(t);

        TaskListener listener = taskListenerRef.get();
        if (listener != null) listener.onTaskError(intent);
    }
}
