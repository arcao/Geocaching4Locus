package com.arcao.geocaching4locus.update.task;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.arcao.geocaching.api.GeocachingApi;
import com.arcao.geocaching.api.GeocachingApiFactory;
import com.arcao.geocaching.api.data.Geocache;
import com.arcao.geocaching.api.exception.InvalidSessionException;
import com.arcao.geocaching.api.filter.CacheCodeFilter;
import com.arcao.geocaching.api.filter.Filter;
import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.authentication.util.AccountManager;
import com.arcao.geocaching4locus.authentication.task.GeocachingApiLoginTask;
import com.arcao.geocaching4locus.base.constants.AppConstants;
import com.arcao.geocaching4locus.base.constants.PrefConstants;
import com.arcao.geocaching4locus.base.task.UserTask;
import com.arcao.geocaching4locus.base.util.LocusTesting;
import com.arcao.geocaching4locus.error.exception.LocusMapRuntimeException;
import com.arcao.geocaching4locus.error.handler.ExceptionHandler;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import locus.api.android.ActionTools;
import locus.api.android.utils.LocusUtils;
import locus.api.mapper.DataMapper;
import locus.api.mapper.WaypointMerger;
import locus.api.objects.extra.Waypoint;
import timber.log.Timber;

public class UpdateMoreTask extends UserTask<long[], Integer, Boolean> {
	public interface TaskListener {
		void onTaskFinished(boolean success);
		void onProgressUpdate(int count);
	}

	private final Context mContext;
	private final WeakReference<TaskListener> mTaskListenerRef;

	public UpdateMoreTask(Context context, TaskListener listener) {
		mContext = context.getApplicationContext();
		mTaskListenerRef = new WeakReference<>(listener);
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);

		TaskListener listener = mTaskListenerRef.get();
		if (listener != null)
			listener.onTaskFinished(result);
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		TaskListener listener = mTaskListenerRef.get();
		if (listener != null)
			listener.onProgressUpdate(values[0]);
	}

	@Override
	protected Boolean doInBackground(long[]... params) throws Exception {
		AccountManager accountManager = App.get(mContext).getAccountManager();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		DataMapper mapper = new DataMapper(mContext);
		WaypointMerger merger = new WaypointMerger(mContext);

		LocusUtils.LocusVersion locusVersion;
		try {
			locusVersion = LocusTesting.getActiveVersion(mContext);
		} catch (Throwable t) {
			throw new LocusMapRuntimeException(t);
		}

		try {
			GeocachingApi api = GeocachingApiFactory.create();
			GeocachingApiLoginTask.create(mContext, api).perform();

			int logCount = prefs.getInt(PrefConstants.DOWNLOADING_COUNT_OF_LOGS, 5);

			GeocachingApi.ResultQuality resultQuality = GeocachingApi.ResultQuality.FULL;
			if (!accountManager.isPremium()) {
				resultQuality = GeocachingApi.ResultQuality.LITE;
				logCount = 0;
			}

			long[] pointIndexes = params[0];

			int current = 0;
			int count = pointIndexes.length;
			int cachesPerRequest = AppConstants.CACHES_PER_REQUEST;
			while (current < count) {
				long startTime = System.currentTimeMillis();

				// prepare old cache data
				List<Waypoint> oldPoints = prepareOldWaypointsFromIndexes(mContext, locusVersion, pointIndexes, current, cachesPerRequest);

				if (oldPoints.isEmpty()) {
					// all are Waypoints without geocaching data
					current += Math.min(pointIndexes.length - current, cachesPerRequest);
					publishProgress(current);
					continue;
				}

				List<Geocache> cachesToAdd = api.searchForGeocaches(resultQuality, cachesPerRequest, logCount, 0, Collections.singletonList(
								(Filter) new CacheCodeFilter(getCachesIds(oldPoints))
				), null);

				accountManager.getRestrictions().updateLimits(api.getLastGeocacheLimits());

				if (isCancelled())
					return false;

				if (cachesToAdd.isEmpty())
					break;

				List<Waypoint> points = mapper.createLocusWaypoints(cachesToAdd);
				for (Waypoint p : points) {
					if (p == null || p.gcData == null)
						continue;

					// Geocaching API can return caches in a different order
					Waypoint oldPoint = searchOldPointByGCCode(oldPoints, p.gcData.getCacheID());
					merger.mergeWaypoint(p, oldPoint);

					// update new point data in Locus
					ActionTools.updateLocusWaypoint(mContext, locusVersion, p, false);
				}

				current += Math.min(pointIndexes.length - current, cachesPerRequest);
				publishProgress(current);

				long requestDuration = System.currentTimeMillis() - startTime;
				cachesPerRequest = computeCachesPerRequest(cachesPerRequest, requestDuration);
			}

			Timber.i("updated caches: " + current);

			publishProgress(current);
			return current > 0;
		} catch (InvalidSessionException e) {
			Timber.e(e, e.getMessage());
			accountManager.invalidateOAuthToken();

			throw e;
		}
	}

	private Waypoint searchOldPointByGCCode(Iterable<Waypoint> oldPoints, String gcCode) {
		if (gcCode == null || gcCode.isEmpty())
			return null;

		for (Waypoint oldPoint : oldPoints) {
			if (oldPoint.gcData != null && gcCode.equals(oldPoint.gcData.getCacheID())) {
				return oldPoint;
			}
		}

		return null;
	}

	private List<Waypoint> prepareOldWaypointsFromIndexes(Context context, LocusUtils.LocusVersion locusVersion, long[] pointIndexes, int current, int cachesPerRequest) {
		int count = Math.min(pointIndexes.length - current, cachesPerRequest);
		List<Waypoint> waypoints = new ArrayList<>(count);

		for (int i = 0; i < count; i++) {
			try {
				// get old waypoint from Locus
				Waypoint wpt = ActionTools.getLocusWaypoint(context, locusVersion, pointIndexes[current + i]);
				if (wpt == null || wpt.gcData == null || wpt.gcData.getCacheID() == null
						|| !wpt.gcData.getCacheID().toUpperCase(Locale.US).startsWith("GC")) {
					Timber.w("Waypoint " + (current + i) + " with id " + pointIndexes[current + i] + " isn't cache. Skipped...");
					continue;
				}

				waypoints.add(wpt);
			} catch (Throwable t) {
				throw new LocusMapRuntimeException(t);
			}
		}

		return waypoints;
	}

	private String[] getCachesIds(List<Waypoint> caches) {
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

		TaskListener listener = mTaskListenerRef.get();
		if (listener != null)
			listener.onTaskFinished(false);
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
		if (listener != null)
			listener.onTaskFinished(false);

		mContext.startActivity(intent);
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
