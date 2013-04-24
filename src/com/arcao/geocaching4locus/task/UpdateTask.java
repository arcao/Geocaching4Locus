package com.arcao.geocaching4locus.task;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;

import locus.api.android.ActionTools;
import locus.api.android.utils.LocusUtils;
import locus.api.android.utils.RequiredVersionMissingException;
import locus.api.mapper.LocusDataMapper;
import locus.api.objects.extra.ExtraData;
import locus.api.objects.extra.Waypoint;
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
import com.arcao.geocaching4locus.constants.PrefConstants;
import com.arcao.geocaching4locus.exception.ExceptionHandler;
import com.arcao.geocaching4locus.task.UpdateTask.UpdateTaskData;
import com.arcao.geocaching4locus.util.UserTask;

public class UpdateTask extends UserTask<UpdateTaskData, Void, UpdateTaskData> {
	private static final String TAG = UpdateTask.class.getName();

	private int logCount;
	private boolean replaceCache;

	public interface OnTaskFinishedListener {
		void onTaskFinished(Intent result);
	}


	private WeakReference<OnTaskFinishedListener> onTaskFinishedListenerRef;

	public void setOnTaskFinishedListener(OnTaskFinishedListener onTaskFinishedListener) {
		this.onTaskFinishedListenerRef = new WeakReference<OnTaskFinishedListener>(onTaskFinishedListener);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Geocaching4LocusApplication.getAppContext());

		logCount = prefs.getInt(PrefConstants.DOWNLOADING_COUNT_OF_LOGS, 5);
		replaceCache = PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW__UPDATE_ONCE.equals(prefs.getString(PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW, PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW__UPDATE_ONCE));
	}

	@Override
	protected void onPostExecute(UpdateTaskData result) {
		super.onPostExecute(result);

		Context mContext = Geocaching4LocusApplication.getAppContext();

		if (result == null || result.newPoint == null) {
			OnTaskFinishedListener listener = onTaskFinishedListenerRef.get();
			if (listener != null) {
				listener.onTaskFinished(null);
			}
			return;
		}


		Waypoint p = LocusDataMapper.mergePoints(mContext, result.newPoint, result.oldWaypoint);

		if (replaceCache) {
			//ActionDisplayPointsExtended.storeGeocacheToCache(mContext, p);
			p.addParameter(ExtraData.PAR_INTENT_EXTRA_ON_DISPLAY, "clear;;;;;");
		}

		// if Waypoint is already in DB we must update it manually
		if (result.oldWaypoint != null) {
			try {
				ActionTools.updateLocusWaypoint(mContext, result.newPoint, false);
			} catch (RequiredVersionMissingException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}

		OnTaskFinishedListener listener = onTaskFinishedListenerRef.get();
		if (listener != null) {
			listener.onTaskFinished(LocusUtils.prepareResultExtraOnDisplayIntent(p, replaceCache));
		}
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();

		OnTaskFinishedListener listener = onTaskFinishedListenerRef.get();
		if (listener != null) {
			listener.onTaskFinished(null);
		}
	}

	@Override
	protected UpdateTaskData doInBackground(UpdateTaskData... params) throws Exception {
		if (!Geocaching4LocusApplication.getAuthenticatorHelper().hasAccount())
			throw new InvalidCredentialsException("Account not found.");

		UpdateTaskData result = params[0];

		GeocachingApi api = LiveGeocachingApiFactory.create();

		try {
			login(api);

			Geocache cache = api.getCache(result.cacheId, logCount, 0);
			Geocaching4LocusApplication.getAuthenticatorHelper().getRestrictions().updateLimits(api.getLastCacheLimits());

			if (isCancelled())
				return null;

			result.newPoint = LocusDataMapper.toLocusPoint(Geocaching4LocusApplication.getAppContext(), cache);
			return result;
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
			listener.onTaskFinished(null);
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

	public static class UpdateTaskData implements Serializable {
		private static final long serialVersionUID = 2711790385777741041L;

		protected final String cacheId;
		protected transient Waypoint oldWaypoint;
		protected transient Waypoint newPoint = null;

		public UpdateTaskData(String cacheId, Waypoint waypoint) {
			this.cacheId = cacheId;
			this.oldWaypoint = waypoint;
		}


		private void writeObject(java.io.ObjectOutputStream out) throws IOException {
			out.defaultWriteObject();

			if (oldWaypoint != null) {
				out.writeBoolean(true);
				oldWaypoint.write(new DataOutputStream(out));
			} else {
				out.writeBoolean(false);
			}

			if (newPoint != null) {
				out.writeBoolean(true);
				newPoint.write(new DataOutputStream(out));
			} else {
				out.writeBoolean(false);
			}
		}

		private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
			in.defaultReadObject();

			if (in.readBoolean()) {
				oldWaypoint = new Waypoint(new DataInputStream(in));
			}

			if (in.readBoolean()) {
				newPoint = new Waypoint(new DataInputStream(in));
			}
		}
	}
}
