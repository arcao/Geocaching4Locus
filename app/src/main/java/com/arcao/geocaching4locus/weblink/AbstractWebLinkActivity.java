package com.arcao.geocaching4locus.weblink;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.base.AbstractActionBarActivity;
import com.arcao.geocaching4locus.error.ErrorActivity;
import com.arcao.geocaching4locus.weblink.fragment.RefreshWebLinkDialogFragment;
import com.arcao.geocaching4locus.base.util.IntentUtil;

import locus.api.android.utils.LocusUtils;
import locus.api.android.utils.exceptions.RequiredVersionMissingException;
import locus.api.objects.extra.Waypoint;
import timber.log.Timber;

public abstract class AbstractWebLinkActivity extends AbstractActionBarActivity implements RefreshWebLinkDialogFragment.DialogListener {
    private static final int REQUEST_SIGN_ON = 1;

    protected abstract Uri getWebLink(Waypoint waypoint);

    protected boolean isRefreshRequired(Waypoint waypoint) {
        return false;
    }

    protected boolean isPremiumMemberRequired() {
        return false;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isPremiumMemberRequired() && !App.get(this).getAccountManager().isPremium()) {
            startActivity(new ErrorActivity.IntentBuilder(this).message(R.string.error_premium_feature).build());
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        try {
            if (LocusUtils.isIntentPointTools(getIntent())) {
                Waypoint waypoint = LocusUtils.handleIntentPointTools(this, getIntent());

                if (!isRefreshRequired(waypoint)) {
                    showWebPage(waypoint);
                    return;
                }

                // test if user is logged in
                if (App.get(this).getAccountManager().requestSignOn(this, REQUEST_SIGN_ON)) {
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
    public void onRefreshFinished(Waypoint waypoint) {
        if (waypoint == null) {
            setResult(RESULT_CANCELED);
            finish();
        }

        showWebPage(waypoint);
    }

    private void showRefreshDialog() {
        try {
            Waypoint p = LocusUtils.handleIntentPointTools(this, getIntent());
            RefreshWebLinkDialogFragment.newInstance(p.gcData.getCacheID())
                    .show(getFragmentManager(), RefreshWebLinkDialogFragment.FRAGMENT_TAG);
        } catch (RequiredVersionMissingException e) {
            Timber.e(e);
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    private void showWebPage(Waypoint waypoint) {
        setResult(IntentUtil.showWebPage(this, getWebLink(waypoint)) ? RESULT_OK : RESULT_CANCELED);
        finish();
    }
}
