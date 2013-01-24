package com.arcao.geocaching4locus.authentication;

import org.acra.ErrorReporter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.arcao.geocaching4locus.fragment.OAuthLoginDialogFragment;
import com.arcao.geocaching4locus.fragment.OAuthLoginDialogFragment.OnTaskFinishedListener;

public class AuthenticatorActivity extends FragmentActivity implements OnTaskFinishedListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
			
		ErrorReporter.getInstance().putCustomData("source", "login");
		
		OAuthLoginDialogFragment fragment = (OAuthLoginDialogFragment) getSupportFragmentManager().findFragmentByTag(OAuthLoginDialogFragment.TAG);
		if (fragment == null)
			fragment = OAuthLoginDialogFragment.newInstance();
			
		fragment.show(getSupportFragmentManager(), OAuthLoginDialogFragment.TAG);
	}

	@Override
	public void onTaskFinished(Intent errorIntent) {
		if (errorIntent != null) {
			startActivity(errorIntent);
		}
		
		finish();
	}

}