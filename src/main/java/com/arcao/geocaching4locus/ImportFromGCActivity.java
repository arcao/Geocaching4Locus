package com.arcao.geocaching4locus;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.arcao.geocaching4locus.fragment.dialog.GCNumberInputDialogFragment;
import com.arcao.geocaching4locus.fragment.dialog.ImportDialogFragment;
import com.arcao.geocaching4locus.util.LocusTesting;
import timber.log.Timber;

public class ImportFromGCActivity extends AppCompatActivity implements ImportDialogFragment.DialogListener, GCNumberInputDialogFragment.DialogListener  {
	private static final int REQUEST_LOGIN = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!LocusTesting.isLocusInstalled(this)) {
			LocusTesting.showLocusMissingError(this);
			return;
		}

		// test if user is logged in
		if (!App.get(this).getAuthenticatorHelper().isLoggedIn(this, REQUEST_LOGIN)) {
			return;
		}

		if (savedInstanceState == null)
			showGCNumberInputDialog();
	}

	private void showGCNumberInputDialog() {
		GCNumberInputDialogFragment.newInstance().show(getFragmentManager(), GCNumberInputDialogFragment.FRAGMENT_TAG);
	}

	private void startImport(String cacheId) {
		Timber.i("source: importFromGC;" + cacheId);

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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode,data);

		// restart update process after log in
		if (requestCode == REQUEST_LOGIN) {
			if (resultCode == RESULT_OK) {
				showGCNumberInputDialog();
			} else {
				onImportFinished(false);
			}
		}
	}
}
