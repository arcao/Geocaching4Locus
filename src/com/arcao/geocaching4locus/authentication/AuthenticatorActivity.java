package com.arcao.geocaching4locus.authentication;

import java.lang.ref.WeakReference;

import org.acra.ErrorReporter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.arcao.geocaching4locus.Geocaching4LocusApplication;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.fragment.AbstractDialogFragment;
import com.arcao.geocaching4locus.fragment.OAuthLoginDialogFragment;
import com.arcao.geocaching4locus.fragment.OAuthLoginDialogFragment.OnTaskFinishedListener;

public class AuthenticatorActivity extends FragmentActivity implements OnTaskFinishedListener {
	protected static final String PARAM_SHOW_WIZARD = "SHOW_WIZARD";
	protected static final String TAG_DIALOG = "dialog";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
			
		ErrorReporter.getInstance().putCustomData("source", "login");
		
		AbstractDialogFragment fragment = (AbstractDialogFragment) getSupportFragmentManager().findFragmentByTag(TAG_DIALOG);
		if (fragment != null) {
			fragment.show(getSupportFragmentManager(), TAG_DIALOG);
			return;
		}

		if (getIntent().getBooleanExtra(PARAM_SHOW_WIZARD, false)) {
			showWizardDialog();
			return;
		}
		
		showLoginDialog();
	}
	
	public void showLoginDialog() {
		// remove previous dialog
		AbstractDialogFragment fragment = (AbstractDialogFragment) getSupportFragmentManager().findFragmentByTag(TAG_DIALOG);
		if (fragment != null)
			fragment.dismiss();
		
		OAuthLoginDialogFragment.newInstance().show(getSupportFragmentManager(), TAG_DIALOG);
	}
	
	public void showWizardDialog() {
		new WizardDialogFragment().show(getSupportFragmentManager(), TAG_DIALOG);
	}

	@Override
	public void onTaskFinished(Intent errorIntent) {
		if (errorIntent != null) {
			startActivity(errorIntent);
		}
		
		setResult(Geocaching4LocusApplication.getAuthenticatorHelper().hasAccount() ? RESULT_OK : RESULT_CANCELED);
		finish();
	}

	
	public static Intent createIntent(Context mContext, boolean mShowWizard) {
		Intent intent = new Intent(mContext, AuthenticatorActivity.class);
		intent.putExtra(PARAM_SHOW_WIZARD, mShowWizard);
		
		return intent;
	}
	
	protected static class WizardDialogFragment extends AbstractDialogFragment {
		protected WeakReference<AuthenticatorActivity> activityRef;
		
		public WizardDialogFragment() {
			setCancelable(false);
		}
		
		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			
			activityRef = new WeakReference<AuthenticatorActivity>((AuthenticatorActivity) activity);
		}
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return new AlertDialog.Builder(getActivity())
				.setTitle(R.string.login_required_title)
				.setMessage(R.string.login_required_message)
				.setPositiveButton(R.string.button_login_start, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						AuthenticatorActivity activity = activityRef.get();
						if (activity != null)
							activity.showLoginDialog();
					}
				})
				.setNegativeButton(R.string.cancel_button, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						AuthenticatorActivity activity = activityRef.get();
						if (activity != null)
							activity.onTaskFinished(null);
					}
				})
				.create();
		}
		
	}
}