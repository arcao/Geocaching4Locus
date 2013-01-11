package com.arcao.geocaching4locus;

import locus.api.android.utils.LocusUtils;
import locus.api.android.utils.RequiredVersionMissingException;
import locus.api.objects.extra.Waypoint;

import org.acra.ErrorReporter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.arcao.geocaching4locus.constants.PrefConstants;
import com.arcao.geocaching4locus.fragment.UpdateDialogFragment;
import com.arcao.geocaching4locus.task.UpdateTask.OnTaskFinishedListener;

public class UpdateActivity extends FragmentActivity implements OnTaskFinishedListener {
	private final static String TAG = "G4L|UpdateActivity";
	
	public static String PARAM_CACHE_ID = "cacheId";
	public static String PARAM_CACHE_ID__DO_NOTHING = "DO_NOTHING";
	public static String PARAM_SIMPLE_CACHE_ID = "simpleCacheId";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
	
		String cacheId = null;
		Waypoint oldPoint = null;

		if (getIntent().hasExtra(PARAM_CACHE_ID)) {
			cacheId = getIntent().getStringExtra(PARAM_CACHE_ID);
			oldPoint = null;
			
		} else if (LocusUtils.isIntentPointTools(getIntent())) {
			Waypoint p = null;
			
			try {
				p = LocusUtils.handleIntentPointTools(this, getIntent());
			} catch (RequiredVersionMissingException e) {
				Log.e(TAG, e.getMessage(), e);
			}		

			if (p != null && p.getGeocachingData() != null) {
				cacheId = p.gcData.getCacheID(); 
				oldPoint = p;
			}
			
		} else if (getIntent().hasExtra(PARAM_SIMPLE_CACHE_ID)) {
			cacheId = getIntent().getStringExtra(PARAM_SIMPLE_CACHE_ID);
			oldPoint = null;
			
			String repeatUpdate = prefs.getString(
							PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW,
							PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW__UPDATE_NEVER);

			if (PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW__UPDATE_NEVER.equals(repeatUpdate)) {
				Log.i(TAG, "Updating simple cache on dispaying is not allowed!");
				setResult(RESULT_CANCELED);
				finish();
				return;
			}/* else if (PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW__UPDATE_ONCE.equals(repeatUpdate)) {
				Waypoint p = ActionDisplayPointsExtended.loadGeocacheFromCache(this, cacheId);
				if (p != null) {
					Log.i(TAG, "Found cache file for: " + cacheId);
					setResult(RESULT_OK, LocusUtils.prepareResultExtraOnDisplayIntent(p, false));
					finish();
					return;
				}
			}*/
		}

		if (cacheId == null || PARAM_CACHE_ID__DO_NOTHING.equals(cacheId)) {
			Log.e(TAG, "cacheId/simpleCacheId not found");
			setResult(RESULT_CANCELED);
			finish();
			return;
		}

		ErrorReporter.getInstance().putCustomData("source", "update;" + cacheId);

		UpdateDialogFragment fragment = (UpdateDialogFragment) getSupportFragmentManager().findFragmentByTag(UpdateDialogFragment.TAG);
		if (fragment == null)
			fragment = UpdateDialogFragment.newInstance(cacheId, oldPoint);
			
		fragment.show(getSupportFragmentManager(), UpdateDialogFragment.TAG);
	}
	
	@Override
	public void onTaskFinished(Intent result) {
		Log.d(TAG, "onTaskFinished result: " + result);
		setResult(result != null ? RESULT_OK : RESULT_CANCELED, result);
		finish();
	}
}
