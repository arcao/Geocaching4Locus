package com.arcao.geocaching4locus.weblink.task;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import com.arcao.geocaching.api.GeocachingApi;
import com.arcao.geocaching.api.GeocachingApiFactory;
import com.arcao.geocaching.api.data.Geocache;
import com.arcao.geocaching.api.exception.InvalidSessionException;
import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.authentication.task.GeocachingApiLoginTask;
import com.arcao.geocaching4locus.authentication.util.AccountManager;
import com.arcao.geocaching4locus.base.task.UserTask;
import com.arcao.geocaching4locus.error.exception.CacheNotFoundException;
import com.arcao.geocaching4locus.error.handler.ExceptionHandler;

import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.lang.ref.WeakReference;

import locus.api.mapper.DataMapper;
import locus.api.objects.extra.Waypoint;
import timber.log.Timber;

public class RefreshWebLinkTask extends
        UserTask<String, Void, RefreshWebLinkTask.ParcelableWaypoint> {
    public interface TaskListener {
        void onTaskFinished(Waypoint waypoint);
    }

    private final Context context;
    private final WeakReference<TaskListener> taskListenerRef;

    public RefreshWebLinkTask(Context context, TaskListener listener) {
        this.taskListenerRef = new WeakReference<>(listener);
        this.context = context.getApplicationContext();
    }

    @Override
    protected void onPostExecute(ParcelableWaypoint result) {
        super.onPostExecute(result);

        TaskListener listener = taskListenerRef.get();
        if (listener != null) {
            listener.onTaskFinished(result.waypoint);
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
    protected ParcelableWaypoint doInBackground(String... params) throws Exception {
        AccountManager accountManager = App.get(context).getAccountManager();
        DataMapper mapper = new DataMapper(context);

        String cacheId = params[0];

        GeocachingApi api = GeocachingApiFactory.create();
        GeocachingApiLoginTask.create(context, api).perform();

        try {
            Geocache cache = api.getGeocache(GeocachingApi.ResultQuality.LITE, cacheId, 0, 0);

            accountManager.getRestrictions().updateLimits(api.getLastGeocacheLimits());

            if (isCancelled())
                return null;

            if (cache == null)
                throw new CacheNotFoundException(cacheId);

            return new ParcelableWaypoint(mapper.createLocusWaypoint(cache));
        } catch (InvalidSessionException e) {
            Timber.e(e, e.getMessage());
            accountManager.invalidateOAuthToken();

            throw e;
        }
    }

    @Override
    protected void onException(Throwable t) {
        super.onException(t);

        if (isCancelled())
            return;

        Timber.e(t, t.getMessage());

        Intent intent = new ExceptionHandler(context).handle(t);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY
                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                | Intent.FLAG_ACTIVITY_NEW_TASK);

        TaskListener listener = taskListenerRef.get();
        if (listener != null) {
            listener.onTaskFinished(null);
        }

        context.startActivity(intent);
    }

    static final class ParcelableWaypoint implements Parcelable {
        public Waypoint waypoint;

        ParcelableWaypoint(Parcel in) {
            try {
                byte[] data = in.createByteArray();

                if (ArrayUtils.isNotEmpty(data))
                    waypoint = new Waypoint(data);
            } catch (IOException e) {
                Timber.e(e, e.getMessage());
            }
        }

        ParcelableWaypoint(Waypoint waypoint) {
            this.waypoint = waypoint;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int i) {
            dest.writeByteArray(waypoint != null ? waypoint.getAsBytes() : null);
        }

        public static final Creator<ParcelableWaypoint> CREATOR = new Creator<ParcelableWaypoint>() {
            @Override
            public ParcelableWaypoint createFromParcel(Parcel in) {
                return new ParcelableWaypoint(in);
            }

            @Override
            public ParcelableWaypoint[] newArray(int size) {
                return new ParcelableWaypoint[size];
            }
        };
    }
}
