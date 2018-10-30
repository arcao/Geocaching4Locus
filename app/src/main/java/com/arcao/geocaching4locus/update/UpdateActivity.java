package com.arcao.geocaching4locus.update;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.authentication.util.AccountManager;
import com.arcao.geocaching4locus.base.constants.AppConstants;
import com.arcao.geocaching4locus.base.constants.PrefConstants;
import com.arcao.geocaching4locus.base.util.AnalyticsUtil;
import com.arcao.geocaching4locus.update.fragment.UpdateDialogFragment;

import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import locus.api.android.utils.LocusUtils;
import locus.api.objects.extra.Point;
import timber.log.Timber;

public class UpdateActivity extends AppCompatActivity implements UpdateDialogFragment.DialogListener {
    private static final String PARAM_CACHE_ID = "cacheId";
    public static final String PARAM_SIMPLE_CACHE_ID = "simpleCacheId";

    private static final int REQUEST_SIGN_ON = 1;

    private SharedPreferences preferences;
    private AccountManager accountManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        // test if user is logged in
        accountManager = App.get(this).getAccountManager();
        if (accountManager.requestSignOn(this, REQUEST_SIGN_ON)) {
            return;
        }

        if (savedInstanceState == null)
            showUpdateDialog();
    }

    private void showUpdateDialog() {
        String cacheId = null;
        Point oldPoint = null;

        if (getIntent().hasExtra(PARAM_CACHE_ID)) {
            cacheId = getIntent().getStringExtra(PARAM_CACHE_ID);

        } else if (LocusUtils.isIntentPointTools(getIntent())) {
            try {
                Point p = LocusUtils.handleIntentPointTools(this, getIntent());

                if (p != null && p.gcData != null) {
                    cacheId = p.gcData.getCacheID();
                    oldPoint = p;
                }
            } catch (Throwable t) {
                Timber.e(t);
            }
        } else if (getIntent().hasExtra(PARAM_SIMPLE_CACHE_ID)) {
            cacheId = getIntent().getStringExtra(PARAM_SIMPLE_CACHE_ID);

            String repeatUpdate = preferences.getString(
                    PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW,
                    PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW__UPDATE_NEVER);

            if (PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW__UPDATE_NEVER.equals(repeatUpdate)) {
                Timber.d("Updating simple cache on displaying is not allowed!");
                onUpdateFinished(null);
                return;
            }
        }

        if (cacheId == null || !cacheId.toUpperCase(Locale.US).startsWith("GC")) {
            Timber.e("cacheId/simpleCacheId not found");
            onUpdateFinished(null);
            return;
        }

        Timber.i("source: update;%s", cacheId);

        boolean updateLogs = AppConstants.UPDATE_WITH_LOGS_COMPONENT.equals(getIntent().getComponent() != null ? getIntent().getComponent().getClassName() : null);

        AnalyticsUtil.actionUpdate(oldPoint != null, updateLogs, accountManager.isPremium());

        UpdateDialogFragment.newInstance(cacheId, oldPoint, updateLogs).show(getSupportFragmentManager(), UpdateDialogFragment.FRAGMENT_TAG);
    }

    @Override
    public void onUpdateFinished(Intent intent) {
        Timber.d("onUpdateFinished intent: %s", intent);
        setResult(intent != null ? RESULT_OK : RESULT_CANCELED, intent);
        finish();
    }

    @Override
    public void onUpdateError(Intent intent) {
        Timber.d("onUpdateError intent: %s", intent);
        startActivity(intent);
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

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
