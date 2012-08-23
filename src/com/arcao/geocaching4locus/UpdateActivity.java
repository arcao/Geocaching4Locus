package com.arcao.geocaching4locus;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import menion.android.locus.addon.publiclib.DisplayDataExtended;
import menion.android.locus.addon.publiclib.LocusDataMapper;
import menion.android.locus.addon.publiclib.LocusIntents;
import menion.android.locus.addon.publiclib.geoData.Point;
import menion.android.locus.addon.publiclib.geoData.PointsData;

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
import com.arcao.geocaching.api.impl.live_geocaching_api.filter.CacheCodeFilter;
import com.arcao.geocaching.api.impl.live_geocaching_api.filter.Filter;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.constants.PrefConstants;
import com.arcao.geocaching4locus.provider.DataStorageProvider;
import com.arcao.geocaching4locus.util.UserTask;

public class UpdateActivity extends Activity {
	private final static String TAG = "G4L|UpdateActivity";
	
	public static String PARAM_CACHE_ID = "cacheId";
	public static String PARAM_SIMPLE_CACHE_ID = "simpleCacheId";
	
	public static final int DIALOG_PROGRESS_ID = 0;
	
	private UpdateTask task;
	
	protected List<Point> oldPoints;
	protected boolean fromPointsScreen;
	protected int count;
	protected ProgressDialog pd;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
	
		oldPoints = new ArrayList<Point>();
		String[] cacheId = new String[0];
		fromPointsScreen = false;
		
		if (getIntent().hasExtra(PARAM_CACHE_ID)) {
			cacheId = new String[] { 
					getIntent().getStringExtra(PARAM_CACHE_ID)
			};
			oldPoints.add(null);
			
		} else if (LocusIntents.isIntentOnPointAction(getIntent())) {
			Point p = LocusIntents.handleIntentOnPointAction(getIntent()); 
		
			if (p != null && p.getGeocachingData() != null) {
				cacheId = new String[] { 
						p.getGeocachingData().cacheID 
				};
				oldPoints.add(p);
			}
			
    } else if (LocusIntents.isIntentPointsScreenTools(getIntent())) {
    	fromPointsScreen = true;
    	
      ArrayList<PointsData> pointsData = LocusIntents.handleIntentPointsScreenTools(getIntent());
      
      // remove points without geocaching data
      if (pointsData != null && pointsData.size() > 0) {
        for(PointsData data : pointsData) {
          for (Point p : data.getPoints()) {
            if (p.getGeocachingData() != null) {
              oldPoints.add(p);
            }
          }
        }
      }

      cacheId = new String[oldPoints.size()];
      
      for (int i = 0; i < oldPoints.size(); i++) {
				cacheId[i] = oldPoints.get(i).getGeocachingData().cacheID;
			}

		} else if (getIntent().hasExtra(PARAM_SIMPLE_CACHE_ID)) {
			cacheId = new String[] { getIntent().getStringExtra(
					PARAM_SIMPLE_CACHE_ID) };
			oldPoints.add(null);
			
			String repeatUpdate = prefs
					.getString(
							PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW,
							PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW__UPDATE_NEVER);

			if (PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW__UPDATE_NEVER
					.equals(repeatUpdate)) {
				Log.i(TAG, "Updating simple cache on dispaying is not allowed!");
				setResult(RESULT_CANCELED);
				finish();
				return;
			} else if (PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW__UPDATE_ONCE
					.equals(repeatUpdate)) {
				Point p = DisplayDataExtended.loadGeocacheFromCache(this,
						cacheId[0]);
				if (p != null) {
					Log.i(TAG, "Found cache file for: " + cacheId);
					setResult(RESULT_OK,
							LocusIntents.prepareResultExtraOnDisplayIntent(p,
									false));
					finish();
					return;
				}
			}
		}

		count = cacheId.length;

		if (count == 0) {
			Log.e(TAG, "cacheId/simpleCacheId not found");
			setResult(RESULT_CANCELED);
			finish();
			return;
		}

		ErrorReporter.getInstance().putCustomData("source",
				"update;" + Arrays.toString(cacheId));

