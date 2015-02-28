package com.arcao.geocaching4locus;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import com.arcao.geocaching4locus.fragment.dialog.FullCacheDownloadConfirmDialogFragment;
import com.arcao.geocaching4locus.fragment.dialog.UpdateMoreDialogFragment;
import locus.api.android.utils.LocusUtils;
import org.acra.ACRA;
import timber.log.Timber;

public class UpdateMoreActivity extends FragmentActivity implements UpdateMoreDialogFragment.DialogListener, FullCacheDownloadConfirmDialogFragment.DialogListener {
	private static final int REQUEST_LOGIN = 1;

	private boolean mShowUpdateMoreDialog = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// test if user is logged in
		if (!App.get(this).getAuthenticatorHelper().isLoggedIn(this, REQUEST_LOGIN)) {
			return;
		}

		if (showBasicMemeberWarningDialog())
			return;

		mShowUpdateMoreDialog = true;
	}

	@Override
	protected void onResumeFragments() {
		super.onResumeFragments();

		// play with fragments here
		if (mShowUpdateMoreDialog) {
			showUpdateMoreDialog();
			mShowUpdateMoreDialog = false;
		}
	}

	private boolean showBasicMemeberWarningDialog() {
		if (!App.get(this).getAuthenticatorHelper().getRestrictions().isFullGeocachesLimitWarningRequired())
			return false;

		// check next dialog fragment
		if (getSupportFragmentManager().findFragmentByTag(UpdateMoreDialogFragment.FRAGMENT_TAG) != null)
			return false;

		if (getSupportFragmentManager().findFragmentByTag(FullCacheDownloadConfirmDialogFragment.FRAGMENT_TAG) != null)
			return true;

		FullCacheDownloadConfirmDialogFragment.newInstance().show(getFragmentManager(), FullCacheDownloadConfirmDialogFragment.FRAGMENT_TAG);
		return true;
	}

	private void showUpdateMoreDialog() {
		long[] pointIndexes = null;

		if (LocusUtils.isIntentPointsScreenTools(getIntent())) {
			pointIndexes = LocusUtils.handleIntentPointsScreenTools(getIntent());
		}

		if (pointIndexes == null || pointIndexes.length == 0) {
			Timber.e("No caches received");
			setResult(RESULT_CANCELED);
			finish();
			return;
		}

		ACRA.getErrorReporter().putCustomData("source", "update;");
		ACRA.getErrorReporter().putCustomData("count", String.valueOf(pointIndexes.length));

		if (getSupportFragmentManager().findFragmentByTag(UpdateMoreDialogFragment.FRAGMENT_TAG) != null)
			return;

		UpdateMoreDialogFragment.newInstance(pointIndexes).show(getFragmentManager(), UpdateMoreDialogFragment.FRAGMENT_TAG);
	}

	@Override
	public void onUpdateFinished(boolean success) {
		Timber.d("onUpdateFinished result: " + success);
		setResult(success ? RESULT_OK : RESULT_CANCELED);
		finish();
	}

	@Override
	public void onFullCacheDownloadConfirmDialogFinished(boolean success) {
		if (success) {
			showUpdateMoreDialog();
		} else {
			onUpdateFinished(false);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// restart update process after log in
		if (requestCode == REQUEST_LOGIN) {
			if (resultCode == RESULT_OK) {
				mShowUpdateMoreDialog = true;
			} else {
				onUpdateFinished(false);
			}
		}
	}
}
