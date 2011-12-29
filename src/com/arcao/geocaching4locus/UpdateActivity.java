package com.arcao.geocaching4locus;

import geocaching.api.AbstractGeocachingApi;
import geocaching.api.AbstractGeocachingApiV2;
import geocaching.api.data.Geocache;
import geocaching.api.data.SimpleGeocache;
import geocaching.api.exception.GeocachingApiException;
import geocaching.api.exception.InvalidCredentialsException;
import geocaching.api.exception.InvalidSessionException;
import geocaching.api.impl.LiveGeocachingApi;

import java.util.List;

import menion.android.locus.addon.publiclib.LocusConst;
import menion.android.locus.addon.publiclib.geoData.Point;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.arcao.geocaching4locus.util.Account;
import com.arcao.geocaching4locus.util.UserTask;

public class UpdateActivity extends Activity {
	private final static String TAG = "Geocaching4Locus|UpdateActivity";
	private UpdateTask task = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		boolean downloadAllCacheDataOnDisplaying = prefs.getBoolean("download_all_cache_data_on_displaying", true);
		String cacheId = null;
		
		if (getIntent().hasExtra("cacheId")) {
			cacheId = getIntent().getStringExtra("cacheId");
		} else if (getIntent().hasExtra("simpleCacheId")) {
			if (!downloadAllCacheDataOnDisplaying) {
				Log.i(TAG, "Updating simple cache on dispaying is not allowed!");
				setResult(RESULT_CANCELED);
				finish();
				return;
			}
			cacheId = getIntent().getStringExtra("simpleCacheId");
		} else {
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
		private Account account;
		private int logCount;
		private int trackableCount;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			prefs = PreferenceManager.getDefaultSharedPreferences(UpdateActivity.this);
			
			String userName = prefs.getString("username", "");
			String password = prefs.getString("password", "");
			String session = prefs.getString("session", null);
			
			logCount = prefs.getInt("downloading_count_of_logs", 5);
			trackableCount = prefs.getInt("downloading_count_of_trackabless", 10);
						
			account = new Account(userName, password, session);
			
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
			
			Point p = result.toPoint();
			p.setExtraCallback(getResources().getString(R.string.locus_update_cache), "com.arcao.geocaching4locus", UpdateActivity.class.getName(), "cacheId", result.getGeoCode());
			
			Intent intent = new Intent();
			intent.putExtra(LocusConst.EXTRA_POINT, p);
			setResult(RESULT_OK, intent);
			finish();
		}

		@Override
		protected Geocache doInBackground(String... params) throws Exception {
			if (account.getUserName() == null || account.getUserName().length() == 0 || account.getPassword() == null || account.getPassword().length() == 0)
				throw new InvalidCredentialsException("Username or password is empty.");
			
			AbstractGeocachingApiV2 api = new LiveGeocachingApi();
			
			login(api, account);
			
			Geocache cache = null;
			try {
				List<SimpleGeocache> caches = api.getCaches(params, false, 0, 1, logCount, trackableCount);
				if (caches != null && caches.size() == 1)
					cache = (Geocache) caches.get(0);
			} catch (InvalidSessionException e) {
				account.setSession(null);
				removeSession();
				
				cache = doInBackground(params);
			} finally {
				account.setSession(api.getSession());
				if (account.getSession() != null && account.getSession().length() > 0) {
					storeSession(account.getSession());
				}
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
			
			Log.e(TAG, e.getMessage(), e);
			
			Intent intent;
			
			if (e instanceof InvalidCredentialsException) {
				intent = createErrorIntent(R.string.error_credentials, null, true);
			} else {
				String message = e.getMessage();
				if (message == null)
					message = "";
				
				intent = createErrorIntent(R.string.error, String.format("<br>%s<br> <br>Exception: %s<br>File: %s<br>Line: %d", message, e.getClass().getSimpleName(), e.getStackTrace()[0].getFileName(), e.getStackTrace()[0].getLineNumber()), false);
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
		
		private void login(AbstractGeocachingApi api, Account account) throws GeocachingApiException, InvalidCredentialsException {
			try {
				if (account.getSession() == null || account.getSession().length() == 0) {
					api.openSession(account.getUserName(), account.getPassword());
				} else {
					api.openSession(account.getSession());
				}
			} catch (InvalidCredentialsException e) {
				Log.e(TAG, "Creditials not valid.", e);
				throw e;
			}
		}
		
		private void storeSession(String session) {
			Editor edit = prefs.edit();
			edit.putString("session", session);
			edit.commit();
		}
		
		protected void removeSession() {
			Editor edit = prefs.edit();
			edit.remove("session");
			edit.commit();
		}
	}
}
