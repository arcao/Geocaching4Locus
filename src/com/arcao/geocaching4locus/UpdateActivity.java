package com.arcao.geocaching4locus;

import menion.android.locus.addon.publiclib.DisplayDataExtended;
import menion.android.locus.addon.publiclib.LocusDataMapper;
import menion.android.locus.addon.publiclib.LocusIntents;
import menion.android.locus.addon.publiclib.geoData.Point;

import org.acra.ErrorReporter;

import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.arcao.geocaching.api.GeocachingApi;
import com.arcao.geocaching.api.data.Geocache;
import com.arcao.geocaching.api.exception.GeocachingApiException;
import com.arcao.geocaching.api.exception.InvalidCredentialsException;
import com.arcao.geocaching.api.exception.InvalidSessionException;
import com.arcao.geocaching.api.exception.NetworkException;
import com.arcao.geocaching.api.impl.LiveGeocachingApi;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.constants.PrefConstants;
import com.arcao.geocaching4locus.util.UserTask;

public class UpdateActivity extends Activity {
	private final static String TAG = "G4L|UpdateActivity";
	
	public static String PARAM_CACHE_ID = "cacheId";
	public static String PARAM_SIMPLE_CACHE_ID = "simpleCacheId";
	
	public static final int DIALOG_PROGRESS_ID = 0;
	
	private UpdateTask task;
	
	protected Point oldPoint;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		oldPoint = null;
		String cacheId = null;
		
		if (getIntent().hasExtra(PARAM_CACHE_ID)) {
			cacheId = getIntent().getStringExtra(PARAM_CACHE_ID);
		} else if (LocusIntents.isIntentOnPointAction(getIntent())) {
			oldPoint = LocusIntents.handleIntentOnPointAction(getIntent());
			
			if (oldPoint.getGeocachingData() != null) {
				cacheId = oldPoint.getGeocachingData().cacheID;
			}
		} else if (getIntent().hasExtra(PARAM_SIMPLE_CACHE_ID)) {
			cacheId = getIntent().getStringExtra(PARAM_SIMPLE_CACHE_ID);
			String repeatUpdate = prefs.getString(PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW, PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW__UPDATE_NEVER);
			
			if (PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW__UPDATE_NEVER.equals(repeatUpdate)) {
				Log.i(TAG, "Updating simple cache on dispaying is not allowed!");
				setResult(RESULT_CANCELED);
				finish();
				return;
			} else if (PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW__UPDATE_ONCE.equals(repeatUpdate)) {
				Point p = DisplayDataExtended.loadGeocacheFromCache(this, cacheId);
				if (p != null) {
					Log.i(TAG, "Found cache file for: " + cacheId);
					setResult(RESULT_OK, LocusIntents.prepareResultExtraOnDisplayIntent(p, false));
					finish();
					return;
				}
			}
		}
		
		if (cacheId == null) {
			Log.e(TAG, "cacheId/simpleCacheId not found");
			setResult(RESULT_CANCELED);
			finish();
			return;
		}
		
		ErrorReporter.getInstance().putCustomData("source", "update;" + cacheId);		

		if ((task = (UpdateTask) getLastNonConfigurationInstance()) == null) {
			Log.i(TAG, "Starting update task for " + cacheId);
			task = new UpdateTask(this);
			task.execute(cacheId);
		} else {
			Log.i(TAG, "Restarting update task for " + cacheId);
			task.attach(this);
		}
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		task.detach();
		return task;
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DIALOG_PROGRESS_ID:
				ProgressDialog dialog = new ProgressDialog(this);
				dialog.setIndeterminate(true);
				dialog.setMessage(getText(R.string.update_cache_progress));
				dialog.setButton(getText(R.string.cancel_button), new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						task.cancel(true);
					}
				});
				return dialog;

			default:
				return super.onCreateDialog(id);
		}
	}

	
	static class UpdateTask extends UserTask<String, Void, Geocache> {
		private boolean replaceCache;
		private int logCount;
		private UpdateActivity activity;
		
		public UpdateTask(UpdateActivity activity) {
			attach(activity);
		}
		
		public void attach(UpdateActivity activity) {
			this.activity = activity;			
		}
		
		public void detach() {
			activity = null;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			activity.showDialog(DIALOG_PROGRESS_ID);
			
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
			
			logCount = prefs.getInt(PrefConstants.DOWNLOADING_COUNT_OF_LOGS, 5);
			replaceCache = PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW__UPDATE_ONCE.equals(prefs.getString(PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW, PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW__UPDATE_ONCE));								
		}	
		
		@Override
		protected void onPostExecute(Geocache result) {
			super.onPostExecute(result);
			
			activity.dismissDialog(DIALOG_PROGRESS_ID);
			
			if (result == null) {
				activity.setResult(RESULT_CANCELED);
				activity.finish();
				return;
			}
			
			Point p = LocusDataMapper.toLocusPoint(activity, result);
			p = LocusDataMapper.mergePoints(p, activity.oldPoint);
			
			if (replaceCache) {
				DisplayDataExtended.storeGeocacheToCache(activity, p);
			}
			
			activity.setResult(RESULT_OK, LocusIntents.prepareResultExtraOnDisplayIntent(p, replaceCache));
			activity.finish();
		}

		@Override
		protected Geocache doInBackground(String... params) throws Exception {
			if (!Geocaching4LocusApplication.getAuthenticatorHelper().hasAccount())
				throw new InvalidCredentialsException("Account not found.");
			
			GeocachingApi api = new LiveGeocachingApi(AppConstants.CONSUMER_KEY, AppConstants.LICENCE_KEY);
			
			Geocache cache = null;
			try {
				login(api);
				cache = api.getCache(params[0], logCount, 0);
			} catch (InvalidSessionException e) {
				Geocaching4LocusApplication.getAuthenticatorHelper().invalidateAuthToken();
				
				// try againg
				login(api);
				cache = api.getCache(params[0], logCount, 0);
			} catch (OperationCanceledException e) {
				cancel(false);
			}
			
			if (isCancelled())
				return null;
			
			return cache;
		}
		
		@Override
		protected void onCancelled() {
			super.onCancelled();
			
			activity.dismissDialog(DIALOG_PROGRESS_ID);
			activity.setResult(RESULT_CANCELED);
			activity.finish();
		}
		
		@Override
		protected void onException(Throwable e) {
			super.onException(e);

			activity.dismissDialog(DIALOG_PROGRESS_ID);
			
			if (isCancelled())
				return;
			
			Log.e(TAG, e.getMessage(), e);
			
			Intent intent;
			
			if (e instanceof InvalidCredentialsException) {
				intent = ErrorActivity.createErrorIntent(activity, R.string.error_credentials, null, true, null);
			} else if (e instanceof NetworkException) {
				intent = ErrorActivity.createErrorIntent(activity, R.string.error_network, null, false, null);
			} else {
				String message = e.getMessage();
				if (message == null)
					message = "";
				
				intent = ErrorActivity.createErrorIntent(activity, R.string.error, String.format("%s<br>Exception: %s", message, e.getClass().getSimpleName()), false, e);
			}
			
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
			activity.startActivity(intent);
			
			activity.setResult(RESULT_CANCELED);
			activity.finish();			
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
}
