package com.arcao.geocaching4locus;

import locus.api.android.utils.LocusUtils;

import org.acra.ErrorReporter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.arcao.geocaching4locus.authentication.AuthenticatorActivity;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.fragment.FullCacheDownloadConfirmDialogFragment;
import com.arcao.geocaching4locus.fragment.FullCacheDownloadConfirmDialogFragment.OnFullCacheDownloadConfirmDialogListener;
import com.arcao.geocaching4locus.fragment.UpdateMoreDialogFragment;
import com.arcao.geocaching4locus.task.UpdateMoreTask.OnTaskFinishedListener;

public class UpdateMoreActivity extends FragmentActivity implements OnTaskFinishedListener, OnFullCacheDownloadConfirmDialogListener {
	private final static String TAG = "G4L|UpdateActivity";

	private static final int REQUEST_LOGIN = 1;
	
	protected boolean authenticatorActivityVisible = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
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
		
		if (showBasicMemeberWarningDialog())
			return;
		
		showUpdateMoreDialog();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putBoolean(AppConstants.STATE_AUTHENTICATOR_ACTIVITY_VISIBLE, authenticatorActivityVisible); 
	}
	
	protected boolean showBasicMemeberWarningDialog() {
		if (!Geocaching4LocusApplication.getAuthenticatorHelper().getRestrictions().isFullGeocachesLimitWarningRequired())
			return false;
		
		// check next dialog fragment
		if (getSupportFragmentManager().findFragmentByTag(UpdateMoreDialogFragment.TAG) != null)
			return false;
		
		FullCacheDownloadConfirmDialogFragment fragment = (FullCacheDownloadConfirmDialogFragment) getSupportFragmentManager().findFragmentByTag(FullCacheDownloadConfirmDialogFragment.TAG);
		if (fragment == null) {
			fragment = FullCacheDownloadConfirmDialogFragment.newInstance();
		}
		
		fragment.show(getSupportFragmentManager(), FullCacheDownloadConfirmDialogFragment.TAG);
		
		return true;
	}
	
	protected void showUpdateMoreDialog() {
		long[] pointIndexes = null; 
		
		if (LocusUtils.isIntentPointsScreenTools(getIntent())) {
      pointIndexes = LocusUtils.handleIntentPointsScreenTools(getIntent());      
		}

		if (pointIndexes.length == 0) {
			Log.e(TAG, "No caches received");
			setResult(RESULT_CANCELED);
			finish();
			return;
		}

		ErrorReporter.getInstance().putCustomData("source", "update;");
		ErrorReporter.getInstance().putCustomData("count", String.valueOf(pointIndexes.length));

		UpdateMoreDialogFragment fragment = (UpdateMoreDialogFragment) getSupportFragmentManager().findFragmentByTag(UpdateMoreDialogFragment.TAG);
		if (fragment == null)
			fragment = UpdateMoreDialogFragment.newInstance(pointIndexes);
			
		fragment.show(getSupportFragmentManager(), UpdateMoreDialogFragment.TAG);
	}
		
	@Override
	public void onTaskFinished(boolean success) {
		Log.d(TAG, "onTaskFinished result: " + success);
		setResult(success ? RESULT_OK : RESULT_CANCELED);
		finish();
	}
	
	@Override
	public void onProgressUpdate(int count) {
		// unused
	}
	
	@Override
	public void onFullCacheDownloadConfirmDialogFinished(boolean success) {
		if (success) {
			showUpdateMoreDialog();
		} else {
			onTaskFinished(false);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// restart update process after log in
		if (requestCode == REQUEST_LOGIN) {
			authenticatorActivityVisible = false;
			
			if (resultCode == RESULT_OK) {
				showUpdateMoreDialog();
			} else {
				onTaskFinished(false);
			}
		}
	}
}
