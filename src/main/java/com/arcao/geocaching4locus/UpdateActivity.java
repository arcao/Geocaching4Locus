package com.arcao.geocaching4locus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.constants.PrefConstants;
import com.arcao.geocaching4locus.fragment.dialog.UpdateDialogFragment;
import com.arcao.geocaching4locus.util.AnalyticsUtil;
import locus.api.android.utils.LocusUtils;
import locus.api.android.utils.exceptions.RequiredVersionMissingException;
import locus.api.objects.extra.Waypoint;
import timber.log.Timber;

import java.util.Locale;

public class UpdateActivity extends AppCompatActivity implements UpdateDialogFragment.DialogListener {
	private static final String PARAM_CACHE_ID = "cacheId";
	public static final String PARAM_SIMPLE_CACHE_ID = "simpleCacheId";

	private static final int REQUEST_SIGN_ON = 1;

	private SharedPreferences mPrefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		// test if user is logged in
		if (App.get(this).getAuthenticatorHelper().requestSignOn(this, REQUEST_SIGN_ON)) {
			return;
		}

		if (savedInstanceState == null)
			showUpdateDialog();
	}

	public void showUpdateDialog() {
		String cacheId = null;
		Waypoint oldPoint = null;

		if (getIntent().hasExtra(PARAM_CACHE_ID)) {
			cacheId = getIntent().getStringExtra(PARAM_CACHE_ID);

		} else if (LocusUtils.isIntentPointTools(getIntent())) {
			try {
				Waypoint p = LocusUtils.handleIntentPointTools(this, getIntent());

				if (p != null && p.gcData != null) {
					cacheId = p.gcData.getCacheID();
					oldPoint = p;
				}
			} catch (RequiredVersionMissingException e) {
				Timber.e(e, e.getMessage());
			}
		} else if (getIntent().hasExtra(PARAM_SIMPLE_CACHE_ID)) {
			cacheId = getIntent().getStringExtra(PARAM_SIMPLE_CACHE_ID);

			String repeatUpdate = mPrefs.getString(
					PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW,
					PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW__UPDATE_NEVER);

			if (PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW__UPDATE_NEVER.equals(repeatUpdate)) {
				Timber.i("Updating simple cache on displaying is not allowed!");
				onUpdateFinished(null);
				return;
			}
		}

		if (cacheId == null || !cacheId.toUpperCase(Locale.US).startsWith("GC")) {
			Timber.e("cacheId/simpleCacheId not found");
			onUpdateFinished(null);
			return;
		}

		Timber.i("source: update;" + cacheId);

		boolean updateLogs = AppConstants.UPDATE_WITH_LOGS_COMPONENT.equals(getIntent().getComponent().getClassName());

		AnalyticsUtil.actionUpdate(oldPoint != null, updateLogs,
				App.get(this).getAuthenticatorHelper().getRestrictions().isPremiumMember());

		UpdateDialogFragment.newInstance(cacheId, oldPoint, updateLogs).show(getFragmentManager(), UpdateDialogFragment.FRAGMENT_TAG);
	}

	@Override
	public void onUpdateFinished(Intent result) {
		Timber.d("onUpdateFinished result: " + result);
		setResult(result != null ? RESULT_OK : RESULT_CANCELED, result);
		finish();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode,resultCode,data);

		// restart update process after log in
		if (requestCode == REQUEST_SIGN_ON) {
			if (resultCode == RESULT_OK) {
				showUpdateDialog();
			} else {
				onUpdateFinished(null);
			}
		}
	}
}
