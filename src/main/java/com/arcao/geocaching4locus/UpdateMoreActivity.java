package com.arcao.geocaching4locus;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.arcao.geocaching4locus.fragment.dialog.UpdateMoreDialogFragment;
import locus.api.android.utils.LocusUtils;
import org.apache.commons.lang3.ArrayUtils;
import timber.log.Timber;

public class UpdateMoreActivity extends AppCompatActivity implements UpdateMoreDialogFragment.DialogListener {
	private static final int REQUEST_LOGIN = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// test if user is logged in
		if (!App.get(this).getAuthenticatorHelper().isLoggedIn(this, REQUEST_LOGIN)) {
			return;
		}

		if (savedInstanceState == null)
			showUpdateMoreDialog();
	}

	private void showUpdateMoreDialog() {
		long[] pointIndexes = null;

		if (LocusUtils.isIntentPointsScreenTools(getIntent()))
			pointIndexes = LocusUtils.handleIntentPointsScreenTools(getIntent());

		if (ArrayUtils.isEmpty(pointIndexes)) {
			Timber.e("No caches received");
			setResult(RESULT_CANCELED);
			finish();
			return;
		}

		Timber.i("source: update;count=" + pointIndexes.length);
		UpdateMoreDialogFragment.newInstance(pointIndexes).show(getFragmentManager(), UpdateMoreDialogFragment.FRAGMENT_TAG);
	}

	@Override
	public void onUpdateFinished(boolean success) {
		Timber.d("onUpdateFinished result: " + success);
		setResult(success ? RESULT_OK : RESULT_CANCELED);
		finish();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// restart update process after log in
		if (requestCode == REQUEST_LOGIN) {
			if (resultCode == RESULT_OK) {
				showUpdateMoreDialog();
			} else {
				onUpdateFinished(false);
			}
		}
	}
}
