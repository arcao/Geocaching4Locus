package com.arcao.geocaching4locus.authentication;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;

import org.acra.ErrorReporter;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.arcao.geocaching.api.GeocachingApi;
import com.arcao.geocaching.api.data.UserProfile;
import com.arcao.geocaching.api.exception.InvalidCredentialsException;
import com.arcao.geocaching.api.exception.NetworkException;
import com.arcao.geocaching.api.impl.LiveGeocachingApi;
import com.arcao.geocaching4locus.ErrorActivity;
import com.arcao.geocaching4locus.Geocaching4LocusApplication;
import com.arcao.geocaching4locus.PreferenceActivity;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.authentication.helper.AuthenticatorHelper;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.util.DeviceInfoFactory;
import com.arcao.geocaching4locus.util.UserTask;
import com.arcao.geocaching4locus.util.UserTask.Status;

public class AuthenticatorActivity extends AccountAuthenticatorActivity {
  private static final String TAG = "G4L|AuthenticatorActivity";

  public static final String PARAM_AUTHTOKEN_TYPE = "AUTHTOKEN_TYPE";

  public static final int DIALOG_PROGRESS_ID = 0;
  public static final int DIALOG_ERROR_CREDENTIALS_ID = 1;
  public static final int DIALOG_ERROR_NETWORK_ID = 2;
  public static final int DIALOG_ERROR_WRONG_INPUT_ID = 3;

  protected OAuthTask task;
  
  String authUrl = null;

