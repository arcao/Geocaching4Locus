package com.arcao.geocaching4locus.task;

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
import com.arcao.geocaching.api.exception.GeocachingApiException;
import com.arcao.geocaching.api.exception.InvalidCredentialsException;
import com.arcao.geocaching.api.exception.InvalidSessionException;
import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.authentication.helper.AuthenticatorHelper;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.constants.PrefConstants;
import com.arcao.geocaching4locus.exception.CacheNotFoundException;
import com.arcao.geocaching4locus.exception.ExceptionHandler;
import com.arcao.geocaching4locus.exception.LocusMapRuntimeException;
import com.arcao.geocaching4locus.task.UpdateTask.UpdateTaskData;
import com.arcao.geocaching4locus.util.LocusTesting;
import com.arcao.geocaching4locus.util.UserTask;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import locus.api.android.ActionTools;
import locus.api.android.utils.LocusUtils;
import locus.api.mapper.LocusDataMapper;
import locus.api.objects.extra.Waypoint;
import org.apache.commons.lang3.ArrayUtils;
import timber.log.Timber;

public class UpdateTask extends UserTask<UpdateTaskData, Integer, UpdateTaskData> {

	public interface TaskListener {
		enum State {
			CACHE,
			LOGS
		}

		void onUpdateState(State state, int progress, int max);
		void onTaskFinished(Intent result);
	}


	private final WeakReference<TaskListener> mTaskListenerRef;
	private final Context mContext;
	private final LocusDataMapper mMapper;

	public UpdateTask(Context context, TaskListener listener) {
		mContext = context.getApplicationContext();
		mTaskListenerRef = new WeakReference<>(listener);
		mMapper = new LocusDataMapper(mContext);
	}

	@Override
	protected void onPostExecute(UpdateTaskData result) {
		super.onPostExecute(result);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		boolean replaceCache = PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW__UPDATE_ONCE.equals(prefs.getString(PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW, PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW__UPDATE_ONCE));
		boolean downloadLogsUpdateCache = prefs.getBoolean(PrefConstants.DOWNLOAD_LOGS_UPDATE_CACHE, true);

		LocusUtils.LocusVersion locusVersion;
		try {
			locusVersion = LocusTesting.getActiveVersion(mContext);
		} catch (Throwable t) {
			throw new LocusMapRuntimeException(t);
		}

		if (result == null || result.newPoint == null) {
			TaskListener listener = mTaskListenerRef.get();
			if (listener != null) {
				listener.onTaskFinished(null);
			}
			return;
		}

		if (result.updateLogs && !downloadLogsUpdateCache) {
			mMapper.mergeCacheLogs(result.oldPoint, result.newPoint);
			result.newPoint = result.oldPoint;
		} else {
			mMapper.mergePoints(result.newPoint, result.oldPoint);

			if (replaceCache) {
				result.newPoint.removeExtraOnDisplay();
			}
		}

		// if Waypoint is already in DB we must update it manually
		if (result.oldPoint != null) {
			try {
				ActionTools.updateLocusWaypoint(mContext, locusVersion, result.newPoint, false);
			} catch (Throwable t) {
				throw new LocusMapRuntimeException(t);
			}
		}

		TaskListener listener = mTaskListenerRef.get();
		if (listener != null) {
			listener.onTaskFinished(LocusUtils.prepareResultExtraOnDisplayIntent(result.newPoint, replaceCache));
		}
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);

		TaskListener listener = mTaskListenerRef.get();
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
		boolean basicMember = !authenticatorHelper.getRestrictions().isPremiumMember();

		if (!authenticatorHelper.hasAccount())
			throw new InvalidCredentialsException("Account not found.");

		UpdateTaskData result = params[0];

		GeocachingApi api = GeocachingApiFactory.create();

		try {
			login(api);

			publishProgress();

			int originalLogCount = logCount;

			GeocachingApi.ResultQuality resultQuality = GeocachingApi.ResultQuality.FULL;
			if (basicMember) {
				resultQuality = GeocachingApi.ResultQuality.LITE;
				logCount = 0;
			}

			Geocache cache = api.getGeocache(resultQuality, result.cacheId, logCount, 0);
			authenticatorHelper.getRestrictions().updateLimits(api.getLastGeocacheLimits());

			if (cache == null)
				throw new CacheNotFoundException(result.cacheId);

			if (isCancelled())
				return null;

			result.newPoint = mMapper.toLocusPoint(cache);

			if (basicMember) {
				// add trackables
				List<Trackable> trackables = api.getTrackablesByCacheCode(result.cacheId, 0, 30, 0);
				mMapper.addTrackables(result.newPoint, trackables);

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

					if (retrievedLogs == null || retrievedLogs.isEmpty()) {
						break;
					}

					mMapper.addCacheLogs(result.newPoint, retrievedLogs);

					startIndex += retrievedLogs.size();
				}
				publishProgress(maxLogs, maxLogs);
			}

			if (isCancelled())
				return null;

			return result;
		} catch (InvalidSessionException e) {
			Timber.e(e, e.getMessage());
			authenticatorHelper.invalidateOAuthToken();

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

	private void login(GeocachingApi api) throws GeocachingApiException {
		AuthenticatorHelper authenticatorHelper = App.get(mContext).getAuthenticatorHelper();

		String token = authenticatorHelper.getOAuthToken();
		if (token == null) {
			authenticatorHelper.removeAccount();
			throw new InvalidCredentialsException("Account not found.");
		}

		api.openSession(token);
	}

	public static class UpdateTaskData implements Parcelable {
		protected final String cacheId;
		protected Waypoint oldPoint;
		protected Waypoint newPoint = null;
		protected final boolean updateLogs;

		public UpdateTaskData(String cacheId, Waypoint waypoint, boolean updateLogs) {
			this.cacheId = cacheId;
			this.oldPoint = waypoint;
			this.updateLogs = updateLogs;
		}

		private UpdateTaskData(Parcel in) {
			cacheId = in.readString();

			try {
				byte[] data = in.createByteArray();
				if (ArrayUtils.isNotEmpty(data)) oldPoint = new Waypoint(data);
			} catch (IOException e) {
				Timber.e(e, e.getMessage());
			}

			try {
				byte[] data = in.createByteArray();
				if (ArrayUtils.isNotEmpty(data)) newPoint = new Waypoint(data);
			} catch (IOException e) {
				Timber.e(e, e.getMessage());
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

			if (oldPoint != null) {
				byte[] data = oldPoint.getAsBytes();
				dest.writeByteArray(data);
			} else {
				dest.writeByteArray(null);
			}

			if (newPoint != null) {
				byte[] data = newPoint.getAsBytes();
				dest.writeByteArray(data);
			} else {
				dest.writeByteArray(null);
			}

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
