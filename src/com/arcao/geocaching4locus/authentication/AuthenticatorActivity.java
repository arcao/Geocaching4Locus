package com.arcao.geocaching4locus.authentication;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;

import org.acra.ErrorReporter;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.arcao.geocaching.api.GeocachingApi;
import com.arcao.geocaching.api.data.UserProfile;
import com.arcao.geocaching.api.exception.InvalidCredentialsException;
import com.arcao.geocaching.api.exception.NetworkException;
import com.arcao.geocaching.api.impl.LiveGeocachingApi;
import com.arcao.geocaching4locus.ErrorActivity;
import com.arcao.geocaching4locus.Geocaching4LocusApplication;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.util.DeviceInfoFactory;
import com.arcao.geocaching4locus.util.UserTask;

public class AuthenticatorActivity extends AccountAuthenticatorActivity {
  private static final String TAG = "G4L|AuthenticatorActivity";

  public static final String PARAM_AUTHTOKEN_TYPE = "AUTHTOKEN_TYPE";

  public static final int DIALOG_PROGRESS_ID = 0;
  public static final int DIALOG_ERROR_CREDENTIALS_ID = 1;
  public static final int DIALOG_ERROR_NETWORK_ID = 2;
  public static final int DIALOG_ERROR_WRONG_INPUT_ID = 3;

  protected OAuthTask task;

  protected FrameLayout webViewPlaceholder;
  protected WebView webView;

  protected OAuthConsumer consumer = new CommonsHttpOAuthConsumer(AppConstants.OAUTH_CONSUMER_KEY, AppConstants.OAUTH_CONSUMER_SECRET);
  protected OAuthProvider provider = new CommonsHttpOAuthProvider(AppConstants.OAUTH_REQUEST_URL, AppConstants.OAUTH_ACCESS_URL,
      AppConstants.OAUTH_AUTHORIZE_URL);
  
  String authUrl = null;

  @Override
  protected void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    
    setContentView(R.layout.login_activity);
    
    // hide preferences button
    findViewById(R.id.image_view_separator_setting).setVisibility(View.GONE);
    findViewById(R.id.image_button_title_setting).setVisibility(View.GONE);

    task = new OAuthTask(this, consumer, provider);
    initWebView();
    
