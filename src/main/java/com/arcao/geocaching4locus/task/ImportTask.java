package com.arcao.geocaching4locus.task;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import com.arcao.geocaching.api.GeocachingApi;
import com.arcao.geocaching.api.data.Geocache;
import com.arcao.geocaching.api.exception.GeocachingApiException;
import com.arcao.geocaching.api.exception.InvalidCredentialsException;
import com.arcao.geocaching.api.exception.InvalidSessionException;
import com.arcao.geocaching.api.impl.LiveGeocachingApiFactory;
import com.arcao.geocaching4locus.Geocaching4LocusApplication;
import com.arcao.geocaching4locus.ImportActivity;
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
import locus.api.utils.StoreableListFileOutput;
import locus.api.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class ImportTask extends UserTask<String, Void, Boolean> {
	private static final String TAG = ImportTask.class.getName();
	private int logCount;

	public interface OnTaskFinishedListener {
		void onTaskFinished(boolean success);
	}

	private WeakReference<OnTaskFinishedListener> onTaskFinishedListenerRef;

	public void setOnTaskFinishedListener(OnTaskFinishedListener onTaskFinishedListener) {
		this.onTaskFinishedListenerRef = new WeakReference<>(onTaskFinishedListener);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Geocaching4LocusApplication.getAppContext());

		logCount = prefs.getInt(PrefConstants.DOWNLOADING_COUNT_OF_LOGS, 5);
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);

		OnTaskFinishedListener listener = onTaskFinishedListenerRef.get();
		if (listener != null) {
			listener.onTaskFinished(result != null ? result : false);
		}
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();

		OnTaskFinishedListener listener = onTaskFinishedListenerRef.get();
		if (listener != null) {
			listener.onTaskFinished(false);
		}
	}

	@Override
	protected Boolean doInBackground(String... params) throws Exception {
		if (!Geocaching4LocusApplication.getAuthenticatorHelper().hasAccount())
			throw new InvalidCredentialsException("Account not found.");

		Context mContext = Geocaching4LocusApplication.getAppContext();
		WherigoService wherigoService = new WherigoServiceImpl();

		String cacheId = params[0];

		// if it's guid we need to convert to cache code
		if (!ImportActivity.CACHE_CODE_PATTERN.matcher(cacheId).find()) {
				cacheId = wherigoService.getCacheCodeFromGuid(cacheId);
		}

		GeocachingApi api = LiveGeocachingApiFactory.getLiveGeocachingApi();

		try {
			login(api);

			Geocache cache = api.getCache(cacheId, logCount, 0);
			Geocaching4LocusApplication.getAuthenticatorHelper().getRestrictions().updateLimits(api.getLastCacheLimits());

			if (isCancelled())
				return false;

			if (cache == null)
				throw new CacheNotFoundException(cacheId);

			File dataFile = ActionDisplayPointsExtended.getCacheFileName(mContext);
			StoreableListFileOutput slfo = null;

			try {
				slfo = new StoreableListFileOutput(ActionDisplayPointsExtended.getCacheFileOutputStream(mContext));

				Waypoint waypoint = LocusDataMapper.toLocusPoint(mContext, cache);
				PackWaypoints pack = new PackWaypoints("import");
				pack.addWaypoint(waypoint);

				slfo.beginList();
				slfo.write(pack);
				slfo.endList();
			} catch (IOException e) {
				Log.e(TAG, e.getMessage(), e);
				throw new GeocachingApiException(e.getMessage(), e);
			} finally {
				Utils.closeStream(slfo);
			}

			try {
				return ActionDisplayPointsExtended.sendPacksFile(mContext, dataFile, true, false, Intent.FLAG_ACTIVITY_NEW_TASK);
			} catch (RequiredVersionMissingException e) {
				Log.e(TAG, e.getMessage(), e);
				return false;
			}
		} catch (InvalidSessionException e) {
			Log.e(TAG, e.getMessage(), e);
			Geocaching4LocusApplication.getAuthenticatorHelper().invalidateAuthToken();

			throw e;
		}
	}


	@Override
	protected void onException(Throwable t) {
		super.onException(t);

		if (isCancelled())
			return;

		Log.e(TAG, t.getMessage(), t);

		Context mContext = Geocaching4LocusApplication.getAppContext();

		Intent intent = new ExceptionHandler(mContext).handle(t);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NEW_TASK);

		OnTaskFinishedListener listener = onTaskFinishedListenerRef.get();
		if (listener != null) {
			listener.onTaskFinished(false);
		}

		mContext.startActivity(intent);
	}

	private void login(GeocachingApi api) throws GeocachingApiException {
		String token = Geocaching4LocusApplication.getAuthenticatorHelper().getAuthToken();
		if (token == null) {
			Geocaching4LocusApplication.getAuthenticatorHelper().removeAccount();
			throw new InvalidCredentialsException("Account not found.");
		}

		api.openSession(token);
	}
}
