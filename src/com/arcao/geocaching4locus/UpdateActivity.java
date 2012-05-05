package com.arcao.geocaching4locus;

import java.io.IOException;

import menion.android.locus.addon.publiclib.DisplayDataExtended;
import menion.android.locus.addon.publiclib.LocusDataMapper;
import menion.android.locus.addon.publiclib.LocusIntents;
import menion.android.locus.addon.publiclib.geoData.Point;

import org.acra.ErrorReporter;

import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
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
import com.arcao.geocaching.api.impl.LiveGeocachingApi;
import com.arcao.geocaching4locus.authentication.AccountAuthenticator;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.constants.PrefConstants;
import com.arcao.geocaching4locus.util.Throwables;
import com.arcao.geocaching4locus.util.UserTask;

public class UpdateActivity extends Activity {
	private final static String TAG = "G4L|UpdateActivity";
	
	public static String PARAM_CACHE_ID = "cacheId";
	public static String PARAM_SIMPLE_CACHE_ID = "simpleCacheId";
	
	private UpdateTask task = null;	
	protected Point oldPoint = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String cacheId = null;
		
		oldPoint = null;
		
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
		
		Log.i(TAG, "Starting update task for " + cacheId);
		task = new UpdateTask();
		task.execute(cacheId);
	}
	
	class UpdateTask extends UserTask<String, Void, Geocache> implements OnClickListener {
		private ProgressDialog dialog;
		private SharedPreferences prefs;
		private boolean replaceCache;
		private int logCount;
		private int trackableCount;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			prefs = PreferenceManager.getDefaultSharedPreferences(UpdateActivity.this);
			
			logCount = prefs.getInt(PrefConstants.DOWNLOADING_COUNT_OF_LOGS, 5);
			trackableCount = prefs.getInt(PrefConstants.DOWNLOADING_COUNT_OF_TRACKABLES, 10);
			replaceCache = PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW__UPDATE_ONCE.equals(prefs.getString(PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW, PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW__UPDATE_ONCE));
								
			dialog = new ProgressDialog(UpdateActivity.this);
			dialog.setIndeterminate(true);
			dialog.setMessage(getResources().getText(R.string.update_cache_progress));
			dialog.setButton(getResources().getText(R.string.cancel_button), this);
			dialog.show();
		}	
		
		@Override
		protected void onPostExecute(Geocache result) {
			super.onPostExecute(result);
			
			if (dialog != null && dialog.isShowing())
				dialog.dismiss();
			
			if (result == null) {
				setResult(RESULT_CANCELED);
				finish();
				return;
			}
			
			Point p = LocusDataMapper.toLocusPoint(UpdateActivity.this, result);
			p = LocusDataMapper.mergePoints(p, oldPoint);
			
			if (replaceCache) {
				DisplayDataExtended.storeGeocacheToCache(UpdateActivity.this, p);
			}
			
			setResult(RESULT_OK, LocusIntents.prepareResultExtraOnDisplayIntent(p, replaceCache));
			finish();
		}

		@Override
		protected Geocache doInBackground(String... params) throws Exception {
			if (!AccountAuthenticator.hasAccount(UpdateActivity.this))
				throw new InvalidCredentialsException("Account not found.");
			
			GeocachingApi api = new LiveGeocachingApi(AppConstants.CONSUMER_KEY, AppConstants.LICENCE_KEY);
			
			Geocache cache = null;
			try {
				login(api);
				cache = api.getCache(params[0], logCount, trackableCount);
			} catch (InvalidCredentialsException e) {
				AccountAuthenticator.clearPassword(UpdateActivity.this);
				
				login(api);
				cache = api.getCache(params[0], logCount, trackableCount);
			} catch (InvalidSessionException e) {
				AccountAuthenticator.invalidateAuthToken(UpdateActivity.this);
				
				// try againg
				login(api);
				cache = api.getCache(params[0], logCount, trackableCount);
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
			
			if (dialog != null && dialog.isShowing())
				dialog.dismiss();
			
			setResult(RESULT_CANCELED);
			finish();
		}
		
		@Override
		protected void onException(Throwable e) {
			super.onException(e);

			if (dialog != null && dialog.isShowing())
				dialog.dismiss();
			
			if (isCancelled())
				return;
			
			Log.e(TAG, e.getMessage(), e);
			
			Intent intent;
			
			if (e instanceof InvalidCredentialsException) {
				intent = createErrorIntent(R.string.error_credentials, null, true);
			} else {
				ErrorReporter.getInstance().handleSilentException(e);
				
				String message = e.getMessage();
				if (message == null)
					message = "";
				
				intent = createErrorIntent(R.string.error, String.format("%s<br>Exception: %s<br>Stack trace:<br>%s", message, e.getClass().getSimpleName(), Throwables.getStackTrace(e)), false);
			}
			
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
			setResult(RESULT_CANCELED);
			finish();
			
			startActivity(intent);
		}
		
		private Intent createErrorIntent(int resErrorId, String errorText, boolean openPreference) {
			Intent intent = new Intent(UpdateActivity.this, ErrorActivity.class);
			intent.setAction(ErrorActivity.ACTION_ERROR);
			intent.putExtra(ErrorActivity.PARAM_RESOURCE_ID, resErrorId);
			intent.putExtra(ErrorActivity.PARAM_ADDITIONAL_MESSAGE, errorText);
			intent.putExtra(ErrorActivity.PARAM_OPEN_PREFERENCE, openPreference);
			
			return intent;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			cancel(true);
		}
		
		private void login(GeocachingApi api) throws GeocachingApiException, OperationCanceledException {
			try {
				api.openSession(AccountAuthenticator.getAuthToken(UpdateActivity.this));
			} catch (AuthenticatorException e) {
				throw new GeocachingApiException(e.getMessage(), e);
			} catch (IOException e) {
				throw new GeocachingApiException(e.getMessage(), e);
			}
		}
	}
}
