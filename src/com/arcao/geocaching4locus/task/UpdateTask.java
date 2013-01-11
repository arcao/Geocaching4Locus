package com.arcao.geocaching4locus.task;

import java.lang.ref.WeakReference;

import locus.api.android.utils.LocusUtils;
import locus.api.mapper.LocusDataMapper;
import locus.api.objects.extra.ExtraData;
import locus.api.objects.extra.Waypoint;

import org.apache.commons.lang3.tuple.Pair;

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
import com.arcao.geocaching4locus.ErrorActivity;
import com.arcao.geocaching4locus.Geocaching4LocusApplication;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.constants.PrefConstants;
import com.arcao.geocaching4locus.task.UpdateTask.UpdateTaskData;
import com.arcao.geocaching4locus.util.UserTask;

public class UpdateTask extends UserTask<UpdateTaskData, Void, UpdateTaskData> {
	private static final String TAG = UpdateTask.class.getName();
	
	private int logCount;
	private boolean replaceCache;
	
	public interface OnTaskFinishedListener {
		void onTaskFinished(Intent result);
	}
	
	public static class UpdateTaskData {
		public Pair<String, Waypoint> cache;
		protected Waypoint newPoint;
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
		
		
		Waypoint p = LocusDataMapper.mergePoints(mContext, result.newPoint, result.cache.getValue());
	
		if (replaceCache) {
			//ActionDisplayPointsExtended.storeGeocacheToCache(mContext, p);
			p.addParameter(ExtraData.PAR_INTENT_EXTRA_ON_DISPLAY, "clear;;;;;");
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
		
		int attempt = 0;
		
		while (++attempt <= 2) {
			try {
				login(api);
					
				Geocache cache = api.getCache(result.cache.getKey(), logCount, 0);
					
				if (isCancelled())
					return null;
	
				result.newPoint = LocusDataMapper.toLocusPoint(Geocaching4LocusApplication.getAppContext(), cache);
				return result;
			} catch (InvalidSessionException e) {
				Log.e(TAG, e.getMessage(), e);
				Geocaching4LocusApplication.getAuthenticatorHelper().invalidateAuthToken();
				
				if (attempt == 1)
					continue;
				
				throw e;
			} catch (OperationCanceledException e) {
				Log.e(TAG, e.getMessage(), e);
				
				return null;
			}
		}

		return null;
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
		if (listener != null) {
			listener.onTaskFinished(null);
		}
		
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