		if ((task = (UpdateTask) getLastNonConfigurationInstance()) == null) {
			Log.i(TAG, "Starting update task for " + Arrays.toString(cacheId));
			task = new UpdateTask(this);
			task.execute(cacheId);
		} else {
			Log.i(TAG, "Restarting update task for " + Arrays.toString(cacheId));
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
				dialog.setMax(count);
				if (count == 1) {
				  dialog.setMessage(getText(R.string.update_cache_progress));
				  dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				} else {
				  dialog.setMessage(getText(R.string.update_caches_progress));
				  dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				}
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
	
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
			case DIALOG_PROGRESS_ID:
				pd = (ProgressDialog) dialog;
			default:
					super.onPrepareDialog(id, dialog);
		}
	}
	
	protected void onUpdateProgress(int current) {
		if (pd == null)
			showDialog(DIALOG_PROGRESS_ID);
			
		pd.setProgress(current);
	}

	
	static class UpdateTask extends UserTask<String, Integer, List<Geocache>> {
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
			
			if (activity.isFinishing()) {
				cancel(true);
				return;
			}
			
			activity.showDialog(DIALOG_PROGRESS_ID);
			
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
			
			logCount = prefs.getInt(PrefConstants.DOWNLOADING_COUNT_OF_LOGS, 5);
			replaceCache = PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW__UPDATE_ONCE.equals(prefs.getString(PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW, PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW__UPDATE_ONCE));								
		}	
		
		@Override
		protected void onPostExecute(List<Geocache> result) {
			super.onPostExecute(result);
			
			try {
				activity.dismissDialog(DIALOG_PROGRESS_ID);
			} catch (IllegalArgumentException ex) {}
			
			if (result == null) {
				activity.setResult(RESULT_CANCELED);
				activity.finish();
				return;
			}
			
			ArrayList<PointsData> pointDataCollection = new ArrayList<PointsData>();
			PointsData points = new PointsData("G4L");
			
			for (int i = 0; i < result.size(); i++) {
				Point p = LocusDataMapper.toLocusPoint(activity, result.get(i));
				
				// if updated cache doesn't exist use old
				if (p == null) {
					p = activity.oldPoints.get(i);
				}
				
				p = LocusDataMapper.mergePoints(activity, p, activity.oldPoints.get(i));
			
				if (replaceCache) {
					DisplayDataExtended.storeGeocacheToCache(activity, p);
				}
				
				if (!activity.fromPointsScreen) {
					activity.setResult(RESULT_OK, LocusIntents.prepareResultExtraOnDisplayIntent(p, replaceCache));
					activity.finish();
					return;
				}
				
				if (points.getPoints().size() >= 50) {
					pointDataCollection.add(points);
					points = new PointsData("G4L");
				}

				points.addPoint(p);
			}
			
			if (!points.getPoints().isEmpty())
				pointDataCollection.add(points);

			Intent intent = null;
			
			// send data via file if is possible
			File file = DisplayDataExtended.getCacheFileName(activity);
			if (file != null) {
				intent = DisplayDataExtended.prepareDataFile(pointDataCollection, file);
			} else {
				intent = DisplayDataExtended.prepareDataCursor(pointDataCollection, DataStorageProvider.URI);
			}

			if (intent != null) {
			  intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			  DisplayDataExtended.sendData(activity, intent, true);
			}
			
			activity.finish();
			return;			
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			activity.onUpdateProgress(values[0]);
		}

		@Override
		protected List<Geocache> doInBackground(String... params) throws Exception {
			if (!Geocaching4LocusApplication.getAuthenticatorHelper().hasAccount())
				throw new InvalidCredentialsException("Account not found.");
			
			GeocachingApi api = new LiveGeocachingApi();
			
			int attempt = 0;
			int current = 0;
			int count = params.length;
			
			while (++attempt <= 2) {
				List<Geocache> result = new ArrayList<Geocache>();

				try {
					login(api);
					
					current = 0;
					int perPage = Math.min(count - current, AppConstants.CACHES_PER_REQUEST);
					
					while (current < count) {
						publishProgress(current);
						
						@SuppressWarnings({ "unchecked", "rawtypes" })
						List<Geocache> cachesToAdd = (List) api.searchForGeocaches(false, AppConstants.CACHES_PER_REQUEST, logCount, 0, new Filter[] {
								new CacheCodeFilter(getPagedCaches(params, current, AppConstants.CACHES_PER_REQUEST))
						});
						
						if (isCancelled())
							return null;
		
						if (cachesToAdd.size() == 0)
							break;
						
						result.addAll(cachesToAdd);
						
						current = current + perPage;		
					}
					Log.i(TAG, "updated caches: " + result.size());
		
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
		
		protected String[] getPagedCaches(String[] params, int current, int cachesPerRequest) {
			int count = Math.min(params.length - current, cachesPerRequest);
			
			String[] ret = new String[count];
			System.arraycopy(params,current, ret, 0, count);
			
			return ret;
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
