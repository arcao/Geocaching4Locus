package com.arcao.geocaching4locus;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;
import com.arcao.geocaching4locus.authentication.AuthenticatorActivity;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.fragment.ImportDialogFragment;
import com.arcao.geocaching4locus.task.ImportTask.OnTaskFinishedListener;
import com.arcao.geocaching4locus.util.LocusTesting;
import org.acra.ACRA;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImportActivity extends FragmentActivity implements OnTaskFinishedListener {
	private final static String TAG = "G4L|ImportActivity";
	public final static Pattern CACHE_CODE_PATTERN = Pattern.compile("(GC[A-HJKMNPQRTV-Z0-9]+)", Pattern.CASE_INSENSITIVE);
	public final static Pattern GUID_PATTERN = Pattern.compile("guid=([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})", Pattern.CASE_INSENSITIVE);

	private static final int REQUEST_LOGIN = 1;

	private boolean authenticatorActivityVisible = false;
	private boolean showImportDialog = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!LocusTesting.isLocusInstalled(this)) {
			LocusTesting.showLocusMissingError(this);
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

		showImportDialog = true;
	}

	@Override
	protected void onResumeFragments() {
		super.onResumeFragments();

		// play with fragments here
		if (showImportDialog) {
			showImportDialog();
			showImportDialog = false;
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBoolean(AppConstants.STATE_AUTHENTICATOR_ACTIVITY_VISIBLE, authenticatorActivityVisible);
	}

	protected void showImportDialog() {
		if (getIntent().getData() == null) {
			Log.e(TAG, "Data uri is null!!!");
			finish();
			return;
		}

		String url = getIntent().getDataString();

		Matcher m = CACHE_CODE_PATTERN.matcher(url);
		if (!m.find()) {
			m = GUID_PATTERN.matcher(url);
			if (!m.find()) {
				Log.e(TAG, "Cache code / guid not found in url: " + url);
				Toast.makeText(this, "Cache code or GUID isn't found in URL: " + url, Toast.LENGTH_LONG).show();
				finish();
				return;
			}
		}

		String cacheId = m.group(1);

		ACRA.getErrorReporter().putCustomData("source", "import;" + cacheId);

		if (getSupportFragmentManager().findFragmentByTag(ImportDialogFragment.TAG) != null)
			return;

		ImportDialogFragment.newInstance(cacheId).show(getSupportFragmentManager(), ImportDialogFragment.TAG);
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
				// we can't show dialog here, we'll do it in onResumeFragments
				showImportDialog = true;
			} else {
				onTaskFinished(false);
			}
		}
	}
}
