package com.arcao.geocaching4locus.download_rectangle.task;

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
import com.arcao.geocaching.api.data.type.ContainerType;
import com.arcao.geocaching.api.data.type.GeocacheType;
import com.arcao.geocaching.api.exception.GeocachingApiException;
import com.arcao.geocaching.api.exception.InvalidSessionException;
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
import com.arcao.geocaching4locus.authentication.task.GeocachingApiLoginTask;
import com.arcao.geocaching4locus.authentication.util.AccountManager;
import com.arcao.geocaching4locus.base.constants.AppConstants;
import com.arcao.geocaching4locus.base.task.UserTask;
import com.arcao.geocaching4locus.base.util.PreferenceUtil;
import com.arcao.geocaching4locus.error.exception.IntendedException;
import com.arcao.geocaching4locus.error.exception.LocusMapRuntimeException;
import com.arcao.geocaching4locus.error.exception.NoResultFoundException;
import com.arcao.geocaching4locus.error.handler.ExceptionHandler;
import com.arcao.geocaching4locus.live_map.model.LastLiveMapData;
import com.arcao.geocaching4locus.search_nearest.parcel.ParcelFile;
import com.arcao.geocaching4locus.update.UpdateActivity;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import locus.api.android.ActionDisplayPointsExtended;
import locus.api.android.objects.PackWaypoints;
import locus.api.mapper.DataMapper;
import locus.api.objects.extra.Waypoint;
import locus.api.utils.StoreableWriter;
import locus.api.utils.Utils;
import timber.log.Timber;

import static com.arcao.geocaching4locus.base.constants.AppConstants.LIVEMAP_CACHES_COUNT;
import static com.arcao.geocaching4locus.base.constants.AppConstants.LIVEMAP_DISTANCE;
import static com.arcao.geocaching4locus.base.constants.PrefConstants.DOWNLOADING_COUNT_OF_LOGS;
import static com.arcao.geocaching4locus.base.constants.PrefConstants.DOWNLOADING_SIMPLE_CACHE_DATA;
import static com.arcao.geocaching4locus.base.constants.PrefConstants.FILTER_CACHE_TYPE_PREFIX;
import static com.arcao.geocaching4locus.base.constants.PrefConstants.FILTER_CONTAINER_TYPE_PREFIX;
import static com.arcao.geocaching4locus.base.constants.PrefConstants.FILTER_DIFFICULTY_MAX;
import static com.arcao.geocaching4locus.base.constants.PrefConstants.FILTER_DIFFICULTY_MIN;
import static com.arcao.geocaching4locus.base.constants.PrefConstants.FILTER_SHOW_DISABLED;
import static com.arcao.geocaching4locus.base.constants.PrefConstants.FILTER_SHOW_FOUND;
import static com.arcao.geocaching4locus.base.constants.PrefConstants.FILTER_SHOW_OWN;
import static com.arcao.geocaching4locus.base.constants.PrefConstants.FILTER_TERRAIN_MAX;
import static com.arcao.geocaching4locus.base.constants.PrefConstants.FILTER_TERRAIN_MIN;

public class DownloadRectangleTask extends UserTask<Void, Integer, Intent> {
    private static final String PACK_WAYPOINTS_NAME = DownloadRectangleTask.class.getName();

    private final Context context;
    private final SharedPreferences preferences;
    private final WeakReference<TaskListener> taskListenerRef;

    public interface TaskListener {
        void onTaskFinished(Intent intent);

        void onTaskError(@NonNull Intent errorIntent);

        void onProgressUpdate(int current, int count);
    }

    public DownloadRectangleTask(Context context, TaskListener listener) {
        this.context = context.getApplicationContext();
        taskListenerRef = new WeakReference<>(listener);

        preferences = PreferenceManager.getDefaultSharedPreferences(this.context);
    }

    @Override
    protected void onPostExecute(Intent result) {
        super.onPostExecute(result);

        TaskListener listener = taskListenerRef.get();
        if (listener != null)
            listener.onTaskFinished(result);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        TaskListener listener = taskListenerRef.get();
        if (listener != null)
            listener.onProgressUpdate(values[0], values[1]);
    }


