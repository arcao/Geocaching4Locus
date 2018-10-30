package com.arcao.geocaching4locus.weblink;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.authentication.util.AccountManager;
import com.arcao.geocaching4locus.base.AbstractActionBarActivity;
import com.arcao.geocaching4locus.base.util.IntentUtil;
import com.arcao.geocaching4locus.error.ErrorActivity;
import com.arcao.geocaching4locus.weblink.fragment.RefreshWebLinkDialogFragment;

import androidx.annotation.Nullable;
import locus.api.android.utils.LocusUtils;
import locus.api.android.utils.exceptions.RequiredVersionMissingException;
import locus.api.objects.extra.Point;
import timber.log.Timber;

public abstract class AbstractWebLinkActivity extends AbstractActionBarActivity implements RefreshWebLinkDialogFragment.DialogListener {
    private static final int REQUEST_SIGN_ON = 1;

    protected abstract Uri getWebLink(Point point);

    protected boolean isRefreshRequired(Point point) {
        return false;
    }

    protected boolean isPremiumMemberRequired() {
        return false;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final AccountManager accountManager = App.get(this).getAccountManager();

        if (isPremiumMemberRequired() && !accountManager.isPremium()) {
            startActivity(new ErrorActivity.IntentBuilder(this).message(R.string.error_premium_feature).build());
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        try {
            if (LocusUtils.isIntentPointTools(getIntent())) {
                Point point = LocusUtils.handleIntentPointTools(this, getIntent());

                if (!isRefreshRequired(point)) {
                    showWebPage(point);
                    return;
                }

                // test if user is logged in
                if (accountManager.requestSignOn(this, REQUEST_SIGN_ON)) {
                    return;
                }

                showRefreshDialog();
            }
        } catch (Exception e) {
            Timber.e(e);
            setResult(RESULT_CANCELED);
            finish();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // restart update process after log in
        if (requestCode == REQUEST_SIGN_ON) {
            if (resultCode == RESULT_OK) {
                showRefreshDialog();
            } else {
                setResult(RESULT_CANCELED);
                finish();
            }
        }
    }

    @Override
    public void onRefreshFinished(Point point) {
        if (point == null) {
            setResult(RESULT_CANCELED);
            finish();
        }

        showWebPage(point);
    }

    @Override
    public void onRefreshError(Intent intent) {
        startActivity(intent);
        setResult(RESULT_CANCELED);
        finish();
    }

    private void showRefreshDialog() {
        try {
            Point p = LocusUtils.handleIntentPointTools(this, getIntent());
            RefreshWebLinkDialogFragment.newInstance(p.gcData.getCacheID())
                    .show(getSupportFragmentManager(), RefreshWebLinkDialogFragment.FRAGMENT_TAG);
        } catch (RequiredVersionMissingException e) {
            Timber.e(e);
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    private void showWebPage(Point point) {
        setResult(IntentUtil.showWebPage(this, getWebLink(point)) ? RESULT_OK : RESULT_CANCELED);
        finish();
    }
}
