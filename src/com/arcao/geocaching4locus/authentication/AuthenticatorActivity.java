package com.arcao.geocaching4locus.authentication;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;

import org.acra.ErrorReporter;
import org.apache.http.impl.cookie.DateUtils;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
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
import com.arcao.geocaching.api.impl.LiveGeocachingApiFactory;
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
  
  public static final int ERROR_ACTIVITY_REQUEST_CODE = 1;

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
    
    final Account account = new Account(username, AuthenticatorHelper.ACCOUNT_TYPE);
    
    helper.addAccountExplicitly(account, null);
    helper.setAuthToken(account, AuthenticatorHelper.ACCOUNT_TYPE, token);

    startActivity(new Intent().setClass(getApplicationContext(), PreferenceActivity.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP));
    finish();
  }
  
  protected void openUriInBrowser(final Uri uri) {
    startActivity(new Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY));
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

			if (activity.isFinishing()) {
				cancel(true);
				return;
			}
      
      activity.showDialog(DIALOG_PROGRESS_ID);
    }

    @Override
    protected String[] doInBackground(String... params) throws Exception {
      OAuthConsumer consumer = Geocaching4LocusApplication.getOAuthConsumer();
      OAuthProvider provider = Geocaching4LocusApplication.getOAuthProvider();
      
      // we use server time for OAuth timestamp because device can have wrong timezone or time
      String timestamp = Long.toString(getServerDate(AppConstants.GEOCACHING_WEBSITE_URL).getTime() / 1000);
      
      try {
        if (params.length == 0) {
          String authUrl = provider.retrieveRequestToken(consumer, AppConstants.OAUTH_CALLBACK_URL, OAuth.OAUTH_TIMESTAMP, timestamp); 
          Geocaching4LocusApplication.storeRequestTokens(consumer);
          return new String[] { authUrl };
        } else {
          Geocaching4LocusApplication.loadRequestTokensIfNecessary(consumer);
          provider.retrieveAccessToken(consumer, params[0], OAuth.OAUTH_TIMESTAMP, timestamp);
          
          // get account name
          GeocachingApi api = LiveGeocachingApiFactory.create();
          api.openSession(consumer.getToken());
          
          UserProfile userProfile = api.getYourUserProfile(false, false, false, false, false, false, DeviceInfoFactory.create());
          
          return new String[] { 
              userProfile.getUser().getUserName(),
              api.getSession()
          };
        }
      } catch (OAuthExpectationFailedException e) {
        if (provider.getResponseParameters().containsKey(AppConstants.OAUTH_ERROR_MESSAGE_PARAMETER)) {
          throw new OAuthExpectationFailedException("Request token or token secret not set in server reply. " 
              + provider.getResponseParameters().getFirst(AppConstants.OAUTH_ERROR_MESSAGE_PARAMETER));
        }
        
        throw e;
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
        
        activity.openUriInBrowser(Uri.parse(result[0]));
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

        activity.startActivity(ErrorActivity.createErrorIntent(activity, 0,
            String.format("%s<br>Exception: %s", message, e.getClass().getSimpleName()), true, e));
        activity.finish();
      }
    }
    
		private static Date getServerDate(String url) {
			HttpURLConnection c = null;

			try {
				Log.i(TAG, "Getting server time from url: " + url);
				c = (HttpURLConnection) new URL(url).openConnection();
				c.setRequestMethod("HEAD");
				c.setDoInput(false);
				c.setDoOutput(false);
				c.connect();
				if (c.getResponseCode() == HttpURLConnection.HTTP_OK) {
					String date = c.getHeaderField("Date");
					if (date != null) {
						Log.i(TAG, "We got time: " + date);
						return DateUtils.parseDate(date);
					}
				}
			} catch (Exception e) {
				new NetworkException(e.getMessage(), e);
			} finally {
				if (c != null)
					c.disconnect();
			}

			Log.e(TAG, "No Date header found in a response, used device time instead.");
			return new Date();
		}
	}
}