    @Override
    protected Intent doInBackground(Void... params) throws Exception {
        LastLiveMapData liveMapData = LastLiveMapData.getInstance();

        Timber.i("source=download_rectangle;center=%s;topLeft=%s;bottomRight=%s",
                liveMapData.getMapCenterCoordinates().toString(), liveMapData.getMapTopLeftCoordinates().toString(), liveMapData.getMapBottomRightCoordinates().toString());

        AccountManager accountManager = App.get(context).getAccountManager();
        DataMapper mapper = new DataMapper(context);
        ParcelFile dataFile = new ParcelFile(ActionDisplayPointsExtended.getCacheFileName());

        StoreableWriter writer = null;
        int current = 0;

        try {
            GeocachingApi api = GeocachingApiFactory.create();
            GeocachingApiLoginTask.create(context, api).perform();

            ResultQuality resultQuality = accountManager.isPremium() ? ResultQuality.FULL : ResultQuality.LITE;
            int logCount = preferences.getInt(DOWNLOADING_COUNT_OF_LOGS, 5);

            boolean simpleCacheData = preferences.getBoolean(DOWNLOADING_SIMPLE_CACHE_DATA, false);
            if (simpleCacheData) {
                resultQuality = ResultQuality.LITE;
                logCount = 0;
            }

            writer = new StoreableWriter(ActionDisplayPointsExtended.getCacheFileOutputStream());

            int cachesPerRequest = AppConstants.CACHES_PER_REQUEST;
            int count = AppConstants.CACHES_PER_REQUEST;

            while (current < count) {
                long startTime = System.currentTimeMillis();

                List<Geocache> cachesToAdd;

                if (current == 0) {
                    cachesToAdd = api.searchForGeocaches(resultQuality, Math.min(cachesPerRequest, count - current), logCount, 0, createFilters(), null);
                    count = Math.min(api.getLastSearchResultsFound(), LIVEMAP_CACHES_COUNT);
                } else {
                    cachesToAdd = api.getMoreGeocaches(resultQuality, current, Math.min(cachesPerRequest, count - current), logCount, 0);
                }

                accountManager.getRestrictions().updateLimits(api.getLastGeocacheLimits());

                if (isCancelled())
                    return null;

                if (cachesToAdd.isEmpty())
                    break;

                PackWaypoints pw = new PackWaypoints(PACK_WAYPOINTS_NAME);
                List<Waypoint> waypoints = mapper.createLocusWaypoints(cachesToAdd);

                for (Waypoint wpt : waypoints) {
                    if (simpleCacheData) {
                        wpt.setExtraOnDisplay(context.getPackageName(), UpdateActivity.class.getName(), UpdateActivity.PARAM_SIMPLE_CACHE_ID, wpt.gcData.getCacheID());
                    }

                    pw.addWaypoint(wpt);
                }

                writer.write(pw);

                current += cachesToAdd.size();
                publishProgress(current, count);

                long requestDuration = System.currentTimeMillis() - startTime;
                cachesPerRequest = computeCachesPerRequest(cachesPerRequest, requestDuration);
            }

            Timber.i("found caches: %s", current);
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
            throw new NoResultFoundException();
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
    protected void onCancelled() {
        super.onCancelled();

        TaskListener listener = taskListenerRef.get();
        if (listener != null)
            listener.onTaskFinished(null);
    }

    @Override
    protected void onException(Throwable t) {
        super.onException(t);

        if (isCancelled())
            return;

        Timber.e(t);

        Intent intent = new ExceptionHandler(context).handle(t);

        TaskListener listener = taskListenerRef.get();
        if (listener != null)
            listener.onTaskError(intent);
    }


    private List<Filter> createFilters() {
        List<Filter> filters = new ArrayList<>(9);

        AccountManager accountManager = App.get(context).getAccountManager();
        //noinspection ConstantConditions
        String userName = accountManager.getAccount().name();
        boolean premiumMember = accountManager.isPremium();

        LastLiveMapData liveMapData = LastLiveMapData.getInstance();

        filters.add(new PointRadiusFilter(liveMapData.getMapCenterCoordinates(), LIVEMAP_DISTANCE));
        filters.add(new ViewportFilter(liveMapData.getMapTopLeftCoordinates(), liveMapData.getMapBottomRightCoordinates()));

        boolean showDisabled = preferences.getBoolean(FILTER_SHOW_DISABLED, false);
        filters.add(new GeocacheExclusionsFilter(false, showDisabled ? null : true, null, null, null, null));

        boolean showFound = preferences.getBoolean(FILTER_SHOW_FOUND, false);
        if (!showFound) {
            filters.add(new NotFoundByUsersFilter(userName));
        }

        boolean showOwn = preferences.getBoolean(FILTER_SHOW_OWN, false);
        if (!showOwn) {
            filters.add(new NotHiddenByUsersFilter(userName));
        }

        if (premiumMember) {
            filters.add(new GeocacheTypeFilter(getSelectedGeocacheTypes()));
            filters.add(new GeocacheContainerSizeFilter(getSelectedContainerTypes()));

            float difficultyMin = PreferenceUtil.getParsedFloat(preferences, FILTER_DIFFICULTY_MIN, 1);
            float difficultyMax = PreferenceUtil.getParsedFloat(preferences, FILTER_DIFFICULTY_MAX, 5);
            if (difficultyMin > 1 || difficultyMax < 5) {
                filters.add(new DifficultyFilter(difficultyMin, difficultyMax));
            }

            float terrainMin = PreferenceUtil.getParsedFloat(preferences, FILTER_TERRAIN_MIN, 1);
            float terrainMax = PreferenceUtil.getParsedFloat(preferences, FILTER_TERRAIN_MAX, 5);
            if (terrainMin > 1 || terrainMax < 5) {
                filters.add(new TerrainFilter(terrainMin, terrainMax));
            }

            // TODO: 3. 9. 2015 Move it to configuration
            filters.add(new BookmarksExcludeFilter(true));
        }

        return filters;
    }

    private GeocacheType[] getSelectedGeocacheTypes() {
        final int len = GeocacheType.values().length;
        Vector<GeocacheType> filter = new Vector<>(len);

        for (int i = 0; i < len; i++) {
            if (preferences.getBoolean(FILTER_CACHE_TYPE_PREFIX + i, true)) {
                filter.add(GeocacheType.values()[i]);
            }
        }

        return filter.toArray(new GeocacheType[filter.size()]);
    }

    private ContainerType[] getSelectedContainerTypes() {
        final int len = ContainerType.values().length;
        Vector<ContainerType> filter = new Vector<>(len);

        for (int i = 0; i < len; i++) {
            if (preferences.getBoolean(FILTER_CONTAINER_TYPE_PREFIX + i, true)) {
                filter.add(ContainerType.values()[i]);
            }
        }

        return filter.toArray(new ContainerType[filter.size()]);
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
