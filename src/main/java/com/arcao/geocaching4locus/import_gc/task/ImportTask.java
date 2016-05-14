package com.arcao.geocaching4locus.import_gc.task;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.arcao.geocaching.api.GeocachingApi;
import com.arcao.geocaching.api.GeocachingApiFactory;
import com.arcao.geocaching.api.data.Geocache;
import com.arcao.geocaching.api.exception.GeocachingApiException;
import com.arcao.geocaching.api.exception.InvalidCredentialsException;
import com.arcao.geocaching.api.exception.InvalidSessionException;
import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.import_gc.ImportActivity;
import com.arcao.geocaching4locus.authentication.util.AccountManager;
import com.arcao.geocaching4locus.base.constants.PrefConstants;
import com.arcao.geocaching4locus.error.exception.CacheNotFoundException;
import com.arcao.geocaching4locus.error.handler.ExceptionHandler;
import com.arcao.geocaching4locus.error.exception.LocusMapRuntimeException;
import com.arcao.geocaching4locus.base.task.UserTask;
import com.arcao.wherigoservice.api.WherigoApiFactory;
import com.arcao.wherigoservice.api.WherigoService;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import locus.api.android.ActionDisplayPointsExtended;
import locus.api.android.objects.PackWaypoints;
import locus.api.mapper.LocusDataMapper;
import locus.api.objects.extra.Waypoint;
import locus.api.utils.StoreableWriter;
import locus.api.utils.Utils;
import timber.log.Timber;

public class ImportTask extends UserTask<String, Void, Boolean> {
	public interface TaskListener {
		void onTaskFinished(boolean success);
	}

	private final Context mContext;
	private final WeakReference<TaskListener> mTaskListenerRef;

	public ImportTask(Context context, TaskListener listener) {
		this.mTaskListenerRef = new WeakReference<>(listener);
		mContext = context.getApplicationContext();
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);

		TaskListener listener = mTaskListenerRef.get();
		if (listener != null) {
			listener.onTaskFinished(result != null ? result : false);
		}
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();

		TaskListener listener = mTaskListenerRef.get();
		if (listener != null) {
			listener.onTaskFinished(false);
		}
	}

	@Override
	protected Boolean doInBackground(String... params) throws Exception {
		AccountManager accountManager = App.get(mContext).getAccountManager();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		int logCount = prefs.getInt(PrefConstants.DOWNLOADING_COUNT_OF_LOGS, 5);

		if (!accountManager.hasAccount())
			throw new InvalidCredentialsException("Account not found.");

		WherigoService wherigoService = WherigoApiFactory.create();
		LocusDataMapper mapper = new LocusDataMapper(mContext);

		String cacheId = params[0];

		// if it's guid we need to convert to cache code
		if (!ImportActivity.CACHE_CODE_PATTERN.matcher(cacheId).find()) {
				cacheId = wherigoService.getCacheCodeFromGuid(cacheId);
		}

		GeocachingApi api = GeocachingApiFactory.create();

		try {
			login(api);

			GeocachingApi.ResultQuality resultQuality = GeocachingApi.ResultQuality.FULL;
			if (!accountManager.getRestrictions().isPremiumMember()) {
				resultQuality = GeocachingApi.ResultQuality.LITE;
				logCount = 0;
			}

			Geocache cache = api.getGeocache(resultQuality, cacheId, logCount, 0);
			accountManager.getRestrictions().updateLimits(api.getLastGeocacheLimits());

			if (isCancelled())
				return false;

			if (cache == null)
				throw new CacheNotFoundException(cacheId);

			File dataFile = ActionDisplayPointsExtended.getCacheFileName(mContext);
			StoreableWriter writer = null;

			try {
				writer = new StoreableWriter(ActionDisplayPointsExtended.getCacheFileOutputStream(mContext));

				Waypoint waypoint = mapper.toLocusPoint(cache);
				PackWaypoints pack = new PackWaypoints("import");
				pack.addWaypoint(waypoint);

				writer.write(pack);
			} catch (IOException e) {
				Timber.e(e, e.getMessage());
				throw new GeocachingApiException(e.getMessage(), e);
			} finally {
				Utils.closeStream(writer);
			}

			try {
				return ActionDisplayPointsExtended.sendPacksFile(mContext, dataFile, true, false, Intent.FLAG_ACTIVITY_NEW_TASK);
			} catch (Throwable t) {
				throw new LocusMapRuntimeException(t);
			}

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
			listener.onTaskFinished(false);
		}

		mContext.startActivity(intent);
	}

	private void login(GeocachingApi api) throws GeocachingApiException {
		AccountManager accountManager = App.get(mContext).getAccountManager();

		String token = accountManager.getOAuthToken();
		if (token == null) {
			accountManager.removeAccount();
			throw new InvalidCredentialsException("Account not found.");
		}

		api.openSession(token);
	}
}
