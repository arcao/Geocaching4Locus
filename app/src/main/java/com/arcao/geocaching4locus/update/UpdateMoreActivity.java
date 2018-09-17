package com.arcao.geocaching4locus.update;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.base.util.AnalyticsUtil;
import com.arcao.geocaching4locus.update.fragment.UpdateMoreDialogFragment;

import locus.api.android.utils.LocusUtils;
import timber.log.Timber;

public class UpdateMoreActivity extends AppCompatActivity implements UpdateMoreDialogFragment.DialogListener {
    private static final int REQUEST_LOGIN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // test if user is logged in
        if (App.get(this).getAccountManager().requestSignOn(this, REQUEST_LOGIN)) {
            return;
        }

        if (savedInstanceState == null)
            showUpdateMoreDialog();
    }

    private void showUpdateMoreDialog() {
        long[] pointIndexes = null;

        if (LocusUtils.isIntentPointsScreenTools(getIntent()))
            pointIndexes = LocusUtils.handleIntentPointsScreenTools(getIntent());

        if (pointIndexes == null || pointIndexes.length == 0) {
            Timber.e("No caches received");
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        AnalyticsUtil.actionUpdateMore(pointIndexes.length,
                App.get(this).getAccountManager().isPremium());

        Timber.i("source: update;count=%d", pointIndexes.length);
        UpdateMoreDialogFragment.newInstance(pointIndexes).show(getFragmentManager(), UpdateMoreDialogFragment.FRAGMENT_TAG);
    }

    @Override
    public void onUpdateFinished(boolean success) {
        Timber.d("onUpdateFinished result: %b", success);
        setResult(success ? RESULT_OK : RESULT_CANCELED);
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
        if (requestCode == REQUEST_LOGIN) {
            if (resultCode == RESULT_OK) {
                showUpdateMoreDialog();
            } else {
                onUpdateFinished(false);
            }
        }
    }
}
