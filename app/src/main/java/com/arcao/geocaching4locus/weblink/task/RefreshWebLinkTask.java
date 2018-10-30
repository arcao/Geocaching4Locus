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
import locus.api.objects.extra.Point;
import timber.log.Timber;

public class RefreshWebLinkTask extends UserTask<String, Void, RefreshWebLinkTask.ParcelablePoint> {

    public interface TaskListener {
        void onTaskFinish(Point point);

        void onTaskError(Intent intent);
    }

    private final Context context;
    private final WeakReference<TaskListener> taskListenerRef;
    private final AccountManager accountManager;

    public RefreshWebLinkTask(Context context, TaskListener listener) {
        this.taskListenerRef = new WeakReference<>(listener);
        this.context = context.getApplicationContext();

        accountManager = App.get(context).getAccountManager();
    }

    @Override
    protected void onPostExecute(ParcelablePoint result) {
        super.onPostExecute(result);

        TaskListener listener = taskListenerRef.get();
        if (listener != null) listener.onTaskFinish(result.point);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();

        TaskListener listener = taskListenerRef.get();
        if (listener != null) listener.onTaskFinish(null);
    }

    @Override
    protected ParcelablePoint doInBackground(String... params) throws Exception {
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

            return new ParcelablePoint(mapper.createLocusPoint(cache));
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
        if (listener != null) listener.onTaskError(intent);
    }

    static final class ParcelablePoint implements Parcelable {
        public Point point;

        ParcelablePoint(Parcel in) {
            try {
                byte[] data = in.createByteArray();

                if (ArrayUtils.isNotEmpty(data)) {
                    point = new Point();
                    point.read(data);
                }
            } catch (IOException e) {
                Timber.e(e);
            }
        }

        ParcelablePoint(Point point) {
            this.point = point;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int i) {
            dest.writeByteArray(point != null ? point.getAsBytes() : null);
        }

        public static final Creator<ParcelablePoint> CREATOR = new Creator<ParcelablePoint>() {
            @Override
            public ParcelablePoint createFromParcel(Parcel in) {
                return new ParcelablePoint(in);
            }

            @Override
            public ParcelablePoint[] newArray(int size) {
                return new ParcelablePoint[size];
            }
        };
    }
}
