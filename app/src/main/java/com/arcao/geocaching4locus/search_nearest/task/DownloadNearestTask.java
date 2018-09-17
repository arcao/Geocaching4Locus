package com.arcao.geocaching4locus.search_nearest.task;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.arcao.geocaching.api.GeocachingApi;
import com.arcao.geocaching.api.GeocachingApiFactory;
import com.arcao.geocaching.api.data.Geocache;
import com.arcao.geocaching.api.data.SearchForGeocachesRequest;
import com.arcao.geocaching.api.data.coordinates.Coordinates;
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
import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.authentication.task.GeocachingApiLoginTask;
import com.arcao.geocaching4locus.authentication.util.Account;
import com.arcao.geocaching4locus.authentication.util.AccountManager;
import com.arcao.geocaching4locus.base.constants.AppConstants;
import com.arcao.geocaching4locus.base.constants.PrefConstants;
import com.arcao.geocaching4locus.base.task.UserTask;
import com.arcao.geocaching4locus.base.util.DownloadingUtil;
import com.arcao.geocaching4locus.base.util.PreferenceUtil;
import com.arcao.geocaching4locus.error.exception.IntendedException;
import com.arcao.geocaching4locus.error.exception.LocusMapRuntimeException;
import com.arcao.geocaching4locus.error.exception.NoResultFoundException;
import com.arcao.geocaching4locus.error.handler.ExceptionHandler;
import com.arcao.geocaching4locus.search_nearest.parcel.ParcelFile;
import com.arcao.geocaching4locus.update.UpdateActivity;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import androidx.annotation.NonNull;
import locus.api.android.ActionDisplayPointsExtended;
import locus.api.android.objects.PackWaypoints;
import locus.api.mapper.DataMapper;
import locus.api.objects.extra.Waypoint;
import locus.api.utils.StoreableWriter;
import timber.log.Timber;

import static com.arcao.geocaching.api.GeocachingApi.ResultQuality.FULL;
import static com.arcao.geocaching.api.GeocachingApi.ResultQuality.LITE;

public class DownloadNearestTask extends UserTask<Void, Integer, Intent> {
    private static final String PACK_WAYPOINTS_NAME = DownloadNearestTask.class.getName();

    private final Context context;
    private final SharedPreferences preferences;
    private final WeakReference<TaskListener> taskListenerRef;
    private final Coordinates coordinates;
    private final int count;
    private final double distance;
    private final AccountManager accountManager;

    public interface TaskListener {
        void onTaskFinish(Intent intent);

        void onTaskError(@NonNull Intent intent);

        void onProgressUpdate(int current, int count);
    }

    public DownloadNearestTask(Context context, TaskListener listener, double latitude, double longitude, int count) {
        this.context = context.getApplicationContext();
        taskListenerRef = new WeakReference<>(listener);
        coordinates = Coordinates.create(latitude, longitude);
        this.count = count;

        preferences = PreferenceManager.getDefaultSharedPreferences(this.context);
        accountManager = App.get(context).getAccountManager();
        distance = getDistance();
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
        if (listener != null) listener.onProgressUpdate(values[0], count);
    }


    @Override
    protected Intent doInBackground(Void... params) throws Exception {
        Timber.i("source=search;coordinates=" + coordinates + ";count=" + count);

        DataMapper mapper = new DataMapper(context);
        ParcelFile dataFile = new ParcelFile(ActionDisplayPointsExtended.getCacheFileName());

        int current = 0;

        try (StoreableWriter writer = new StoreableWriter(ActionDisplayPointsExtended.getCacheFileOutputStream())) {

            GeocachingApi api = GeocachingApiFactory.create();
            GeocachingApiLoginTask.create(context, api).perform();

            boolean simpleCacheData = preferences.getBoolean(PrefConstants.DOWNLOADING_SIMPLE_CACHE_DATA, false);

            SearchForGeocachesRequest request = SearchForGeocachesRequest.builder()
                    .resultQuality(accountManager.isPremium() && !simpleCacheData ? FULL : LITE)
                    .addFilters(createFilters())
                    .geocacheLogCount(simpleCacheData ? 0 : preferences.getInt(PrefConstants.DOWNLOADING_COUNT_OF_LOGS, 5))
                    .maxPerPage(Math.min(AppConstants.ITEMS_PER_REQUEST, count))
                    .build();

            publishProgress(current);

            int itemsPerRequest = AppConstants.ITEMS_PER_REQUEST;
            while (current < count) {
                long startTimeMillis = System.currentTimeMillis();

                List<Geocache> geocacheList;

                if (current == 0) {
                    geocacheList = api.searchForGeocaches(request);
                } else {
                    geocacheList = api.getMoreGeocaches(request.resultQuality(), current, Math.min(itemsPerRequest, count - current), request.geocacheLogCount(), request.trackableLogCount());
                }

                accountManager.getRestrictions().updateLimits(api.getLastGeocacheLimits());

                if (isCancelled())
                    return null;

                if (geocacheList.isEmpty())
                    break;

                // FIX for not working distance filter
                if (computeDistance(coordinates, geocacheList.get(geocacheList.size() - 1)) > distance) {
                    removeCachesOverDistance(geocacheList, coordinates, distance);

                    if (geocacheList.isEmpty())
                        break;
                }

                PackWaypoints pw = new PackWaypoints(PACK_WAYPOINTS_NAME);
                for (Waypoint wpt : mapper.createLocusWaypoints(geocacheList)) {
                    if (simpleCacheData) {
                        wpt.setExtraOnDisplay(context.getPackageName(), UpdateActivity.class.getName(), UpdateActivity.PARAM_SIMPLE_CACHE_ID, wpt.gcData.getCacheID());
                    }

                    pw.addWaypoint(wpt);
                }
                writer.write(pw);

                current += geocacheList.size();
                publishProgress(current);

                itemsPerRequest = DownloadingUtil.computeItemsPerRequest(itemsPerRequest, startTimeMillis);
            }

            Timber.i("found caches: %d", current);
        } catch (Exception e) {
            throw handleException(e, dataFile, current);
        }

        try {
            if (current > 0)
                return ActionDisplayPointsExtended.createSendPacksIntent(dataFile, true, true);
        } catch (Throwable t) {
            throw new LocusMapRuntimeException(t);
        }
        throw new NoResultFoundException();
    }

