package com.arcao.geocaching4locus.authentication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.arcao.geocaching4locus.AbstractActionBarActivity;
import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.authentication.helper.AuthenticatorHelper;
import com.arcao.geocaching4locus.fragment.OAuthLoginFragment;
import com.arcao.geocaching4locus.util.AnalyticsUtil;
import com.crashlytics.android.Crashlytics;
import timber.log.Timber;

public class AuthenticatorActivity extends AbstractActionBarActivity implements OAuthLoginFragment.DialogListener {
	private static final String TAG_DIALOG = "DIALOG";

	@Bind(R.id.toolbar) Toolbar toolbar;

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
		AuthenticatorHelper helper = App.get(this).getAuthenticatorHelper();

		if (helper.hasAccount()) {
			Crashlytics.setUserName(helper.getAccount().name);
		}

		AnalyticsUtil.actionLogin(helper.hasAccount());

		if (errorIntent != null)
			startActivity(errorIntent);

		setResult(helper.hasAccount() ? RESULT_OK : RESULT_CANCELED);
		finish();
	}

	public static Intent createIntent(Context context) {
		return new Intent(context, AuthenticatorActivity.class);
	}
}