    if ((task = (OAuthTask) getLastNonConfigurationInstance()) == null) {
      // if oAuth pre-state
      if (authUrl == null) {
        Log.i(TAG, "Starting oAuthTask");
        task = createOAuthTask();
        task.execute();
      }
    } else {
      Log.i(TAG, "Restarting oAuthTask");
      task.attach(this, consumer, provider);
    }
  }
  
  protected OAuthTask createOAuthTask() {
    return new OAuthTask(this, consumer, provider);
  }

  @SuppressLint("SetJavaScriptEnabled")
  protected void initWebView() {
    // Retrieve UI elements
    webViewPlaceholder = ((FrameLayout) findViewById(R.id.webViewPlaceholder));

    // Initialize the WebView if necessary
    if (webView == null) {
      // Create the webview
      webView = new WebView(this);
      webView.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
      webView.getSettings().setSupportZoom(true);
      webView.getSettings().setBuiltInZoomControls(true);
      webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
      webView.setScrollbarFadingEnabled(true);
      webView.getSettings().setLoadsImagesAutomatically(true);
      //webView.getSettings().setJavaScriptEnabled(true);
            
      // Load the URLs inside the WebView, not in the external web browser
      webView.setWebViewClient(new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
          if (url != null && url.startsWith(AppConstants.OAUTH_CALLBACK_URL)) {
            Uri uri = Uri.parse(url);
            task = createOAuthTask();
            task.execute(uri.getQueryParameter("oauth_verifier"));
            return true;
          }
          
          return super.shouldOverrideUrlLoading(view, url);
        }
        
        @Override
        public void onPageFinished(WebView view, String url) {
          super.onPageFinished(view, url);
          
          try {
            AuthenticatorActivity.this.dismissDialog(DIALOG_PROGRESS_ID);
          } catch (IllegalArgumentException ex) {
          }
        }
        
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
          super.onPageStarted(view, url, favicon);
          
          AuthenticatorActivity.this.showDialog(DIALOG_PROGRESS_ID);
        }
        
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
          super.onReceivedError(view, errorCode, description, failingUrl);
          
          try {
            AuthenticatorActivity.this.dismissDialog(DIALOG_PROGRESS_ID);
          } catch (IllegalArgumentException ex) {
          }
          
          AuthenticatorActivity.this.showDialog(DIALOG_ERROR_NETWORK_ID);
        }
      });

      // Load a page
      if (authUrl != null) {
        webView.loadUrl(authUrl);
      }
    }

    // Attach the WebView to its placeholder
    webViewPlaceholder.addView(webView);
  }
  
  public void onClickClose(View view) {
    finish();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    if (webView != null) {
      // Remove the WebView from the old placeholder
      webViewPlaceholder.removeView(webView);
    }

    super.onConfigurationChanged(newConfig);

    // Load the layout resource for the new configuration
    setContentView(R.layout.login_activity);

    // hide preferences button
    findViewById(R.id.image_view_separator_setting).setVisibility(View.GONE);
    findViewById(R.id.image_button_title_setting).setVisibility(View.GONE);

    // Reinitialize the UI
    initWebView();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    // Save the state of the WebView
    webView.saveState(outState);
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);

    // Restore the state of the WebView
    webView.restoreState(savedInstanceState);
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
        dialog.setButton(getText(R.string.cancel_button), new OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            if (task == null) {
              AuthenticatorActivity.this.finish();
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
            .setPositiveButton(R.string.ok_button, new OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                AuthenticatorActivity.this.finish();
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
                AuthenticatorActivity.this.finish();
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

    final Account account = new Account(username, AccountAuthenticator.ACCOUNT_TYPE);

    if (Geocaching4LocusApplication.getAuthenticatorHelper().hasAccount()) {
      Geocaching4LocusApplication.getAuthenticatorHelper().removeAccount();
    }
    
    Geocaching4LocusApplication.getAuthenticatorHelper().addAccountExplicitly(account, null);

    final Intent intent = new Intent();

    intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, username);
    intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, AccountAuthenticator.ACCOUNT_TYPE);

    Geocaching4LocusApplication.getAuthenticatorHelper().setAuthToken(account, AccountAuthenticator.ACCOUNT_TYPE, token);
    intent.putExtra(AccountManager.KEY_AUTHTOKEN, token);

    setAccountAuthenticatorResult(intent.getExtras());
    setResult(RESULT_OK, intent);
    finish();
  }

  static class OAuthTask extends UserTask<String, Void, String[]> {
    private AuthenticatorActivity activity;
    private OAuthConsumer consumer;
    private OAuthProvider provider;

    public OAuthTask(AuthenticatorActivity activity, OAuthConsumer consumer, OAuthProvider provider) {
      attach(activity, consumer, provider);
    }

    public void attach(AuthenticatorActivity activity, OAuthConsumer consumer, OAuthProvider provider) {
      this.activity = activity;
      this.consumer = consumer;
      this.provider = provider;
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
      if (params.length == 0) {
        return new String[] { provider.retrieveRequestToken(consumer, AppConstants.OAUTH_CALLBACK_URL) };
      } else {
        provider.retrieveAccessToken(consumer, params[0]);
        
        // get account name
        GeocachingApi api = new LiveGeocachingApi();
        api.openSession(consumer.getToken());
        
        UserProfile userProfile = api.getYourUserProfile(false, false, false, false, false, false, DeviceInfoFactory.create());
        return new String[] { userProfile.getUser().getUserName(), api.getSession() };
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
        activity.webView.loadUrl(result[0]);
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
        activity.finish();
      }
      
    }
  }
}