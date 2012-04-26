package com.arcao.geocaching4locus;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import menion.android.locus.addon.publiclib.DisplayData;
import menion.android.locus.addon.publiclib.LocusDataMapper;
import menion.android.locus.addon.publiclib.geoData.PointsData;
import menion.android.locus.addon.publiclib.utils.RequiredVersionMissingException;

import org.acra.ErrorReporter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.arcao.geocaching.api.GeocachingApi;
import com.arcao.geocaching.api.data.Geocache;
import com.arcao.geocaching.api.exception.GeocachingApiException;
import com.arcao.geocaching.api.exception.InvalidCredentialsException;
import com.arcao.geocaching.api.exception.InvalidSessionException;
import com.arcao.geocaching.api.impl.LiveGeocachingApi;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.constants.PrefConstants;
import com.arcao.geocaching4locus.util.Account;
import com.arcao.geocaching4locus.util.AccountPreference;
import com.arcao.geocaching4locus.util.LocusTesting;
import com.arcao.geocaching4locus.util.Throwables;
import com.arcao.geocaching4locus.util.UserTask;
import com.arcao.wherigoservice.api.WherigoService;
import com.arcao.wherigoservice.api.WherigoServiceImpl;

public class ImportActivity extends Activity {
	private final static String TAG = "G4L|ImportActivity";
	protected final static Pattern CACHE_CODE_PATTERN = Pattern.compile("(GC[A-HJKMNPQRTV-Z0-9]+)", Pattern.CASE_INSENSITIVE); 
	protected final static Pattern GUID_PATTERN = Pattern.compile("guid=([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})", Pattern.CASE_INSENSITIVE);
	protected ImpotTask task = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (!LocusTesting.isLocusInstalled(this)) {
			LocusTesting.showLocusMissingError(this);
			return;
		}
		
		if (getIntent().getData() == null) {
			Log.e(TAG, "Data uri is null!!!");
			finish();
			return;
		}
		
		String url = getIntent().getDataString();
		
		Matcher m = CACHE_CODE_PATTERN.matcher(url);
		if (!m.find()) {
			m = GUID_PATTERN.matcher(url);
			if (!m.find()) {
				Log.e(TAG, "Cache code / guid not found in url: " + url);
				Toast.makeText(this, "Cache code or GUID isn't found in URL: " + url, Toast.LENGTH_LONG).show();
				finish();
				return;
			}
		}
		
		String cacheId = m.group(1);
		
		Log.i(TAG, "Starting import task for " + cacheId);
		task = new ImpotTask();
		task.execute(cacheId);
	}
	
	class ImpotTask extends UserTask<String, Void, Geocache> implements OnClickListener {
		private ProgressDialog dialog;
		private SharedPreferences prefs;
		private Account account;
		private int logCount;
		private int trackableCount;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			prefs = PreferenceManager.getDefaultSharedPreferences(ImportActivity.this);
			
		
			logCount = prefs.getInt(PrefConstants.DOWNLOADING_COUNT_OF_LOGS, 5);
			trackableCount = prefs.getInt(PrefConstants.DOWNLOADING_COUNT_OF_TRACKABLES, 10);
						
			account = AccountPreference.get(ImportActivity.this);
			
			dialog = new ProgressDialog(ImportActivity.this);
			dialog.setIndeterminate(true);
			dialog.setMessage(getResources().getText(R.string.import_cache_progress));
			dialog.setButton(getResources().getText(R.string.cancel_button), this);
			dialog.show();
		}	
		
		@Override
		protected void onPostExecute(Geocache result) {
			super.onPostExecute(result);
			
			if (dialog != null && dialog.isShowing())
				dialog.dismiss();
			
			if (result != null) {			
				PointsData pointsData = new PointsData(TAG);
				pointsData.addPoint(LocusDataMapper.toLocusPoint(ImportActivity.this, result));
				
				try {
					DisplayData.sendData(ImportActivity.this, pointsData, true);
				} catch (RequiredVersionMissingException e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}
			
			finish();
		}

		@Override
		protected Geocache doInBackground(String... params) throws Exception {
			if (account.getUserName() == null || account.getUserName().length() == 0 || account.getPassword() == null || account.getPassword().length() == 0)
				throw new InvalidCredentialsException("Username or password is empty.");
			
			// if it's guid we need to convert to cache code
			for (int i = 0; i < params.length; i++) {
				if (!CACHE_CODE_PATTERN.matcher(params[i]).find()) {
					WherigoService wherigoService = new WherigoServiceImpl();
					params[i] = wherigoService.getCacheCodeFromGuid(params[i]);
				}
			}
			
			GeocachingApi api = new LiveGeocachingApi(AppConstants.CONSUMER_KEY, AppConstants.LICENCE_KEY);
			
			Geocache cache = null;
			try {
				login(api, account);
				cache = api.getCache(params[0], logCount, trackableCount);
			} catch (InvalidSessionException e) {
				account.setSession(null);
				AccountPreference.updateSession(ImportActivity.this, account);
				
				// try againg
				login(api, account);
				cache = api.getCache(params[0], logCount, trackableCount);
			} finally {
				account.setSession(api.getSession());
				AccountPreference.updateSession(ImportActivity.this, account);
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
			Intent intent = new Intent(ImportActivity.this, ErrorActivity.class);
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
		
		private boolean login(GeocachingApi api, Account account) throws GeocachingApiException, InvalidCredentialsException {
			try {
				if (account.getSession() == null || account.getSession().length() == 0) {
					api.openSession(account.getUserName(), account.getPassword());
					return true;
				} else {
					api.openSession(account.getSession());
					return false;
				}
			} catch (InvalidCredentialsException e) {
				Log.e(TAG, "Creditials not valid.", e);
				throw e;
			}
		}		
	}
}
