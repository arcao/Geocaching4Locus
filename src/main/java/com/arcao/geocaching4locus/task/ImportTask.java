package com.arcao.geocaching4locus.task;

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
import com.arcao.geocaching4locus.ImportActivity;
import com.arcao.geocaching4locus.authentication.helper.AuthenticatorHelper;
import com.arcao.geocaching4locus.constants.PrefConstants;
import com.arcao.geocaching4locus.exception.CacheNotFoundException;
import com.arcao.geocaching4locus.exception.ExceptionHandler;
import com.arcao.geocaching4locus.util.UserTask;
import com.arcao.wherigoservice.api.WherigoService;
import com.arcao.wherigoservice.api.WherigoServiceImpl;
import locus.api.android.ActionDisplayPointsExtended;
import locus.api.android.objects.PackWaypoints;
import locus.api.android.utils.exceptions.RequiredVersionMissingException;
import locus.api.mapper.LocusDataMapper;
import locus.api.objects.extra.Waypoint;
import timber.log.Timber;

import java.lang.ref.WeakReference;

public class ImportTask extends UserTask<String, Void, Waypoint> {
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
	protected void onPostExecute(Waypoint result) {
		super.onPostExecute(result);

		if (result != null) {
			PackWaypoints pack = new PackWaypoints("import");
			pack.addWaypoint(result);

			try {
				ActionDisplayPointsExtended.sendPack(mContext, pack, true, false, Intent.FLAG_ACTIVITY_NEW_TASK);
			} catch (RequiredVersionMissingException e) {
				Timber.e(e.getMessage(), e);
			}
		}

		TaskListener listener = mTaskListenerRef.get();
		if (listener != null) {
			listener.onTaskFinished(result != null);
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
	protected Waypoint doInBackground(String... params) throws Exception {
		AuthenticatorHelper authenticatorHelper = App.get(mContext).getAuthenticatorHelper();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		int logCount = prefs.getInt(PrefConstants.DOWNLOADING_COUNT_OF_LOGS, 5);

		if (!authenticatorHelper.hasAccount())
			throw new InvalidCredentialsException("Account not found.");

		WherigoService wherigoService = new WherigoServiceImpl();

		String cacheId = params[0];

		// if it's guid we need to convert to cache code
		if (!ImportActivity.CACHE_CODE_PATTERN.matcher(cacheId).find()) {
				cacheId = wherigoService.getCacheCodeFromGuid(cacheId);
		}

		GeocachingApi api = GeocachingApiFactory.create();

		try {
			login(api);

			Geocache cache = api.getCache(cacheId, logCount, 0);
			authenticatorHelper.getRestrictions().updateLimits(api.getLastCacheLimits());

			if (isCancelled())
				return null;

			if (cache == null)
				throw new CacheNotFoundException(cacheId);

			return LocusDataMapper.toLocusPoint(mContext, cache);
		} catch (InvalidSessionException e) {
			Timber.e(e.getMessage(), e);
			authenticatorHelper.invalidateAuthToken();

			throw e;
		}
	}


	@Override
	protected void onException(Throwable t) {
		super.onException(t);

		if (isCancelled())
			return;

		Timber.e(t.getMessage(), t);

		Intent intent = new ExceptionHandler(mContext).handle(t);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NEW_TASK);

		TaskListener listener = mTaskListenerRef.get();
		if (listener != null) {
			listener.onTaskFinished(false);
		}

		mContext.startActivity(intent);
	}

	private void login(GeocachingApi api) throws GeocachingApiException {
		AuthenticatorHelper authenticatorHelper = App.get(mContext).getAuthenticatorHelper();

		String token = authenticatorHelper.getAuthToken();
		if (token == null) {
			authenticatorHelper.removeAccount();
			throw new InvalidCredentialsException("Account not found.");
		}

		api.openSession(token);
	}
}
