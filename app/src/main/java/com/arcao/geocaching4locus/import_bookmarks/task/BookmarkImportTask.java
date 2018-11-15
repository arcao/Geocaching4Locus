package com.arcao.geocaching4locus.import_bookmarks.task;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.arcao.geocaching.api.GeocachingApi;
import com.arcao.geocaching.api.GeocachingApiFactory;
import com.arcao.geocaching.api.data.Geocache;
import com.arcao.geocaching.api.data.SearchForGeocachesRequest;
import com.arcao.geocaching.api.data.bookmarks.Bookmark;
import com.arcao.geocaching.api.exception.GeocachingApiException;
import com.arcao.geocaching.api.filter.CacheCodeFilter;
import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.authentication.task.GeocachingApiLoginTask;
import com.arcao.geocaching4locus.authentication.util.AccountManager;
import com.arcao.geocaching4locus.base.constants.AppConstants;
import com.arcao.geocaching4locus.base.constants.PrefConstants;
import com.arcao.geocaching4locus.base.task.UserTask;
import com.arcao.geocaching4locus.base.util.DownloadingUtil;
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
import locus.api.android.objects.PackPoints;
import locus.api.mapper.DataMapper;
import locus.api.objects.extra.Point;
import locus.api.utils.StoreableWriter;
import timber.log.Timber;

public class BookmarkImportTask extends UserTask<String, Void, Boolean> {
    public interface TaskListener {
        void onTaskFinish();

        void onTaskError(Intent intent);

        void onProgressUpdate(int count, int max);
    }

    private final Context context;
    private final WeakReference<TaskListener> taskListenerRef;
    private final AccountManager accountManager;
    private final SharedPreferences preferences;

    private int progress;
    private int count;

    public BookmarkImportTask(Context context, TaskListener listener) {
        this.context = context.getApplicationContext();
        taskListenerRef = new WeakReference<>(listener);
        accountManager = App.get(context).getAccountManager();
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    protected Boolean doInBackground(String... params) throws Exception {
        File dataFile = ActionDisplayPointsExtended.getCacheFileName();
        DataMapper mapper = new DataMapper(context);

        GeocachingApi api = GeocachingApiFactory.create();
        GeocachingApiLoginTask.create(context, api).perform();

        boolean simpleCacheData = preferences.getBoolean(PrefConstants.DOWNLOADING_SIMPLE_CACHE_DATA, false);
        int logCount = preferences.getInt(PrefConstants.DOWNLOADING_COUNT_OF_LOGS, 5);

        final List<String> geocacheCodes = new ArrayList<>();
        if (params.length == 1 && isGuid(params[0])) {
            String guid = params[0];

            Timber.d("source: import_from_bookmark;guid=%s", guid);

            // retrieve Bookmark list geocaches
            List<Bookmark> bookmarks = api.getBookmarkListByGuid(guid);

            for (Bookmark bookmark : bookmarks)
                geocacheCodes.add(bookmark.cacheCode());

        } else {
            Collections.addAll(geocacheCodes, params);
        }

        Timber.d("source: import_from_bookmark;gccodes=%s", geocacheCodes);

        count = geocacheCodes.size();
        if (count <= 0)
            throw new NoResultFoundException();

        GeocachingApi.ResultQuality resultQuality = GeocachingApi.ResultQuality.FULL;
        if (simpleCacheData) {
            resultQuality = GeocachingApi.ResultQuality.LITE;
            logCount = 0;
        }

        try (StoreableWriter writer = new StoreableWriter(ActionDisplayPointsExtended.getCacheFileOutputStream())){
            publishProgress();

            progress = 0;
            int itemsPerRequest = AppConstants.ITEMS_PER_REQUEST;
            while (progress < count) {
                long startTimeMillis = System.currentTimeMillis();

                List<String> requestedCaches = geocacheCodes.subList(progress,
                        Math.min(count, progress + itemsPerRequest));

                List<Geocache> cachesToAdd = api.searchForGeocaches(SearchForGeocachesRequest.builder()
                        .resultQuality(resultQuality)
                        .maxPerPage(itemsPerRequest)
                        .geocacheLogCount(logCount)
                        .addFilter(new CacheCodeFilter(requestedCaches.toArray(new String[requestedCaches.size()])))
                        .build()
                );

                if (!simpleCacheData)
                    accountManager.getRestrictions().updateLimits(api.getLastGeocacheLimits());

                if (isCancelled())
                    return false;

                if (cachesToAdd.isEmpty())
                    break;

                PackPoints pack = new PackPoints("BookmarkImport");
                for (Point p : mapper.createLocusPoints(cachesToAdd)) {
                    if (simpleCacheData) {
                        p.setExtraOnDisplay(context.getPackageName(), UpdateActivity.class.getName(), UpdateActivity.PARAM_SIMPLE_CACHE_ID, p.gcData.getCacheID());
                    }

                    pack.addWaypoint(p);
                }
                writer.write(pack);

                progress += cachesToAdd.size();
                publishProgress();

                itemsPerRequest = DownloadingUtil.computeItemsPerRequest(itemsPerRequest, startTimeMillis);
            }

            Timber.i("found caches: %d", progress);

            if (progress > 0) {
                try {
                    ActionDisplayPointsExtended.sendPacksFile(context, dataFile, true, false, Intent.FLAG_ACTIVITY_NEW_TASK);
                    return true;
                } catch (Throwable t) {
                    throw new LocusMapRuntimeException(t);
                }
            } else {
                throw new NoResultFoundException();
            }
        } catch (IOException e) {
            Timber.e(e);
            throw new GeocachingApiException(e.getMessage(), e);
        }
    }

    private boolean isGuid(String value) {
        return value.indexOf('-') >= 0;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);

        TaskListener listener = taskListenerRef.get();
        if (listener != null) listener.onTaskFinish();
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        TaskListener listener = taskListenerRef.get();
        if (listener != null) listener.onProgressUpdate(progress, count);
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