  @Override
  protected void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    
    if ((task = (OAuthTask) getLastNonConfigurationInstance()) == null || task.getStatus() == Status.FINISHED) {
      onNewIntent(getIntent());
    } else {      
      Log.i(TAG, "Restarting oAuthTask");
      task.attach(this);
    }
  }
  
  @Override
  public void onNewIntent(Intent intent) {
    super.onNewIntent(intent); 
    
    final Uri uri = intent.getData();
    
    if (uri != null && uri.toString().startsWith(AppConstants.OAUTH_CALLBACK_URL) && (task == null || task.getStatus() == Status.FINISHED)) {
      Log.i(TAG, "Callback received : " + uri);
      Log.i(TAG, "Retrieving Access Token");
      
      task = new OAuthTask(this);
      task.execute(uri.getQueryParameter(OAuth.OAUTH_VERIFIER));
    } else if (task == null || task.getStatus() == Status.FINISHED) {
      Log.i(TAG, "Retrieving Request Token");
      Log.i(TAG, "Starting oAuthTask");
      
      task = new OAuthTask(this);
      task.execute();
    }
  }

  
  @Override
  public Object onRetainNonConfigurationInstance() {
    if (task != null)
      task.detach();
    
    return task;
  }

  @Override
  protected Dialog onCreateDialog(int id) {
    switch (id) {
      case DIALOG_PROGRESS_ID:
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setMessage(getText(R.string.loading));
        dialog.setCancelable(false);
        dialog.setButton(getText(R.string.cancel_button), new OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            if (task == null) {
              startActivity(new Intent().setClass(getApplicationContext(), PreferenceActivity.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP));
              finish();
            } else {
              task.cancel(true);
            }
          }
        });
        return dialog;

      case DIALOG_ERROR_NETWORK_ID:
        return new AlertDialog.Builder(this)
            .setTitle(R.string.login_error_title)
            .setMessage(R.string.error_network)
            .setCancelable(false)
            .setPositiveButton(R.string.ok_button, new OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startActivity(new Intent().setClass(getApplicationContext(), PreferenceActivity.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
              }
            })
            .create();

      case DIALOG_ERROR_WRONG_INPUT_ID:
        return new AlertDialog.Builder(this)
            .setTitle(R.string.login_error_title)
            .setMessage(R.string.login_message)
            .setCancelable(false)
            .setPositiveButton(R.string.ok_button, new OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startActivity(new Intent().setClass(getApplicationContext(), PreferenceActivity.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
              }
            })
            .create();

      default:
        return super.onCreateDialog(id);
    }
  }

  protected void storeToken(String username, String token) {
    Log.i(TAG, "storeToken()");

    task = null;
    
    final AuthenticatorHelper helper = Geocaching4LocusApplication.getAuthenticatorHelper();

    if (helper.hasAccount()) {
      helper.removeAccount();
    }
    
    final Account account = new Account(username, AccountAuthenticator.ACCOUNT_TYPE);
    
    helper.addAccountExplicitly(account, null);
    helper.setAuthToken(account, AccountAuthenticator.ACCOUNT_TYPE, token);

    startActivity(new Intent().setClass(getApplicationContext(), PreferenceActivity.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP));
    finish();
  }
  
  protected void tryOpenStockBrowser(final Uri uri) {
    final Intent stockBrowser = new Intent()
        .setComponent(new ComponentName("com.android.browser",
                        "com.android.browser.BrowserActivity"))
        .setData(uri)
        .setAction(Intent.ACTION_VIEW)
        .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

    // try the stock browser first, fall back to implicit intent if not found
    try {
        startActivity(stockBrowser);
    } catch (ActivityNotFoundException e) {
        Log.w(TAG, "default browser not found, falling back");
        startActivity(new Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY));
    }
}

  static class OAuthTask extends UserTask<String, Void, String[]> {
    private AuthenticatorActivity activity;

    public OAuthTask(AuthenticatorActivity activity) {
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
    protected String[] doInBackground(String... params) throws Exception {
      OAuthConsumer consumer = Geocaching4LocusApplication.getOAuthConsumer();
      OAuthProvider provider = Geocaching4LocusApplication.getOAuthProvider();
      
      if (params.length == 0) {
        return new String[] { 
            provider.retrieveRequestToken(consumer, AppConstants.OAUTH_CALLBACK_URL)
        };
      } else {
        provider.retrieveAccessToken(consumer, params[0]);
        
        // get account name
        GeocachingApi api = new LiveGeocachingApi();
        api.openSession(consumer.getToken());
        
        UserProfile userProfile = api.getYourUserProfile(false, false, false, false, false, false, DeviceInfoFactory.create());
        
        return new String[] { 
            userProfile.getUser().getUserName(),
            api.getSession()
        };
      }
    }

    @Override
    protected void onPostExecute(String[] result) {
      try {
        activity.dismissDialog(DIALOG_PROGRESS_ID);
      } catch (IllegalArgumentException ex) {
      }

      activity.task = null; 

      if (result.length == 1) {
        
        activity.authUrl = result[0];
        
        activity.tryOpenStockBrowser(Uri.parse(result[0]));
        activity.finish();
      } else if (result.length == 2) {
        activity.storeToken(result[0], result[1]);
      }
    }

    @Override
    protected void onCancelled() {
      super.onCancelled();

      try {
        activity.dismissDialog(DIALOG_PROGRESS_ID);
      } catch (IllegalArgumentException ex) {
      }
    
      activity.startActivity(new Intent().setClass(activity.getApplicationContext(), PreferenceActivity.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP));
      activity.finish();
    }

    @Override
    protected void onException(Throwable e) {
      try {
        activity.dismissDialog(DIALOG_PROGRESS_ID);
      } catch (IllegalArgumentException ex) {
      }

      if (isCancelled())
        return;

      Log.e(TAG, e.getMessage(), e);

      if (e instanceof InvalidCredentialsException) {
        activity.showDialog(DIALOG_ERROR_CREDENTIALS_ID);
      } else if (e instanceof NetworkException || e instanceof OAuthCommunicationException) {
        activity.showDialog(DIALOG_ERROR_NETWORK_ID);
      } else {
        ErrorReporter.getInstance().putCustomData("source", "login");

        String message = e.getMessage();
        if (message == null)
          message = "";

        activity.startActivity(ErrorActivity.createErrorIntent(activity, R.string.error,
            String.format("%s<br>Exception: %s", message, e.getClass().getSimpleName()), false, e));
        
        activity.startActivity(new Intent().setClass(activity.getApplicationContext(), PreferenceActivity.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP));

        activity.finish();
      }
      
    }
  }
}