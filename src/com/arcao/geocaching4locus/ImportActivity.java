package com.arcao.geocaching4locus;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import menion.android.locus.addon.publiclib.DisplayData;
import menion.android.locus.addon.publiclib.LocusDataMapper;
import menion.android.locus.addon.publiclib.geoData.PointsData;
import menion.android.locus.addon.publiclib.utils.RequiredVersionMissingException;

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
import android.widget.Toast;

import com.arcao.geocaching.api.GeocachingApi;
import com.arcao.geocaching.api.data.Geocache;
import com.arcao.geocaching.api.exception.GeocachingApiException;
import com.arcao.geocaching.api.exception.InvalidCredentialsException;
import com.arcao.geocaching.api.exception.InvalidSessionException;
import com.arcao.geocaching.api.exception.NetworkException;
import com.arcao.geocaching.api.impl.LiveGeocachingApi;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.constants.PrefConstants;
import com.arcao.geocaching4locus.util.LocusTesting;
import com.arcao.geocaching4locus.util.UserTask;
import com.arcao.wherigoservice.api.WherigoService;
import com.arcao.wherigoservice.api.WherigoServiceImpl;

public class ImportActivity extends Activity {
	private final static String TAG = "G4L|ImportActivity";
	protected final static Pattern CACHE_CODE_PATTERN = Pattern.compile("(GC[A-HJKMNPQRTV-Z0-9]+)", Pattern.CASE_INSENSITIVE); 
	protected final static Pattern GUID_PATTERN = Pattern.compile("guid=([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})", Pattern.CASE_INSENSITIVE);
	
	public static final int DIALOG_PROGRESS_ID = 0;
	
	protected ImportTask task = null;
	protected String cacheId;
	
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
		
		cacheId = m.group(1);
		
		ErrorReporter.getInstance().putCustomData("source", "import;" + cacheId);
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		
		if (cacheId == null)
			return;
		
		if ((task = (ImportTask) getLastNonConfigurationInstance()) == null) {
			Log.i(TAG, "Starting import task for " + cacheId);
			task = new ImportTask(this);
			task.execute(cacheId);
		} else {
			Log.i(TAG, "Restarting import task for " + cacheId);
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
				dialog.setMessage(getText(R.string.import_cache_progress));
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

	
	static class ImportTask extends UserTask<String, Void, Geocache> {
		private int logCount;
		private int trackableCount;
		
		private ImportActivity activity;
		
		public ImportTask(ImportActivity activity) {
			attach(activity);
		}
		
		public void attach(ImportActivity activity) {
			this.activity = activity;
			
			if (getStatus() == Status.FINISHED)
				return;
			
			activity.showDialog(DIALOG_PROGRESS_ID);
		}
		
		public void detach() {
			activity.dismissDialog(DIALOG_PROGRESS_ID);
			activity = null;
		}

		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
			
			logCount = prefs.getInt(PrefConstants.DOWNLOADING_COUNT_OF_LOGS, 5);
			trackableCount = prefs.getInt(PrefConstants.DOWNLOADING_COUNT_OF_TRACKABLES, 10);						
		}	
		
		@Override
		protected void onPostExecute(Geocache result) {
			super.onPostExecute(result);
			
			activity.dismissDialog(DIALOG_PROGRESS_ID);
			
			if (result != null) {			
				PointsData pointsData = new PointsData(TAG);
				pointsData.addPoint(LocusDataMapper.toLocusPoint(activity, result));
				
				try {
					DisplayData.sendData(activity, pointsData, true);
				} catch (RequiredVersionMissingException e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}
			
			activity.finish();
		}

		@Override
		protected Geocache doInBackground(String... params) throws Exception {
			if (!Geocaching4LocusApplication.getAuthenticatorHelper().hasAccount())
				throw new InvalidCredentialsException("Account not found.");
			
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
				login(api);
				cache = api.getCache(params[0], logCount, trackableCount);
			} catch (InvalidSessionException e) {
				Geocaching4LocusApplication.getAuthenticatorHelper().invalidateAuthToken();
				
				// try again
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

			activity.setResult(RESULT_CANCELED);
			activity.finish();
			
			activity.startActivity(intent);
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
