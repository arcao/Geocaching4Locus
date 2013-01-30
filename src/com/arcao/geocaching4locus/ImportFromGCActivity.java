package com.arcao.geocaching4locus;

import org.acra.ErrorReporter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.arcao.geocaching4locus.authentication.AuthenticatorActivity;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.fragment.GCNumberInputDialogFragment;
import com.arcao.geocaching4locus.fragment.GCNumberInputDialogFragment.OnInputFinishedListener;
import com.arcao.geocaching4locus.fragment.ImportDialogFragment;
import com.arcao.geocaching4locus.task.ImportTask.OnTaskFinishedListener;
import com.arcao.geocaching4locus.util.LocusTesting;

public class ImportFromGCActivity extends FragmentActivity implements OnTaskFinishedListener, OnInputFinishedListener {
	private static final String TAG = ImportFromGCActivity.class.getName();

	private static final int REQUEST_LOGIN = 1; 
	private boolean authenticatorActivityVisible = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (!LocusTesting.isLocusInstalled(this)) {
			LocusTesting.showLocusMissingError(this);
			return;
		}
		
		// if import task is running, show the import task progress dialog
		ImportDialogFragment fragment = (ImportDialogFragment) getSupportFragmentManager().findFragmentByTag(ImportDialogFragment.TAG);
		if (fragment != null) {
			fragment.show(getSupportFragmentManager(), ImportDialogFragment.TAG);
			return;
		}
		
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
		
		showGCNumberInputDialog();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putBoolean(AppConstants.STATE_AUTHENTICATOR_ACTIVITY_VISIBLE, authenticatorActivityVisible); 
	}
	
	protected void showGCNumberInputDialog() {
		GCNumberInputDialogFragment fragment = (GCNumberInputDialogFragment) getSupportFragmentManager().findFragmentByTag(GCNumberInputDialogFragment.TAG);
		if (fragment == null) {
			fragment = GCNumberInputDialogFragment.newInstance();
		}
		
		fragment.show(getSupportFragmentManager(), GCNumberInputDialogFragment.TAG);
	}
	
	protected void startImport(String cacheId) {
		ErrorReporter.getInstance().putCustomData("source", "importFromGC;" + cacheId);
		
		ImportDialogFragment fragment = (ImportDialogFragment) getSupportFragmentManager().findFragmentByTag(ImportDialogFragment.TAG);
		if (fragment == null)
			fragment = ImportDialogFragment.newInstance(cacheId);
			
		fragment.show(getSupportFragmentManager(), ImportDialogFragment.TAG);
	}
	
	@Override
	public void onInputFinished(String input) {
		if (input != null) {
			startImport(input);
		} else {
			onTaskFinished(false);
		}
	}
	
	@Override
	public void onTaskFinished(boolean success) {
		Log.d(TAG, "onTaskFinished result: " + success);
		setResult(success ? RESULT_OK : RESULT_CANCELED);
		finish();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// restart update process after log in
		if (requestCode == REQUEST_LOGIN) {
			authenticatorActivityVisible = false;
			
			if (resultCode == RESULT_OK) {
				showGCNumberInputDialog();
			} else {
				onTaskFinished(false);
			}
		}
	}


}
