package com.arcao.geocaching4locus.authentication;

import org.acra.ErrorReporter;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.arcao.geocaching.api.GeocachingApi;
import com.arcao.geocaching.api.exception.InvalidCredentialsException;
import com.arcao.geocaching.api.exception.NetworkException;
import com.arcao.geocaching.api.impl.LiveGeocachingApi;
import com.arcao.geocaching4locus.ErrorActivity;
import com.arcao.geocaching4locus.Geocaching4LocusApplication;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.util.UserTask;

public class AuthenticatorActivity extends AccountAuthenticatorActivity implements OnEditorActionListener {
	private static final String TAG = "G4L|AuthenticatorActivity";
	
  public static final String PARAM_USERNAME = "USERNAME"; 
	public static final String PARAM_PASSWORD = "PASSWORD";
	public static final String PARAM_AUTHTOKEN_TYPE = "AUTHTOKEN_TYPE";
	
	public static final int DIALOG_PROGRESS_ID = 0;
	public static final int DIALOG_ERROR_CREDENTIALS_ID = 1;
	public static final int DIALOG_ERROR_NETWORK_ID = 2;
	public static final int DIALOG_ERROR_WRONG_INPUT_ID = 3;
	
	protected String mUsername;
	protected String mPassword;
	protected String mAuthToken;
	protected String mAuthTokenType;
	protected boolean mRequestNewAccount;
	
	protected EditText mUsernameEdit;
	protected EditText mPasswordEdit;
	
	protected AuthenticatorTask task;
	
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
    final Intent intent = getIntent();
    
    mUsername = intent.getStringExtra(PARAM_USERNAME);
    mAuthTokenType = intent.getStringExtra(PARAM_AUTHTOKEN_TYPE);
    
    mRequestNewAccount = mUsername == null;
        
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
		
    // attach activity instance to AuthenticatorTask if is necessary
    if ((task = (AuthenticatorTask) getLastNonConfigurationInstance()) != null) {
    	task.attach(this);
    }
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DIALOG_PROGRESS_ID:
				ProgressDialog dialog = new ProgressDialog(this);
				dialog.setIndeterminate(true);
				dialog.setMessage(getText(R.string.account_logging_in));
				dialog.setButton(getText(R.string.cancel_button), new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						task.cancel(true);
					}
				});
				return dialog;
				
			case DIALOG_ERROR_CREDENTIALS_ID:
				return new AlertDialog.Builder(this)
					.setTitle(R.string.login_error_title)
					.setMessage(R.string.login_error_credentials)
					.setPositiveButton(R.string.ok_button, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					})
					.create();
				
			case DIALOG_ERROR_NETWORK_ID:
				return new AlertDialog.Builder(this)
					.setTitle(R.string.login_error_title)
					.setMessage(R.string.error_network)
					.setPositiveButton(R.string.ok_button, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					})
					.create();
			
			case DIALOG_ERROR_WRONG_INPUT_ID:
				return new AlertDialog.Builder(this)
					.setTitle(R.string.login_error_title)
					.setMessage(R.string.login_message)
					.setPositiveButton(R.string.ok_button, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					})
					.create();
				
			default:
				return super.onCreateDialog(id);
		}
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		if (task != null) {
			task.detach();
		}
		return task;
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
    
    if (mUsername.trim().length() == 0 || mPassword.trim().length() == 0) {
			showDialog(DIALOG_ERROR_WRONG_INPUT_ID);
			return;
    }
    
    task = new AuthenticatorTask(this);
    task.execute(mUsername, mPassword);
	}
	
	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_SEARCH ||
				actionId == EditorInfo.IME_ACTION_NEXT ||
        actionId == EditorInfo.IME_ACTION_DONE ||
        event.getAction() == KeyEvent.ACTION_DOWN &&
        event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
			
			if (v.equals(mUsernameEdit)) {
				mPasswordEdit.requestFocus();
				return true;
			} else if (v.equals(mPasswordEdit)) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				return true;				
			}
		}
		
		return false;
	}

	
	protected void finishLogin(String mAuthToken) {
    Log.i(TAG, "finishLogin()");
    
    task = null;
    
    final Account account = new Account(mUsername, AccountAuthenticator.ACCOUNT_TYPE);

    if (mRequestNewAccount) {
    	Geocaching4LocusApplication.getAuthenticatorHelper().addAccountExplicitly(account, mPassword);
    } else {
    	Geocaching4LocusApplication.getAuthenticatorHelper().setPassword(account, mPassword);
    }
    
    final Intent intent = new Intent();

    intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, mUsername);
    intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, AccountAuthenticator.ACCOUNT_TYPE);
    if (mAuthTokenType != null && mAuthTokenType.equals(AccountAuthenticator.ACCOUNT_TYPE)) {
    	Geocaching4LocusApplication.getAuthenticatorHelper().setAuthToken(account, mAuthTokenType, mAuthToken);
    	intent.putExtra(AccountManager.KEY_AUTHTOKEN, mAuthToken);
    }
    
    setAccountAuthenticatorResult(intent.getExtras());
    setResult(RESULT_OK, intent);
    finish();
	}
	
	static class AuthenticatorTask extends UserTask<String, Void, String> {
		private AuthenticatorActivity activity;
		
		public AuthenticatorTask(AuthenticatorActivity activity) {
			attach(activity);
		}
		
		public void attach(AuthenticatorActivity activity) {
			this.activity = activity;
			
		}
		
		public void detach() {
			activity = null;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			activity.showDialog(DIALOG_PROGRESS_ID);
		}
			
		@Override
		protected String doInBackground(String... params) throws Exception {
			GeocachingApi api = new LiveGeocachingApi(AppConstants.CONSUMER_KEY, AppConstants.LICENCE_KEY);
			
			api.openSession(params[0], params[1]);
			
			return api.getSession();
		}
		
		@Override
		protected void onPostExecute(String result) {
			activity.dismissDialog(DIALOG_PROGRESS_ID);
			activity.finishLogin(result);
		}
		
		@Override
		protected void onCancelled() {
			super.onCancelled();
			
			activity.dismissDialog(DIALOG_PROGRESS_ID);
		}

		
		@Override
		protected void onException(Throwable e) {
			activity.dismissDialog(DIALOG_PROGRESS_ID);
			
			if (isCancelled())
				return;
			
			Log.e(TAG, e.getMessage(), e);
						
			if (e instanceof InvalidCredentialsException) {
				activity.showDialog(DIALOG_ERROR_CREDENTIALS_ID);
			} else if (e instanceof NetworkException) {
				activity.showDialog(DIALOG_ERROR_NETWORK_ID);
			} else {
				ErrorReporter.getInstance().putCustomData("source", "login");
				
				String message = e.getMessage();
				if (message == null)
					message = "";
				
				activity.startActivity(ErrorActivity.createErrorIntent(activity, R.string.error, String.format("%s<br>Exception: %s", message, e.getClass().getSimpleName()), false, e));
			}
		}		
	}
}