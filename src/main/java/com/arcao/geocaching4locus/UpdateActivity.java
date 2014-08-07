package com.arcao.geocaching4locus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.arcao.geocaching4locus.authentication.AuthenticatorActivity;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.constants.PrefConstants;
import com.arcao.geocaching4locus.fragment.FullCacheDownloadConfirmDialogFragment;
import com.arcao.geocaching4locus.fragment.FullCacheDownloadConfirmDialogFragment.OnFullCacheDownloadConfirmDialogListener;
import com.arcao.geocaching4locus.fragment.UpdateDialogFragment;
import com.arcao.geocaching4locus.task.UpdateTask;

import org.acra.ACRA;

import locus.api.android.utils.LocusUtils;
import locus.api.android.utils.exceptions.RequiredVersionMissingException;
import locus.api.objects.extra.Waypoint;

public class UpdateActivity extends FragmentActivity implements UpdateTask.OnTaskListener, OnFullCacheDownloadConfirmDialogListener {
	private final static String TAG = "G4L|UpdateActivity";

	public static String PARAM_CACHE_ID = "cacheId";
	public static String PARAM_CACHE_ID__DO_NOTHING = "DO_NOTHING";
	public static String PARAM_SIMPLE_CACHE_ID = "simpleCacheId";

	private static final int REQUEST_LOGIN = 1;

	private SharedPreferences prefs;

	private boolean authenticatorActivityVisible = false;
	private boolean showUpdateDialog = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		// test if user is logged in
		if (!Geocaching4LocusApplication.getAuthenticatorHelper().hasAccount()) {
			if (savedInstanceState != null)
				authenticatorActivityVisible = savedInstanceState.getBoolean(AppConstants.STATE_AUTHENTICATOR_ACTIVITY_VISIBLE, false);

			if (!authenticatorActivityVisible) {
				startActivityForResult(AuthenticatorActivity.createIntent(this, true), REQUEST_LOGIN);
				authenticatorActivityVisible = true;
			}

			return;
		}

		String repeatUpdate = prefs.getString(
			PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW,
			PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW__UPDATE_NEVER);

		if (!PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW__UPDATE_NEVER.equals(repeatUpdate) && showBasicMemeberWarningDialog())
			return;

		showUpdateDialog = true;
	}

	@Override
	protected void onResumeFragments() {
		super.onResumeFragments();

		// play with fragments here
		if (showUpdateDialog) {
			showUpdateDialog();
			showUpdateDialog = false;
		}
	}

	protected boolean showBasicMemeberWarningDialog() {
		if (!Geocaching4LocusApplication.getAuthenticatorHelper().getRestrictions().isFullGeocachesLimitWarningRequired())
			return false;

		// check next dialog fragment
		if (getSupportFragmentManager().findFragmentByTag(UpdateDialogFragment.TAG) != null)
			return false;

		if (getSupportFragmentManager().findFragmentByTag(FullCacheDownloadConfirmDialogFragment.TAG) != null)
			return true;

		FullCacheDownloadConfirmDialogFragment.newInstance().show(getSupportFragmentManager(), FullCacheDownloadConfirmDialogFragment.TAG);
		return true;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBoolean(AppConstants.STATE_AUTHENTICATOR_ACTIVITY_VISIBLE, authenticatorActivityVisible);
	}

	public void showUpdateDialog() {
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
			}
		}

		if (cacheId == null || PARAM_CACHE_ID__DO_NOTHING.equals(cacheId)) {
			Log.e(TAG, "cacheId/simpleCacheId not found");
			setResult(RESULT_CANCELED);
			finish();
			return;
		}

		boolean updateLogs = AppConstants.UPDATE_WITH_LOGS_COMPONENT.equals(getIntent().getComponent().getClassName());

		ACRA.getErrorReporter().putCustomData("source", "update;" + cacheId);

		if (getSupportFragmentManager().findFragmentByTag(UpdateDialogFragment.TAG) != null)
			return;

		UpdateDialogFragment.newInstance(cacheId, oldPoint, updateLogs).show(getSupportFragmentManager(), UpdateDialogFragment.TAG);
	}

	@Override
	public void onTaskFinished(Intent result) {
		Log.d(TAG, "onTaskFinished result: " + result);
		setResult(result != null ? RESULT_OK : RESULT_CANCELED, result);
		finish();
	}

	@Override
	public void onUpdateState(State state, int progress) {
		// nothing here
	}

	@Override
	public void onFullCacheDownloadConfirmDialogFinished(boolean success) {
		if (success) {
			showUpdateDialog();
		} else {
			onTaskFinished(null);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// restart update process after log in
		if (requestCode == REQUEST_LOGIN) {
			authenticatorActivityVisible = false;
			if (resultCode == RESULT_OK) {
				// we can't show dialog here, we'll do it in onPostResume
				showUpdateDialog = true;
			} else {
				onTaskFinished(null);
			}
		}
	}
}
