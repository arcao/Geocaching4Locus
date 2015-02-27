package com.arcao.geocaching4locus.authentication;

import android.app.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import com.arcao.geocaching4locus.AbstractActionBarActivity;
import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.authentication.helper.AccountRestrictions;
import com.arcao.geocaching4locus.authentication.helper.AuthenticatorHelper;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.fragment.OAuthLoginFragment;
import com.arcao.geocaching4locus.fragment.dialog.AbstractDialogFragment;
import com.arcao.geocaching4locus.util.SpannedFix;
import org.acra.ACRA;

import java.lang.ref.WeakReference;

public class AuthenticatorActivity extends AbstractActionBarActivity implements OAuthLoginFragment.DialogListener {
	private static final String TAG_DIALOG = "DIALOG";

	protected final Handler handler = new Handler();

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

	public void showBasicMemberWarning() {
		// remove previous dialog
		DialogFragment fragment = (DialogFragment) getFragmentManager().findFragmentByTag(TAG_DIALOG);
		if (fragment != null)
			fragment.dismiss();

		new BasicMemberLoginWarningDialogFragment().show(getFragmentManager(), TAG_DIALOG);
	}

	@Override
	public void onLoginFinished(Intent errorIntent) {
		if (errorIntent != null) {
			startActivity(errorIntent);
		}

		AuthenticatorHelper helper = App.get(this).getAuthenticatorHelper();

		boolean hasAccount = helper.hasAccount();

		// for basic member show warning dialog about limits
		/*if (hasAccount && !helper.getRestrictions().isPremiumMember()) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					showBasicMemberWarning();
				}
			});
			return;
		}*/

		setResult(hasAccount ? RESULT_OK : RESULT_CANCELED);
		finish();
	}

	public static Intent createIntent(Context mContext) {
		return new Intent(mContext, AuthenticatorActivity.class);
	}

	public static class BasicMemberLoginWarningDialogFragment extends AbstractDialogFragment {
		protected WeakReference<Activity> activityRef;

		public BasicMemberLoginWarningDialogFragment() {
			setCancelable(false);
		}

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);

			activityRef = new WeakReference<>(activity);
		}

		@NonNull
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AccountRestrictions restrictions = App.get(getActivity()).getAuthenticatorHelper().getRestrictions();

			// apply format on a text
			int cachesPerPeriod = (int) restrictions.getMaxFullGeocacheLimit();
			int period = (int) restrictions.getFullGeocacheLimitPeriod();

			String periodString;
			if (period < AppConstants.SECONDS_PER_MINUTE) {
				periodString = getResources().getQuantityString(R.plurals.plurals_minute, period, period);
			} else {
				period /= AppConstants.SECONDS_PER_MINUTE;
				periodString = getResources().getQuantityString(R.plurals.plurals_hour, period, period);
			}

			String cacheString = getResources().getQuantityString(R.plurals.plurals_cache, cachesPerPeriod, cachesPerPeriod);

			String message = getString(R.string.basic_member_sign_on_warning_message, cacheString, periodString);

			return new AlertDialog.Builder(getActivity())
				.setTitle(R.string.basic_member_warning_title)
				.setMessage(SpannedFix.fromHtml(message))
				.setPositiveButton(R.string.ok_button, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Activity activity = activityRef.get();
						if (activity != null) {
							activity.setResult(RESULT_OK);
							activity.finish();
						}
					}
				})
				.create();
		}
	}
}