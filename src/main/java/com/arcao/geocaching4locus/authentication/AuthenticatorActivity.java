package com.arcao.geocaching4locus.authentication;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import com.arcao.geocaching4locus.AbstractActionBarActivity;
import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.authentication.helper.AuthenticatorHelper;
import com.arcao.geocaching4locus.fragment.OAuthLoginFragment;
import org.acra.ACRA;

public class AuthenticatorActivity extends AbstractActionBarActivity implements OAuthLoginFragment.DialogListener {
	private static final String TAG_DIALOG = "DIALOG";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);

		setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
		getSupportActionBar().setTitle(getTitle());
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		ACRA.getErrorReporter().putCustomData("source", "login");
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (getFragmentManager().findFragmentByTag(TAG_DIALOG) != null)
			return;

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

	public void showLoginFragment() {
		// remove previous dialog
		Fragment fragment = getFragmentManager().findFragmentById(android.R.id.content);
		if (!(fragment instanceof OAuthLoginFragment)) {
			getFragmentManager().beginTransaction()
							.replace(R.id.fragment, OAuthLoginFragment.newInstance())
							.commit();
		}
	}

	@Override
	public void onLoginFinished(Intent errorIntent) {
		if (errorIntent != null) {
			startActivity(errorIntent);
		}

		AuthenticatorHelper helper = App.get(this).getAuthenticatorHelper();
		setResult(helper.hasAccount() ? RESULT_OK : RESULT_CANCELED);
		finish();
	}

	public static Intent createIntent(Context mContext) {
		return new Intent(mContext, AuthenticatorActivity.class);
	}
}