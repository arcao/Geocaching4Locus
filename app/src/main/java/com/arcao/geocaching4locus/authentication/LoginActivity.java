package com.arcao.geocaching4locus.authentication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.authentication.fragment.BasicMembershipWarningDialogFragment;
import com.arcao.geocaching4locus.authentication.fragment.OAuthLoginFragment;
import com.arcao.geocaching4locus.authentication.util.Account;
import com.arcao.geocaching4locus.authentication.util.AccountManager;
import com.arcao.geocaching4locus.base.AbstractActionBarActivity;
import com.arcao.geocaching4locus.base.constants.CrashlyticsConstants;
import com.arcao.geocaching4locus.base.util.AnalyticsUtil;
import com.crashlytics.android.Crashlytics;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class LoginActivity extends AbstractActionBarActivity implements OAuthLoginFragment.DialogListener {
    @BindView(R.id.toolbar) Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getTitle());
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Timber.i("source: login");

        if (savedInstanceState == null)
            showLoginFragment();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showLoginFragment() {
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment, OAuthLoginFragment.newInstance())
                .commit();
    }

    @Override
    public void onLoginFinished(Intent errorIntent) {
        AccountManager helper = App.get(this).getAccountManager();
        Account account = helper.getAccount();
        boolean premiumMember = helper.isPremium();

        if (account != null) {
            Crashlytics.setBool(CrashlyticsConstants.PREMIUM_MEMBER, account.premium());
            AnalyticsUtil.setPremiumUser(this, account.premium());
        }

        AnalyticsUtil.actionLogin(account != null, premiumMember);

        setResult(account != null ? RESULT_OK : RESULT_CANCELED);

        if (errorIntent != null) {
            startActivity(errorIntent);
            finish();
            return;
        }

        if (premiumMember) {
            finish();
            return;
        }

        BasicMembershipWarningDialogFragment.newInstance().show(getFragmentManager(), BasicMembershipWarningDialogFragment.FRAGMENT_TAG);
    }

    public static Intent createIntent(Context context) {
        return new Intent(context, LoginActivity.class);
    }
}