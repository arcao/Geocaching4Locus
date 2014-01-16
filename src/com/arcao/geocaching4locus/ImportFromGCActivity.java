package com.arcao.geocaching4locus;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import com.arcao.geocaching4locus.authentication.AuthenticatorActivity;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.fragment.FullCacheDownloadConfirmDialogFragment;
import com.arcao.geocaching4locus.fragment.FullCacheDownloadConfirmDialogFragment.OnFullCacheDownloadConfirmDialogListener;
import com.arcao.geocaching4locus.fragment.GCNumberInputDialogFragment;
import com.arcao.geocaching4locus.fragment.GCNumberInputDialogFragment.OnInputFinishedListener;
import com.arcao.geocaching4locus.fragment.ImportDialogFragment;
import com.arcao.geocaching4locus.task.ImportTask.OnTaskFinishedListener;
import com.arcao.geocaching4locus.util.LocusTesting;
import org.acra.ACRA;

public class ImportFromGCActivity extends FragmentActivity implements OnTaskFinishedListener, OnInputFinishedListener, OnFullCacheDownloadConfirmDialogListener {
	private static final String TAG = ImportFromGCActivity.class.getName();

	private static final int REQUEST_LOGIN = 1;
	private boolean authenticatorActivityVisible = false;
	private boolean showGCNumberInputDialog = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!LocusTesting.isLocusInstalled(this)) {
			LocusTesting.showLocusMissingError(this);
			return;
		}

		// if import task is running, show the import task progress dialog
		if (getSupportFragmentManager().findFragmentByTag(ImportDialogFragment.TAG) != null) {
			showGCNumberInputDialog = false;
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

		if (showBasicMemeberWarningDialog())
			return;

		showGCNumberInputDialog = true;
	}

	@Override
	protected void onResumeFragments() {
		super.onResumeFragments();

		// play with fragments here
		if (showGCNumberInputDialog) {
			showGCNumberInputDialog();
			showGCNumberInputDialog = false;
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBoolean(AppConstants.STATE_AUTHENTICATOR_ACTIVITY_VISIBLE, authenticatorActivityVisible);
	}

	protected void showGCNumberInputDialog() {
		if (getSupportFragmentManager().findFragmentByTag(GCNumberInputDialogFragment.TAG) != null)
			return;

		GCNumberInputDialogFragment.newInstance().show(getSupportFragmentManager(), GCNumberInputDialogFragment.TAG);
	}

	protected boolean showBasicMemeberWarningDialog() {
		if (!Geocaching4LocusApplication.getAuthenticatorHelper().getRestrictions().isFullGeocachesLimitWarningRequired())
			return false;

		// check next dialog fragment
		if (getSupportFragmentManager().findFragmentByTag(GCNumberInputDialogFragment.TAG) != null)
			return false;

		if (getSupportFragmentManager().findFragmentByTag(FullCacheDownloadConfirmDialogFragment.TAG) != null)
			return true;

		FullCacheDownloadConfirmDialogFragment.newInstance().show(getSupportFragmentManager(), FullCacheDownloadConfirmDialogFragment.TAG);

		return true;
	}

	protected void startImport(String cacheId) {
		ACRA.getErrorReporter().putCustomData("source", "importFromGC;" + cacheId);

		if (getSupportFragmentManager().findFragmentByTag(ImportDialogFragment.TAG) != null)
			return;

		ImportDialogFragment.newInstance(cacheId).show(getSupportFragmentManager(), ImportDialogFragment.TAG);
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
	public void onFullCacheDownloadConfirmDialogFinished(boolean success) {
		Log.d(TAG, "onFullCacheDownloadConfirmDialogFinished result: " + success);
		if (success) {
			showGCNumberInputDialog();
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
				// we can't show dialog here, we'll do it in onPostResume
				showGCNumberInputDialog = true;
			} else {
				onTaskFinished(false);
			}
		}
	}


}
