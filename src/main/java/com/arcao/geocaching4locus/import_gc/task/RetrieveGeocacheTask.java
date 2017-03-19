package com.arcao.geocaching4locus.import_gc.task;

import android.content.Context;
import android.content.Intent;

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
import com.arcao.geocaching4locus.import_gc.ImportActivity;
import com.arcao.geocaching4locus.import_gc.util.ParcelableGeocache;
import com.arcao.wherigoservice.api.WherigoApiFactory;
import com.arcao.wherigoservice.api.WherigoService;

import java.lang.ref.WeakReference;

import timber.log.Timber;

public class RetrieveGeocacheTask extends UserTask<String, Void, ParcelableGeocache> {
    public interface TaskListener {
        void onTaskFinished(Geocache geocache);
    }

    private final Context mContext;
    private final WeakReference<TaskListener> mTaskListenerRef;

    public RetrieveGeocacheTask(Context context, TaskListener listener) {
        this.mTaskListenerRef = new WeakReference<>(listener);
        mContext = context.getApplicationContext();
    }

    @Override
    protected void onPostExecute(ParcelableGeocache result) {
        super.onPostExecute(result);

        TaskListener listener = mTaskListenerRef.get();
        if (listener != null) {
            listener.onTaskFinished(result != null ? result.get() : null);
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();

        TaskListener listener = mTaskListenerRef.get();
        if (listener != null) {
            listener.onTaskFinished(null);
        }
    }

    @Override
    protected ParcelableGeocache doInBackground(String... params) throws Exception {
        AccountManager accountManager = App.get(mContext).getAccountManager();
        String cacheId = params[0];

        // if it's guid we need to convert to cache code
        if (!ImportActivity.CACHE_CODE_PATTERN.matcher(cacheId).find()) {
            WherigoService wherigoService = WherigoApiFactory.create();
            cacheId = wherigoService.getCacheCodeFromGuid(cacheId);
        }

        try {
            GeocachingApi api = GeocachingApiFactory.create();
            GeocachingApiLoginTask.create(mContext, api).perform();

            Geocache geocache = api.getGeocache(GeocachingApi.ResultQuality.LITE, cacheId, 0, 0);
            accountManager.getRestrictions().updateLimits(api.getLastGeocacheLimits());

            if (isCancelled())
                return null;

            if (geocache == null)
                throw new CacheNotFoundException(cacheId);

            return new ParcelableGeocache(geocache);
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

        Intent intent = new ExceptionHandler(mContext).handle(t);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NEW_TASK);

        TaskListener listener = mTaskListenerRef.get();
        if (listener != null) {
            listener.onTaskFinished(null);
        }

        mContext.startActivity(intent);
    }
}