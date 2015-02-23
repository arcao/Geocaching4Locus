package com.arcao.geocaching4locus;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.fragment.dialog.FullCacheDownloadConfirmDialogFragment;
import com.arcao.geocaching4locus.fragment.dialog.GCNumberInputDialogFragment;
import com.arcao.geocaching4locus.fragment.dialog.ImportDialogFragment;
import com.arcao.geocaching4locus.util.LocusTesting;
import org.acra.ACRA;
import timber.log.Timber;

public class ImportFromGCActivity extends FragmentActivity implements ImportDialogFragment.DialogListener, GCNumberInputDialogFragment.DialogListener, FullCacheDownloadConfirmDialogFragment.DialogListener {
	private static final int REQUEST_LOGIN = 1;
	private boolean mAuthenticatorActivityVisible = false;
	private boolean mShowGCNumberInputDialog = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!LocusTesting.isLocusInstalled(this)) {
			LocusTesting.showLocusMissingError(this);
			return;
		}

		// if import task is running, show the import task progress dialog
		if (getSupportFragmentManager().findFragmentByTag(ImportDialogFragment.FRAGMENT_TAG) != null) {
			mShowGCNumberInputDialog = false;
			return;
		}

		// test if user is logged in
		if (!App.get(this).getAuthenticatorHelper().isLoggedIn(this, REQUEST_LOGIN)) {
			return;
		}

		if (showBasicMemberWarningDialog())
			return;

		mShowGCNumberInputDialog = true;
	}

	@Override
	protected void onResumeFragments() {
		super.onResumeFragments();

		// play with fragments here
		if (mShowGCNumberInputDialog) {
			showGCNumberInputDialog();
			mShowGCNumberInputDialog = false;
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBoolean(AppConstants.STATE_AUTHENTICATOR_ACTIVITY_VISIBLE, mAuthenticatorActivityVisible);
	}

	protected void showGCNumberInputDialog() {
		if (getSupportFragmentManager().findFragmentByTag(GCNumberInputDialogFragment.FRAGMENT_TAG) != null)
			return;

		GCNumberInputDialogFragment.newInstance().show(getFragmentManager(), GCNumberInputDialogFragment.FRAGMENT_TAG);
	}

	protected boolean showBasicMemberWarningDialog() {
		if (!App.get(this).getAuthenticatorHelper().getRestrictions().isFullGeocachesLimitWarningRequired())
			return false;

		// check next dialog fragment
		if (getSupportFragmentManager().findFragmentByTag(GCNumberInputDialogFragment.FRAGMENT_TAG) != null)
			return false;

		if (getSupportFragmentManager().findFragmentByTag(FullCacheDownloadConfirmDialogFragment.FRAGMENT_TAG) != null)
			return true;

		FullCacheDownloadConfirmDialogFragment.newInstance().show(getFragmentManager(), FullCacheDownloadConfirmDialogFragment.FRAGMENT_TAG);

		return true;
	}

	protected void startImport(String cacheId) {
		ACRA.getErrorReporter().putCustomData("source", "importFromGC;" + cacheId);

		if (getSupportFragmentManager().findFragmentByTag(ImportDialogFragment.FRAGMENT_TAG) != null)
			return;

		ImportDialogFragment.newInstance(cacheId).show(getFragmentManager(), ImportDialogFragment.FRAGMENT_TAG);
	}

	@Override
	public void onInputFinished(String input) {
		if (input != null) {
			startImport(input);
		} else {
			onImportFinished(false);
		}
	}

	@Override
	public void onImportFinished(boolean success) {
		Timber.d("onImportFinished result: " + success);
		setResult(success ? RESULT_OK : RESULT_CANCELED);
		finish();
	}

	@Override
	public void onFullCacheDownloadConfirmDialogFinished(boolean success) {
		Timber.d("onFullCacheDownloadConfirmDialogFinished result: " + success);
		if (success) {
			showGCNumberInputDialog();
		} else {
			onImportFinished(false);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// restart update process after log in
		if (requestCode == REQUEST_LOGIN) {
			mAuthenticatorActivityVisible = false;

			if (resultCode == RESULT_OK) {
				// we can't show dialog here, we'll do it in onPostResume
				mShowGCNumberInputDialog = true;
			} else {
				onImportFinished(false);
			}
		}
	}


}
