package com.arcao.geocaching4locus.update.task;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.arcao.geocaching.api.GeocachingApi;
import com.arcao.geocaching.api.GeocachingApi.ResultQuality;
import com.arcao.geocaching.api.GeocachingApiFactory;
import com.arcao.geocaching.api.data.Geocache;
import com.arcao.geocaching.api.data.SearchForGeocachesRequest;
import com.arcao.geocaching.api.exception.InvalidSessionException;
import com.arcao.geocaching.api.filter.CacheCodeFilter;
import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.authentication.task.GeocachingApiLoginTask;
import com.arcao.geocaching4locus.authentication.util.AccountManager;
import com.arcao.geocaching4locus.base.constants.AppConstants;
import com.arcao.geocaching4locus.base.constants.PrefConstants;
import com.arcao.geocaching4locus.base.task.UserTask;
import com.arcao.geocaching4locus.base.util.DownloadingUtil;
import com.arcao.geocaching4locus.base.util.LocusMapUtil;
import com.arcao.geocaching4locus.error.exception.LocusMapRuntimeException;
import com.arcao.geocaching4locus.error.handler.ExceptionHandler;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import locus.api.android.ActionTools;
import locus.api.android.utils.LocusUtils.LocusVersion;
import locus.api.mapper.DataMapper;
import locus.api.mapper.WaypointMerger;
import locus.api.objects.extra.Waypoint;
import timber.log.Timber;

import static com.arcao.geocaching.api.GeocachingApi.ResultQuality.FULL;
import static com.arcao.geocaching.api.GeocachingApi.ResultQuality.LITE;

public class UpdateMoreTask extends UserTask<long[], Integer, Boolean> {
    public interface TaskListener {
        void onTaskFinished(boolean success);
        void onTaskError(Intent intent);
        void onProgressUpdate(int count);
    }

    private final Context context;
    private final WeakReference<TaskListener> taskListenerRef;
    private final AccountManager accountManager;
    private final SharedPreferences preferences;

    public UpdateMoreTask(Context context, TaskListener listener) {
        this.context = context.getApplicationContext();
        taskListenerRef = new WeakReference<>(listener);

        accountManager = App.get(this.context).getAccountManager();
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);

        TaskListener listener = taskListenerRef.get();
        if (listener != null) listener.onTaskFinished(result);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        TaskListener listener = taskListenerRef.get();
        if (listener != null) listener.onProgressUpdate(values[0]);
    }

    @Override
    protected Boolean doInBackground(long[]... params) throws Exception {
        long[] ids = params[0];
        int current = 0;
        int count = ids.length;

        DataMapper mapper = new DataMapper(this.context);
        WaypointMerger merger = new WaypointMerger(this.context);

        final LocusVersion locusVersion = LocusMapUtil.getLocusVersion(context);

        try {
            GeocachingApi api = GeocachingApiFactory.create();
            GeocachingApiLoginTask.create(context, api).perform();

            final boolean premium = accountManager.isPremium();

            ResultQuality resultQuality = premium ? FULL : LITE;
            int logCount = premium ? preferences.getInt(PrefConstants.DOWNLOADING_COUNT_OF_LOGS, 5) : 0;

            int itemsPerRequest = AppConstants.ITEMS_PER_REQUEST;
            while (current < count) {
                long startTimeMillis = System.currentTimeMillis();

                // prepare old cache data
                List<Waypoint> oldWaypoints = retrieveWaypointsByIds(context, locusVersion, ids, current, itemsPerRequest);

                if (oldWaypoints.isEmpty()) {
                    // all Waypoints are without geocaching data
                    current += Math.min(ids.length - current, itemsPerRequest);
                    publishProgress(current);
                    continue;
                }

                List<Geocache> cachesToAdd = api.searchForGeocaches(SearchForGeocachesRequest.builder()
                        .resultQuality(resultQuality)
                        .addFilter(new CacheCodeFilter(getGeocacheCodes(oldWaypoints)))
                        .geocacheLogCount(logCount)
                        .maxPerPage(itemsPerRequest)
                        .build()
                );

                accountManager.getRestrictions().updateLimits(api.getLastGeocacheLimits());

                if (isCancelled())
                    return false;

                if (cachesToAdd.isEmpty())
                    break;

                for (Waypoint p : mapper.createLocusWaypoints(cachesToAdd)) {
                    // Geocaching API can return caches in a different order
                    Waypoint oldWaypoint = getWaypointByGeocacheCode(oldWaypoints, p.gcData.getCacheID());
                    merger.mergeWaypoint(p, oldWaypoint);

                    // update new point data in Locus
                    ActionTools.updateLocusWaypoint(context, locusVersion, p, false);
                }

                current += Math.min(ids.length - current, itemsPerRequest);
                publishProgress(current);

                itemsPerRequest = DownloadingUtil.computeItemsPerRequest(itemsPerRequest, startTimeMillis);
            }

            Timber.i("updated caches: %d", current);

            publishProgress(current);
            return current > 0;
        } catch (InvalidSessionException e) {
            accountManager.invalidateOAuthToken();
            throw e;
        }
    }

    private Waypoint getWaypointByGeocacheCode(Iterable<Waypoint> waypoints, String geocacheCode) {
        if (TextUtils.isEmpty(geocacheCode))
            return null;

        for (Waypoint oldPoint : waypoints) {
            if (oldPoint.gcData != null && geocacheCode.equals(oldPoint.gcData.getCacheID())) {
                return oldPoint;
            }
        }

        return null;
    }

    private List<Waypoint> retrieveWaypointsByIds(Context context, LocusVersion locusVersion, long[] ids, int currentItem, int itemsPerRequest) {
        try {
            int count = Math.min(ids.length - currentItem, itemsPerRequest);
            List<Waypoint> waypoints = new ArrayList<>(count);

            for (int i = 0; i < count; i++) {
                // get old waypoint from Locus
                Waypoint wpt = ActionTools.getLocusWaypoint(context, locusVersion, ids[currentItem + i]);
                if (LocusMapUtil.isGeocache(wpt)) {
                    Timber.w("Waypoint " + (currentItem + i) + " with id " + ids[currentItem + i] + " isn't geocache. Skipped...");
                    continue;
                }

                waypoints.add(wpt);
            }
            return waypoints;
        } catch (Throwable t) {
            throw new LocusMapRuntimeException(t);
        }
    }

    private String[] getGeocacheCodes(List<Waypoint> waypoints) {
        int count = waypoints.size();
        String[] ret = new String[count];

        for (int i = 0; i < count; i++) {
            ret[i] = waypoints.get(i).gcData.getCacheID();
        }

        return ret;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();

        TaskListener listener = taskListenerRef.get();
        if (listener != null) listener.onTaskFinished(false);
    }

    @Override
    protected void onException(Throwable t) {
        super.onException(t);

        if (isCancelled()) return;

        Intent intent = new ExceptionHandler(context).handle(t);

        TaskListener listener = taskListenerRef.get();
        if (listener != null) listener.onTaskError(intent);
    }
}
