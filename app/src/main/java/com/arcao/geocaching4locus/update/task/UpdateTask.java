package com.arcao.geocaching4locus.update.task;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;

import com.arcao.geocaching.api.GeocachingApi;
import com.arcao.geocaching.api.GeocachingApiFactory;
import com.arcao.geocaching.api.data.Geocache;
import com.arcao.geocaching.api.data.GeocacheLog;
import com.arcao.geocaching.api.data.Trackable;
import com.arcao.geocaching.api.exception.InvalidSessionException;
import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.authentication.task.GeocachingApiLoginTask;
import com.arcao.geocaching4locus.authentication.util.AccountManager;
import com.arcao.geocaching4locus.base.constants.AppConstants;
import com.arcao.geocaching4locus.base.constants.PrefConstants;
import com.arcao.geocaching4locus.base.task.UserTask;
import com.arcao.geocaching4locus.error.exception.CacheNotFoundException;
import com.arcao.geocaching4locus.error.exception.LocusMapRuntimeException;
import com.arcao.geocaching4locus.error.handler.ExceptionHandler;
import com.arcao.geocaching4locus.update.task.UpdateTask.UpdateTaskData;

import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

import locus.api.android.ActionTools;
import locus.api.android.utils.LocusUtils;
import locus.api.mapper.DataMapper;
import locus.api.mapper.WaypointMerger;
import locus.api.objects.extra.Waypoint;
import timber.log.Timber;

import static locus.api.mapper.Util.applyUnavailabilityForGeocache;

public class UpdateTask extends UserTask<UpdateTaskData, Integer, UpdateTaskData> {

    public interface TaskListener {
        enum State {
            CACHE,
            LOGS
        }

        void onUpdateState(State state, int progress, int max);
        void onTaskFinished(Intent result);
        void onTaskError(Intent intent);
    }


    private final WeakReference<TaskListener> taskListenerRef;
    private final Context context;
    private final DataMapper mapper;
    private final WaypointMerger merger;

    public UpdateTask(Context context, TaskListener listener) {
        this.context = context.getApplicationContext();
        taskListenerRef = new WeakReference<>(listener);
        mapper = new DataMapper(this.context);
        merger = new WaypointMerger(this.context);
    }

