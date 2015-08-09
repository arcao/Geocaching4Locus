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
import com.arcao.geocaching.api.impl.live_geocaching_api.filter.CacheCodeFilter;
import com.arcao.geocaching.api.impl.live_geocaching_api.filter.Filter;
import com.arcao.geocaching4locus.Geocaching4LocusApplication;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.constants.PrefConstants;
import com.arcao.geocaching4locus.exception.ExceptionHandler;
import com.arcao.geocaching4locus.util.LocusTesting;
import com.arcao.geocaching4locus.util.UserTask;
import locus.api.android.ActionTools;
import locus.api.android.utils.LocusUtils;
import locus.api.android.utils.exceptions.RequiredVersionMissingException;
import locus.api.mapper.LocusDataMapper;
import locus.api.objects.extra.Waypoint;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class UpdateMoreTask extends UserTask<long[], Integer, Boolean> {
	private static final String TAG = UpdateMoreTask.class.getName();

	private int logCount;
	private LocusUtils.LocusVersion locusVersion;

	public interface OnTaskFinishedListener {
		void onTaskFinished(boolean success);
		void onProgressUpdate(int count);
	}

	private WeakReference<OnTaskFinishedListener> onTaskFinishedListenerRef;

	public void setOnTaskUpdateListener(OnTaskFinishedListener onTaskUpdateInterface) {
		this.onTaskFinishedListenerRef = new WeakReference<>(onTaskUpdateInterface);
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
		if (listener != null)
			listener.onTaskFinished(result);
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		OnTaskFinishedListener listener = onTaskFinishedListenerRef.get();
		if (listener != null)
			listener.onProgressUpdate(values[0]);
	}

	@Override
	protected Boolean doInBackground(long[]... params) throws Exception {
		Context context = Geocaching4LocusApplication.getAppContext();
		locusVersion = LocusTesting.getActiveVersion(context);

		long[] pointIndexes = params[0];

		if (!Geocaching4LocusApplication.getAuthenticatorHelper().hasAccount())
			throw new InvalidCredentialsException("Account not found.");

		GeocachingApi api = LiveGeocachingApiFactory.getLiveGeocachingApi();

		int current = 0;
		int count = pointIndexes.length;
		int cachesPerRequest = AppConstants.CACHES_PER_REQUEST;

		try {
			login(api);

			while (current < count) {
				long startTime = System.currentTimeMillis();

				// prepare old cache data
				List<Waypoint> oldPoints = prepareOldWaypointsFromIndexes(context, pointIndexes, current, cachesPerRequest);

				if (oldPoints.size() == 0) {
					// all are Waypoints without geocaching data
					current = current + Math.min(pointIndexes.length - current, cachesPerRequest);
					publishProgress(current);
					continue;
				}

				@SuppressWarnings({ "unchecked", "rawtypes" })
				List<Geocache> cachesToAdd = (List) api.searchForGeocaches(false, cachesPerRequest, logCount, 0, new Filter[] {
						new CacheCodeFilter(getCachesIds(oldPoints))
				});

				Geocaching4LocusApplication.getAuthenticatorHelper().getRestrictions().updateLimits(api.getLastCacheLimits());

				if (isCancelled())
					return false;

				if (cachesToAdd.size() == 0)
					break;

				List<Waypoint> points = LocusDataMapper.toLocusPoints(context, cachesToAdd);

				for (Waypoint p : points) {
					if (p == null || p.gcData == null)
						continue;

					// Geocaching API can return caches in a different order
					Waypoint oldPoint = searchOldPointByGCCode(oldPoints, p.gcData.getCacheID());

					p = LocusDataMapper.mergePoints(Geocaching4LocusApplication.getAppContext(), p, oldPoint);

					// update new point data in Locus
					ActionTools.updateLocusWaypoint(context, locusVersion, p, false);
				}

				current = current + Math.min(pointIndexes.length - current, cachesPerRequest);
				publishProgress(current);

				long requestDuration = System.currentTimeMillis() - startTime;
				cachesPerRequest = computeCachesPerRequest(cachesPerRequest, requestDuration);
			}

			publishProgress(current);

			Log.i(TAG, "updated caches: " + current);

			return current > 0;
		} catch (InvalidSessionException e) {
			Log.e(TAG, e.getMessage(), e);
			Geocaching4LocusApplication.getAuthenticatorHelper().invalidateAuthToken();

			throw e;
		}
	}

	private Waypoint searchOldPointByGCCode(Iterable<Waypoint> oldPoints, String gcCode) {
		if (gcCode == null || gcCode.length() == 0)
			return null;

		for (Waypoint oldPoint : oldPoints) {
			if (oldPoint.gcData != null && gcCode.equals(oldPoint.gcData.getCacheID())) {
				return oldPoint;
			}
		}

		return null;
	}

	private List<Waypoint> prepareOldWaypointsFromIndexes(Context context, long[] pointIndexes, int current, int cachesPerRequest) {
		int count = Math.min(pointIndexes.length - current, cachesPerRequest);
		
		List<Waypoint> waypoints = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			try {
				// get old waypoint from Locus
				Waypoint wpt = ActionTools.getLocusWaypoint(context, locusVersion, pointIndexes[current + i]);
				if (wpt == null || wpt.gcData == null || wpt.gcData.getCacheID() == null || wpt.gcData.getCacheID().length() == 0) {
					Log.w(TAG, "Waypoint " + (current + i) + " with id " + pointIndexes[current + i] + " isn't cache. Skipped...");
					continue;
				}

				waypoints.add(wpt);
			} catch (RequiredVersionMissingException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}


		return waypoints;
	}

	protected String[] getCachesIds(List<Waypoint> caches) {
		int count = caches.size();

		String[] ret = new String[count];

		for (int i = 0; i < count; i++) {
			ret[i] = caches.get(i).gcData.getCacheID();
		}

		return ret;
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();

		OnTaskFinishedListener listener = onTaskFinishedListenerRef.get();
		if (listener != null)
			listener.onTaskFinished(false);
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
		if (listener != null)
			listener.onTaskFinished(false);

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

	private int computeCachesPerRequest(int currentCachesPerRequest, long requestDuration) {
		int cachesPerRequest = currentCachesPerRequest;

		// keep the request time between ADAPTIVE_DOWNLOADING_MIN_TIME_MS and ADAPTIVE_DOWNLOADING_MAX_TIME_MS
		if (requestDuration < AppConstants.ADAPTIVE_DOWNLOADING_MIN_TIME_MS)
			cachesPerRequest+= AppConstants.ADAPTIVE_DOWNLOADING_STEP;

		if (requestDuration > AppConstants.ADAPTIVE_DOWNLOADING_MAX_TIME_MS)
			cachesPerRequest-= AppConstants.ADAPTIVE_DOWNLOADING_STEP;

		// keep the value in a range
		cachesPerRequest = Math.max(cachesPerRequest, AppConstants.ADAPTIVE_DOWNLOADING_MIN_CACHES);
		cachesPerRequest = Math.min(cachesPerRequest, AppConstants.ADAPTIVE_DOWNLOADING_MAX_CACHES);

		return cachesPerRequest;
	}
}