    private Exception handleException(@NonNull Exception e, @NonNull File dataFile, int itemsStored) {
        if (e instanceof InvalidSessionException)
            accountManager.invalidateOAuthToken();

        if (e instanceof IOException)
            e = new GeocachingApiException(e.getMessage(), e);

        if (itemsStored == 0)
            return e;

        return new IntendedException(e, ActionDisplayPointsExtended.createSendPacksIntent(dataFile, true, true));
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();

        TaskListener listener = taskListenerRef.get();
        if (listener != null)
            listener.onTaskFinish(null);
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


    private List<Filter> createFilters() {
        List<Filter> filters = new ArrayList<>(9);

        final Account account = accountManager.getAccount();

        String userName = account != null ? account.name() : null;
        boolean premiumMember = account != null && account.premium();

        filters.add(new PointRadiusFilter(coordinates.latitude(), coordinates.longitude(), (long) (distance * 1000)));

        boolean showDisabled = preferences.getBoolean(PrefConstants.FILTER_SHOW_DISABLED, false);
        filters.add(new GeocacheExclusionsFilter(false, showDisabled ? null : true, null, null, null, null));

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
        final int len = GeocacheType.values().length;
        Vector<GeocacheType> filter = new Vector<>(len);

        for (int i = 0; i < len; i++) {
            if (preferences.getBoolean(PrefConstants.FILTER_CACHE_TYPE_PREFIX + i, true)) {
                filter.add(GeocacheType.values()[i]);
            }
        }

        return filter.toArray(new GeocacheType[filter.size()]);
    }

    private ContainerType[] getSelectedContainerTypes() {
        final int len = ContainerType.values().length;
        Vector<ContainerType> filter = new Vector<>(len);

        for (int i = 0; i < len; i++) {
            if (preferences.getBoolean(PrefConstants.FILTER_CONTAINER_TYPE_PREFIX + i, true)) {
                filter.add(ContainerType.values()[i]);
            }
        }

        return filter.toArray(new ContainerType[filter.size()]);
    }

    private void removeCachesOverDistance(@NonNull List<Geocache> caches, @NonNull Coordinates coordinates, double maxDistance) {
        while (!caches.isEmpty()) {
            Geocache cache = caches.get(caches.size() - 1);
            double distance = computeDistance(coordinates, cache);

            if (distance > maxDistance) {
                Timber.i("Cache " + cache.code() + " is over distance.");
                caches.remove(cache);
            } else {
                return;
            }
        }
    }

    private float getDistance() {
        boolean imperialUnits = preferences.getBoolean(PrefConstants.IMPERIAL_UNITS, false);

        double distance = PreferenceUtil.getParsedDouble(preferences, PrefConstants.FILTER_DISTANCE,
                imperialUnits ? AppConstants.DISTANCE_MILES_DEFAULT : AppConstants.DISTANCE_KM_DEFAULT);
        if (imperialUnits) {
            distance *= AppConstants.MILES_PER_KILOMETER;
        }

        // fix for min and max distance error in Geocaching Live API
        return (float) Math.max(Math.min(distance, AppConstants.DISTANCE_KM_MAX), AppConstants.DISTANCE_KM_MIN);
    }

    private double computeDistance(@NonNull Coordinates coordinates, @NonNull Geocache cache) {
        return cache.coordinates().distanceTo(coordinates) / 1000;
    }
}
