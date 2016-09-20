package com.arcao.geocaching4locus.import_gc;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.base.util.AnalyticsUtil;
import com.arcao.geocaching4locus.base.util.LocusTesting;
import com.arcao.geocaching4locus.base.util.PermissionUtil;
import com.arcao.geocaching4locus.error.fragment.NoExternalStoragePermissionErrorDialogFragment;
import com.arcao.geocaching4locus.import_gc.fragment.GCNumberInputDialogFragment;
import com.arcao.geocaching4locus.import_gc.fragment.ImportDialogFragment;

import timber.log.Timber;

public class ImportFromGCActivity extends AppCompatActivity implements ImportDialogFragment.DialogListener, GCNumberInputDialogFragment.DialogListener  {
	private static final int REQUEST_SIGN_ON = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!LocusTesting.isLocusInstalled(this)) {
			LocusTesting.showLocusMissingError(this);
			return;
		}

		// test if user is logged in
		if (App.get(this).getAccountManager().requestSignOn(this, REQUEST_SIGN_ON)) {
			return;
		}

		if (savedInstanceState != null)
			return;

		if (PermissionUtil.requestExternalStoragePermission(this))
			showGCNumberInputDialog();
	}

	private void showGCNumberInputDialog() {
		GCNumberInputDialogFragment.newInstance().show(getFragmentManager(), GCNumberInputDialogFragment.FRAGMENT_TAG);
	}

	private void startImport(String cacheId) {
		Timber.i("source: importFromGC;" + cacheId);

		AnalyticsUtil.actionImportGC(App.get(this).getAccountManager().isPremium());

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
		if (requestCode == REQUEST_SIGN_ON) {
			if (resultCode == RESULT_OK) {
				if (PermissionUtil.requestExternalStoragePermission(this))
				showGCNumberInputDialog();
			} else {
				onImportFinished(false);
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		if (requestCode == PermissionUtil.REQUEST_EXTERNAL_STORAGE_PERMISSION) {
			if (PermissionUtil.verifyPermissions(grantResults)) {
				showGCNumberInputDialog();
			} else {
				NoExternalStoragePermissionErrorDialogFragment.newInstance(true).show(getFragmentManager(), NoExternalStoragePermissionErrorDialogFragment.FRAGMENT_TAG);
			}
		}
	}
}
