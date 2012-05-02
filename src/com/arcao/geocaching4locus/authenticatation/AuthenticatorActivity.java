package com.arcao.geocaching4locus.authenticatation;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.arcao.geocaching.api.GeocachingApi;
import com.arcao.geocaching.api.impl.LiveGeocachingApi;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.util.UserTask;

public class AuthenticatorActivity extends AccountAuthenticatorActivity {
	private static final String TAG = "G4L|AuthenticatorActivity";
	
  public static final String PARAM_USERNAME = "USERNAME"; 
	public static final String PARAM_PASSWORD = "PASSWORD";
	public static final String PARAM_AUTHTOKEN_TYPE = "AUTHTOKEN_TYPE";
	
	protected String mUsername;
	protected String mPassword;
	protected String mAuthToken;
	protected String mAuthTokenType;
	protected boolean mRequestNewAccount;
	
	protected AccountManager mAccountManager;
	
	protected EditText mUsernameEdit;
	protected EditText mPasswordEdit;
	
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		Log.i(TAG, "onCreate(" + icicle + ")");
		
		mAccountManager = AccountManager.get(this);
		
    Log.i(TAG, "loading data from Intent");
    final Intent intent = getIntent();
    
    mUsername = intent.getStringExtra(PARAM_USERNAME);
    mAuthTokenType = intent.getStringExtra(PARAM_AUTHTOKEN_TYPE);
    
    mRequestNewAccount = mUsername == null;
    
    //mConfirmCredentials = intent.getBooleanExtra(PARAM_CONFIRMCREDENTIALS, false);

    Log.i(TAG, "    request new: " + mRequestNewAccount);
    
    setContentView(R.layout.login_activity);
    
    // hide preferences button
    findViewById(R.id.image_view_separator_setting).setVisibility(View.GONE);
    findViewById(R.id.image_button_title_setting).setVisibility(View.GONE);
    
    mUsernameEdit = (EditText) findViewById(R.id.username);
    mPasswordEdit = (EditText) findViewById(R.id.password);

    if (!mRequestNewAccount) {
    	mUsernameEdit.setText(mUsername);
    	mUsernameEdit.setEnabled(false);
    }
	}
	
	
	public void onClickClose(View view) {
		finish();
	}
	
	public void onClickCancel(View view) {
		finish();
	}

	public void onClickContinue(View view) {
    mUsername = mUsernameEdit.getText().toString();   
    mPassword = mPasswordEdit.getText().toString();
    
    new AutheticateTask().execute(mUsername, mPassword);
	}

	
	protected void finishLogin() {
    Log.i(TAG, "finishLogin()");
    
    final Account account = new Account(mUsername, AccountAuthenticator.ACCOUNT_TYPE);

    if (mRequestNewAccount) {
        mAccountManager.addAccountExplicitly(account, mPassword, null);
    } else {
        mAccountManager.setPassword(account, mPassword);
    }
    
    final Intent intent = new Intent();

    intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, mUsername);
    intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, AccountAuthenticator.ACCOUNT_TYPE);
    if (mAuthTokenType != null && mAuthTokenType.equals(AccountAuthenticator.ACCOUNT_TYPE)) {
        intent.putExtra(AccountManager.KEY_AUTHTOKEN, mAuthToken);
    }
    
    setAccountAuthenticatorResult(intent.getExtras());
    setResult(RESULT_OK, intent);
    finish();
	}
	
	protected class AutheticateTask extends UserTask<String, Void, String> {
		private ProgressDialog pd;
		
		@Override
		protected void onPreExecute() {
			pd = new ProgressDialog(AuthenticatorActivity.this);
			pd.setMessage(getText(R.string.account_logging_in));
			pd.setCancelable(true);
			pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			pd.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					AutheticateTask.this.cancel(true);
					pd.dismiss();
				}
			});
			pd.show();
		}
		
		@Override
		protected String doInBackground(String... params) throws Exception {
			GeocachingApi api = new LiveGeocachingApi(AppConstants.CONSUMER_KEY, AppConstants.LICENCE_KEY);
			
			api.openSession(params[0], params[1]);
			
			return api.getSession();
		}
		
		@Override
		protected void onPostExecute(String result) {
			if (pd.isShowing())
				pd.dismiss();
			
			mAuthToken = result;
			finishLogin();
		}
		
		@Override
		protected void onException(Throwable e) {
			if (pd.isShowing())
				pd.dismiss();
			
			// TODO show error dialog
		}
	}
}
