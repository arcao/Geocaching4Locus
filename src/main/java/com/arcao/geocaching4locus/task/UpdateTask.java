package com.arcao.geocaching4locus.task;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.arcao.geocaching.api.GeocachingApi;
import com.arcao.geocaching.api.GeocachingApiFactory;
import com.arcao.geocaching.api.data.CacheLog;
import com.arcao.geocaching.api.data.Geocache;
import com.arcao.geocaching.api.exception.GeocachingApiException;
import com.arcao.geocaching.api.exception.InvalidCredentialsException;
import com.arcao.geocaching.api.exception.InvalidSessionException;
import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.authentication.helper.AuthenticatorHelper;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.constants.PrefConstants;
import com.arcao.geocaching4locus.exception.ExceptionHandler;
import com.arcao.geocaching4locus.task.UpdateTask.UpdateTaskData;
import com.arcao.geocaching4locus.util.LocusTesting;
import com.arcao.geocaching4locus.util.UserTask;
import locus.api.android.ActionTools;
import locus.api.android.utils.LocusUtils;
import locus.api.android.utils.exceptions.RequiredVersionMissingException;
import locus.api.mapper.LocusDataMapper;
import locus.api.objects.extra.ExtraData;
import locus.api.objects.extra.Waypoint;
import locus.api.utils.DataReaderBigEndian;
import locus.api.utils.DataWriterBigEndian;
import timber.log.Timber;

import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.List;

public class UpdateTask extends UserTask<UpdateTaskData, Integer, UpdateTaskData> {
	public interface TaskListener {
		enum State {
			CACHE,
			LOGS
		}

		void onUpdateState(State state, int progress);
		void onTaskFinished(Intent result);
	}


	private WeakReference<TaskListener> mTaskListenerRef;
	private Context mContext;

	public UpdateTask(Context context, TaskListener listener) {
		mContext = context.getApplicationContext();
		mTaskListenerRef = new WeakReference<>(listener);
	}

	@Override
	protected void onPostExecute(UpdateTaskData result) {
		super.onPostExecute(result);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		boolean replaceCache = PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW__UPDATE_ONCE.equals(prefs.getString(PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW, PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW__UPDATE_ONCE));
		boolean downloadLogsUpdateCache = prefs.getBoolean(PrefConstants.DOWNLOAD_LOGS_UPDATE_CACHE, true);

		LocusUtils.LocusVersion locusVersion = LocusTesting.getActiveVersion(mContext);

		if (result == null || result.newPoint == null) {
			TaskListener listener = mTaskListenerRef.get();
			if (listener != null) {
				listener.onTaskFinished(null);
			}
			return;
		}

		Waypoint p;
		if (result.updateLogs && !downloadLogsUpdateCache) {
			p = LocusDataMapper.mergeCacheLogs(result.oldPoint, result.newPoint);
			result.newPoint = p;
		} else {
			p = LocusDataMapper.mergePoints(mContext, result.newPoint, result.oldPoint);

			if (replaceCache) {
				//ActionDisplayPointsExtended.storeGeocacheToCache(mContext, p);
				p.addParameter(ExtraData.PAR_INTENT_EXTRA_ON_DISPLAY, "clear;;;;;");
			}
		}

		// if Waypoint is already in DB we must update it manually
		if (result.oldPoint != null) {
			try {
				ActionTools.updateLocusWaypoint(mContext, locusVersion, result.newPoint, false);
			} catch (RequiredVersionMissingException e) {
				Timber.e(e.getMessage(), e);
			}
		}

		TaskListener listener = mTaskListenerRef.get();
		if (listener != null) {
			listener.onTaskFinished(LocusUtils.prepareResultExtraOnDisplayIntent(p, replaceCache));
		}
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);

		TaskListener listener = mTaskListenerRef.get();
		if (listener != null) {
			if (values == null || values.length != 1) {
				listener.onUpdateState(TaskListener.State.CACHE, 0);
			} else {
				listener.onUpdateState(TaskListener.State.LOGS, values[0]);
			}
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
	protected UpdateTaskData doInBackground(UpdateTaskData... params) throws Exception {
		AuthenticatorHelper authenticatorHelper = App.get(mContext).getAuthenticatorHelper();

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		int logCount = prefs.getInt(PrefConstants.DOWNLOADING_COUNT_OF_LOGS, 5);


		if (!authenticatorHelper.hasAccount())
			throw new InvalidCredentialsException("Account not found.");

		UpdateTaskData result = params[0];

		GeocachingApi api = GeocachingApiFactory.create();

		try {
			login(api);

			publishProgress();

			Geocache cache = api.getCache(result.cacheId, logCount, 0);
			authenticatorHelper.getRestrictions().updateLimits(api.getLastCacheLimits());

			if (result.updateLogs) {
				int startIndex = logCount;
				int maxLogs = AppConstants.LOGS_TO_UPDATE_MAX - logCount;

				while (startIndex < maxLogs) {
					publishProgress(startIndex);

					int logsPerRequest = Math.min(maxLogs - startIndex, AppConstants.LOGS_PER_REQUEST);
					List<CacheLog> retrievedLogs = api.getCacheLogsByCacheCode(result.cacheId, startIndex, logsPerRequest);

					if (retrievedLogs == null || retrievedLogs.isEmpty()) {
						break;
					}

					cache.getCacheLogs().addAll(retrievedLogs);

					startIndex += retrievedLogs.size();
				}
				publishProgress(AppConstants.LOGS_TO_UPDATE_MAX);
			}

			if (isCancelled())
				return null;

			result.newPoint = LocusDataMapper.toLocusPoint(mContext, cache);
			return result;
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
			listener.onTaskFinished(null);
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

	public static class UpdateTaskData implements Serializable {
		private static final long serialVersionUID = 2711790385777741041L;

		protected final String cacheId;
		protected transient Waypoint oldPoint;
		protected transient Waypoint newPoint = null;
		protected final boolean updateLogs;

		public UpdateTaskData(String cacheId, Waypoint waypoint, boolean updateLogs) {
			this.cacheId = cacheId;
			this.oldPoint = waypoint;
			this.updateLogs = updateLogs;
		}


		private void writeObject(java.io.ObjectOutputStream out) throws IOException {
			out.defaultWriteObject();

			DataWriterBigEndian writer = new DataWriterBigEndian();

			if (oldPoint != null) {
				oldPoint.write(writer);

				out.writeInt(writer.size());
				writer.writeTo(out);
			} else {
				out.writeInt(0);
			}

			writer.reset();

			if (newPoint != null) {
				newPoint.write(writer);

				out.writeInt(writer.size());
				writer.writeTo(out);
			} else {
				out.writeInt(0);
			}
		}

		private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
			in.defaultReadObject();

			int len = in.readInt();
			if (len > 0) {
				byte[] buffer = new byte[len];
				if (in.read(buffer) > 0) {
					oldPoint = new Waypoint(new DataReaderBigEndian(buffer));
				}
			}

			len = in.readInt();
			if (len > 0) {
				byte[] buffer = new byte[len];
				if (in.read(buffer) > 0) {
					newPoint = new Waypoint(new DataReaderBigEndian(buffer));
				}
			}
		}
	}
}
