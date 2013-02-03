package com.arcao.geocaching4locus.task;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import locus.api.android.ActionTools;
import locus.api.android.utils.RequiredVersionMissingException;
import locus.api.mapper.LocusDataMapper;
import locus.api.objects.extra.Waypoint;
import android.accounts.OperationCanceledException;
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
import com.arcao.geocaching.api.exception.NetworkException;
import com.arcao.geocaching.api.impl.LiveGeocachingApiFactory;
import com.arcao.geocaching.api.impl.live_geocaching_api.filter.CacheCodeFilter;
import com.arcao.geocaching.api.impl.live_geocaching_api.filter.Filter;
import com.arcao.geocaching4locus.ErrorActivity;
import com.arcao.geocaching4locus.Geocaching4LocusApplication;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.constants.PrefConstants;
import com.arcao.geocaching4locus.util.UserTask;

public class UpdateMoreTask extends UserTask<long[], Integer, Boolean> {
	private static final String TAG = UpdateMoreTask.class.getName();
	
	private int logCount;
	
	public interface OnTaskFinishedListener {
		void onTaskFinished(boolean success);
		void onProgressUpdate(int count);
	}
	
	private WeakReference<OnTaskFinishedListener> onTaskFinishedListenerRef;
	
	public void setOnTaskUpdateListener(OnTaskFinishedListener onTaskUpdateInterface) {
		this.onTaskFinishedListenerRef = new WeakReference<OnTaskFinishedListener>(onTaskUpdateInterface);
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
		long[] pointIndexes = params[0];
		
		if (!Geocaching4LocusApplication.getAuthenticatorHelper().hasAccount())
			throw new InvalidCredentialsException("Account not found.");
		
		GeocachingApi api = LiveGeocachingApiFactory.create();
		
		int attempt = 0;
		int current = 0;
		int count = pointIndexes.length;
		
		while (++attempt <= 2) {
			try {
				login(api);
				
				current = 0;
				while (current < count) {
					// prepare old cache data
					List<Waypoint> oldPoints = prepareOldWaypointsFromIndexes(context, pointIndexes, current, AppConstants.CACHES_PER_REQUEST);
					
					if (oldPoints.size() == 0) {
						// all are Waypoints without geocaching data
						current = current + Math.min(pointIndexes.length - current, AppConstants.CACHES_PER_REQUEST);
						publishProgress(current);
						continue;
					}
					
					@SuppressWarnings({ "unchecked", "rawtypes" })
					List<Geocache> cachesToAdd = (List) api.searchForGeocaches(false, AppConstants.CACHES_PER_REQUEST, logCount, 0, new Filter[] {
							new CacheCodeFilter(getPagedCachesIds(oldPoints, current, AppConstants.CACHES_PER_REQUEST))
					});
					
					if (isCancelled())
						return false;
	
					if (cachesToAdd.size() == 0)
						break;
					
					List<Waypoint> points = LocusDataMapper.toLocusPoints(context, cachesToAdd);
					
					int index = 0;
					for (Waypoint p : points) {
						Waypoint oldPoint = oldPoints.get(index);
						
						// if updated cache doesn't exist use old
						if (p == null) {
							p = oldPoint;
						}
													
						p = LocusDataMapper.mergePoints(Geocaching4LocusApplication.getAppContext(), p, oldPoint);
						
						// update new point data in Locus
						ActionTools.updateLocusWaypoint(context, p, false);
						
						index++;
					}
										
					current = current + Math.min(pointIndexes.length - current, AppConstants.CACHES_PER_REQUEST);
					publishProgress(current);
					
					// force memory clean
					oldPoints = null;
					cachesToAdd = null;
					points = null;
				}
				publishProgress(current);

				Log.i(TAG, "updated caches: " + current);
	
				if (current > 0) {
					return true;
				} else {
					return false;
				}
				
			} catch (InvalidSessionException e) {
				Log.e(TAG, e.getMessage(), e);
				Geocaching4LocusApplication.getAuthenticatorHelper().invalidateAuthToken();
				
				if (attempt == 1)
					continue;
				
				throw e;
			} catch (OperationCanceledException e) {
				Log.e(TAG, e.getMessage(), e);
				
				return false;
			}
		}

		return null;
	}
	
	private List<Waypoint> prepareOldWaypointsFromIndexes(Context context, long[] pointIndexes, int current, int cachesPerRequest) {
		List<Waypoint> waypoints = new ArrayList<Waypoint>();
		
		int count = Math.min(pointIndexes.length - current, cachesPerRequest);
		
		for (int i = 0; i < count; i++) {
			try {
				// get old waypoint from Locus
				Waypoint wpt = ActionTools.getLocusWaypoint(context, pointIndexes[current + i]);
				if (wpt.gcData == null) {
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

	protected String[] getPagedCachesIds(List<Waypoint> caches, int current, int cachesPerRequest) {
		int count = Math.min(caches.size() - current, cachesPerRequest);
		
		String[] ret = new String[count];

		for (int i = 0; i < count; i++) {
			ret[i] = caches.get(current + i).gcData.getCacheID();
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
	protected void onException(Throwable e) {
		super.onException(e);

		if (isCancelled())
			return;
		
		Log.e(TAG, e.getMessage(), e);
		
		Intent intent;
		Context mContext = Geocaching4LocusApplication.getAppContext();
		
		if (e instanceof InvalidCredentialsException) {
			intent = ErrorActivity.createErrorIntent(mContext, R.string.error_credentials, null, true, null);
		} else if (e instanceof NetworkException) {
			intent = ErrorActivity.createErrorIntent(mContext, R.string.error_network, null, false, null);
		} else {
			String message = e.getMessage();
			if (message == null)
				message = "";
			
			intent = ErrorActivity.createErrorIntent(mContext, R.string.error, String.format("%s<br>Exception: %s", message, e.getClass().getSimpleName()), false, e);
		}
		
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NEW_TASK);
		
		OnTaskFinishedListener listener = onTaskFinishedListenerRef.get();
		if (listener != null)
			listener.onTaskFinished(false);
		
		mContext.startActivity(intent);
	}
	
	private void login(GeocachingApi api) throws GeocachingApiException, OperationCanceledException {
		String token = Geocaching4LocusApplication.getAuthenticatorHelper().getAuthToken();
		if (token == null) {
			Geocaching4LocusApplication.getAuthenticatorHelper().removeAccount();
			throw new InvalidCredentialsException("Account not found.");
		}
			
		api.openSession(token);
	}
}
