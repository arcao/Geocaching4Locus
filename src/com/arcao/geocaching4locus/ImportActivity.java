package com.arcao.geocaching4locus;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import menion.android.locus.addon.publiclib.DisplayData;
import menion.android.locus.addon.publiclib.LocusDataMapper;
import menion.android.locus.addon.publiclib.geoData.Point;
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
import com.arcao.geocaching.api.impl.LiveGeocachingApiFactory;
import com.arcao.geocaching4locus.constants.PrefConstants;
import com.arcao.geocaching4locus.util.LocusTesting;
import com.arcao.geocaching4locus.util.UserTask;
import com.arcao.wherigoservice.api.WherigoService;
import com.arcao.wherigoservice.api.WherigoServiceException;
import com.arcao.wherigoservice.api.WherigoServiceImpl;

public class ImportActivity extends Activity {
	private final static String TAG = "G4L|ImportActivity";
	protected final static Pattern CACHE_CODE_PATTERN = Pattern.compile("(GC[A-HJKMNPQRTV-Z0-9]+)", Pattern.CASE_INSENSITIVE); 
	protected final static Pattern GUID_PATTERN = Pattern.compile("guid=([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})", Pattern.CASE_INSENSITIVE);
	
	public static final int DIALOG_PROGRESS_ID = 0;
	
	protected ImportTask task = null;
	
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
		
		ErrorReporter.getInstance().putCustomData("source", "import;" + cacheId);
		
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

	
	static class ImportTask extends UserTask<String, Void, Point> {
		private int logCount;
		
		private ImportActivity activity;
		
		public ImportTask(ImportActivity activity) {
			attach(activity);
		}
		
		public void attach(ImportActivity activity) {
			this.activity = activity;			
		}
		
		public void detach() {
			activity = null;
		}

		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			if (activity.isFinishing()) {
				cancel(true);
				return;
			}

			activity.showDialog(DIALOG_PROGRESS_ID);
			
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
			
			logCount = prefs.getInt(PrefConstants.DOWNLOADING_COUNT_OF_LOGS, 5);
		}	
		
		@Override
		protected void onPostExecute(Point result) {
			super.onPostExecute(result);
			
			try {
				activity.dismissDialog(DIALOG_PROGRESS_ID);
			} catch (IllegalArgumentException ex) {}
			
			if (result != null) {			
				PointsData pointsData = new PointsData(TAG);
				pointsData.addPoint(result);
				
				try {
					DisplayData.sendData(activity, pointsData, true);
				} catch (RequiredVersionMissingException e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}
			
			activity.finish();
		}

		@Override
		protected Point doInBackground(String... params) throws Exception {
			if (!Geocaching4LocusApplication.getAuthenticatorHelper().hasAccount())
				throw new InvalidCredentialsException("Account not found.");
			
			WherigoService wherigoService = new WherigoServiceImpl();

			// if it's guid we need to convert to cache code
			for (int i = 0; i < params.length; i++) {
				if (!CACHE_CODE_PATTERN.matcher(params[i]).find()) {
					params[i] = wherigoService.getCacheCodeFromGuid(params[i]);
				}
			}
			
			GeocachingApi api = LiveGeocachingApiFactory.create();
			
			Geocache cache = null;
			try {
				login(api);
				cache = api.getCache(params[0], logCount, 0);
			} catch (InvalidSessionException e) {
				Geocaching4LocusApplication.getAuthenticatorHelper().invalidateAuthToken();
				
				// try again
				login(api);
				cache = api.getCache(params[0], logCount, 0);
			} catch (OperationCanceledException e) {
				cancel(false);
			}
			
			if (isCancelled())
				return null;
			
			return LocusDataMapper.toLocusPoint(Geocaching4LocusApplication.getAppContext(), cache);
		}
		
		@Override
		protected void onCancelled() {
			super.onCancelled();
			
			try {
				activity.dismissDialog(DIALOG_PROGRESS_ID);
			} catch (IllegalArgumentException ex) {}
			
			activity.setResult(RESULT_CANCELED);
			activity.finish();
		}
		
		@Override
		protected void onException(Throwable e) {
			super.onException(e);

			try {
				activity.dismissDialog(DIALOG_PROGRESS_ID);
			} catch (IllegalArgumentException ex) {}
			
			if (isCancelled())
				return;
			
			Log.e(TAG, e.getMessage(), e);
			
			Intent intent;
			
			if (e instanceof InvalidCredentialsException) {
				intent = ErrorActivity.createErrorIntent(activity, R.string.error_credentials, null, true, null);
			} else if (e instanceof NetworkException || 
					(e instanceof WherigoServiceException && ((WherigoServiceException) e).getCode() == WherigoServiceException.ERROR_CONNECTION_ERROR)) {
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