    @Override
    protected void onPostExecute(UpdateTaskData result) {
        super.onPostExecute(result);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean replaceCache = PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW__UPDATE_ONCE.equals(preferences.getString(PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW, PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW__UPDATE_ONCE));
        boolean downloadLogsUpdateCache = preferences.getBoolean(PrefConstants.DOWNLOAD_LOGS_UPDATE_CACHE, true);
        boolean disableDnfNmNaGeocaches = preferences.getBoolean(PrefConstants.DOWNLOADING_DISABLE_DNF_NM_NA_CACHES, false);
        int disableDnfNmNaGeocachesThreshold = preferences.getInt(PrefConstants.DOWNLOADING_DISABLE_DNF_NM_NA_CACHES_LOGS_COUNT, 1);

        LocusUtils.LocusVersion locusVersion;
        try {
            locusVersion = LocusUtils.getActiveVersion(context);
            if (locusVersion == null) {
                throw new IllegalStateException("Locus is not installed.");
            }
        } catch (Throwable t) {
            throw new LocusMapRuntimeException(t);
        }

        if (result == null || result.newPoint == null) {
            TaskListener listener = taskListenerRef.get();
            if (listener != null) {
                listener.onTaskFinished(null);
            }
            return;
        }

        if (result.updateLogs && !downloadLogsUpdateCache) {
            merger.mergeGeocachingLogs(result.oldPoint, result.newPoint);

            // only when this feature is enabled
            if (disableDnfNmNaGeocaches)
                applyUnavailabilityForGeocache(result.oldPoint, disableDnfNmNaGeocachesThreshold);

            result.newPoint = result.oldPoint;
        } else {
            merger.mergeWaypoint(result.newPoint, result.oldPoint);

            if (replaceCache) {
                result.newPoint.removeExtraOnDisplay();
            }
        }

        // if Waypoint is already in DB we must update it manually
        if (result.oldPoint != null) {
            try {
                ActionTools.updateLocusWaypoint(context, locusVersion, result.newPoint, false);
            } catch (Throwable t) {
                throw new LocusMapRuntimeException(t);
            }
        }

        TaskListener listener = taskListenerRef.get();
        if (listener != null) {
            listener.onTaskFinished(LocusUtils.prepareResultExtraOnDisplayIntent(result.newPoint, replaceCache));
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);

        TaskListener listener = taskListenerRef.get();
        if (listener != null) {
            if (values == null || values.length != 2) {
                listener.onUpdateState(TaskListener.State.CACHE, 0, 0);
            } else {
                listener.onUpdateState(TaskListener.State.LOGS, values[0], values[1]);
            }
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();

        TaskListener listener = taskListenerRef.get();
        if (listener != null) {
            listener.onTaskFinished(null);
        }
    }

    @Override
    protected UpdateTaskData doInBackground(UpdateTaskData... params) throws Exception {
        AccountManager accountManager = App.get(context).getAccountManager();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        UpdateTaskData result = params[0];
        try {
            publishProgress();

            GeocachingApi api = GeocachingApiFactory.create();
            GeocachingApiLoginTask.create(context, api).perform();

            int logCount = prefs.getInt(PrefConstants.DOWNLOADING_COUNT_OF_LOGS, 5);
            int originalLogCount = logCount;

            GeocachingApi.ResultQuality resultQuality = GeocachingApi.ResultQuality.FULL;
            boolean basicMember = !accountManager.isPremium();
            if (basicMember) {
                resultQuality = GeocachingApi.ResultQuality.LITE;
                logCount = 0;
            }

            Geocache cache = api.getGeocache(resultQuality, result.cacheId, logCount, 0);
            accountManager.getRestrictions().updateLimits(api.getLastGeocacheLimits());

            if (cache == null)
                throw new CacheNotFoundException(result.cacheId);

            if (isCancelled())
                return null;

            result.newPoint = mapper.createLocusWaypoint(cache);

            if (basicMember) {
                // add trackables
                List<Trackable> trackables = api.getTrackablesByCacheCode(result.cacheId, 0, 30, 0);
                mapper.addTrackables(result.newPoint, trackables);

                // TODO images
            }

            if (result.updateLogs || basicMember) {
                int startIndex = logCount;
                int maxLogs = AppConstants.LOGS_TO_UPDATE_MAX - logCount;

                if (!result.updateLogs) {
                    maxLogs = originalLogCount;
                }

                while (startIndex < maxLogs) {
                    publishProgress(startIndex, maxLogs);

                    int logsPerRequest = Math.min(maxLogs - startIndex, AppConstants.LOGS_PER_REQUEST);
                    List<GeocacheLog> retrievedLogs = api.getGeocacheLogsByCacheCode(result.cacheId, startIndex, logsPerRequest);

                    if (retrievedLogs.isEmpty()) {
                        break;
                    }

                    mapper.addCacheLogs(result.newPoint, retrievedLogs);

                    startIndex += retrievedLogs.size();
                }
                publishProgress(maxLogs, maxLogs);
            }

            if (isCancelled())
                return null;

            return result;
        } catch (InvalidSessionException e) {
            accountManager.invalidateOAuthToken();

            throw e;
        }
    }

    @Override
    protected void onException(Throwable t) {
        super.onException(t);

        if (isCancelled())
            return;

        Intent intent = new ExceptionHandler(context).handle(t);

        TaskListener listener = taskListenerRef.get();
        if (listener != null)             listener.onTaskError(intent);
    }

    public static class UpdateTaskData implements Parcelable {
        final String cacheId;
        Waypoint oldPoint;
        Waypoint newPoint;
        final boolean updateLogs;

        public UpdateTaskData(String cacheId, Waypoint waypoint, boolean updateLogs) {
            this.cacheId = cacheId;
            this.oldPoint = waypoint;
            this.updateLogs = updateLogs;
        }

        UpdateTaskData(Parcel in) {
            cacheId = in.readString();

            try {
                byte[] data = in.createByteArray();
                if (ArrayUtils.isNotEmpty(data))
                    oldPoint = new Waypoint(data);
            } catch (IOException e) {
                Timber.e(e);
            }

            try {
                byte[] data = in.createByteArray();
                if (ArrayUtils.isNotEmpty(data))
                    newPoint = new Waypoint(data);
            } catch (IOException e) {
                Timber.e(e);
            }

            updateLogs = in.readInt() == 1;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(cacheId);
            dest.writeByteArray(oldPoint != null ? oldPoint.getAsBytes() : null);
            dest.writeByteArray(newPoint != null ? newPoint.getAsBytes() : null);
            dest.writeInt(updateLogs ? 1 : 0);
        }

        public static final Creator<UpdateTaskData> CREATOR = new Creator<UpdateTaskData>() {
            @Override
            public UpdateTaskData createFromParcel(Parcel source) {
                return new UpdateTaskData(source);
            }

            @Override
            public UpdateTaskData[] newArray(int size) {
                return new UpdateTaskData[0];
            }
        };
    }
}
