package com.arcao.geocaching4locus;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.arcao.geocaching4locus.fragment.dialog.ImportDialogFragment;
import com.arcao.geocaching4locus.util.LocusTesting;

import org.acra.ACRA;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

public class ImportActivity extends AppCompatActivity implements ImportDialogFragment.DialogListener {
	public final static Pattern CACHE_CODE_PATTERN = Pattern.compile("(GC[A-HJKMNPQRTV-Z0-9]+)", Pattern.CASE_INSENSITIVE);
	private final static Pattern GUID_PATTERN = Pattern.compile("guid=([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})", Pattern.CASE_INSENSITIVE);

	private static final int REQUEST_LOGIN = 1;

	private boolean mShowImportDialog = false;

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

		mShowImportDialog = true;
	}

	@Override
	protected void onResumeFragments() {
		super.onResumeFragments();

		// play with fragments here
		if (mShowImportDialog) {
			showImportDialog();
			mShowImportDialog = false;
		}
	}

	protected void showImportDialog() {
		if (getIntent().getData() == null) {
			Timber.e("Data uri is null!!!");
			finish();
			return;
		}

		String url = getIntent().getDataString();

		Matcher m = CACHE_CODE_PATTERN.matcher(url);
		if (!m.find()) {
			m = GUID_PATTERN.matcher(url);
			if (!m.find()) {
				Timber.e("Cache code / guid not found in url: " + url);
				Toast.makeText(this, "Cache code or GUID isn't found in URL: " + url, Toast.LENGTH_LONG).show();
				finish();
				return;
			}
		}

		String cacheId = m.group(1);

		ACRA.getErrorReporter().putCustomData("source", "import;" + cacheId);

		if (getFragmentManager().findFragmentByTag(ImportDialogFragment.FRAGMENT_TAG) != null)
			return;

		ImportDialogFragment.newInstance(cacheId).show(getFragmentManager(), ImportDialogFragment.FRAGMENT_TAG);
	}

	@Override
	public void onImportFinished(boolean success) {
		Timber.d("onImportFinished result: " + success);
		setResult(success ? RESULT_OK : RESULT_CANCELED);
		finish();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// restart update process after log in
		if (requestCode == REQUEST_LOGIN) {
			if (resultCode == RESULT_OK) {
				// we can't show dialog here, we'll do it in onResumeFragments
				mShowImportDialog = true;
			} else {
				onImportFinished(false);
			}
		}
	}
}
