package com.arcao.geocaching4locus.fragment;

import java.lang.ref.WeakReference;
import java.util.Locale;

import oauth.signpost.OAuth;
import android.accounts.Account;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.arcao.geocaching4locus.ErrorActivity;
import com.arcao.geocaching4locus.Geocaching4LocusApplication;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.authentication.helper.AuthenticatorHelper;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.task.OAuthLoginTask;
import com.arcao.geocaching4locus.task.OAuthLoginTask.OAuthLoginTaskListener;

public class OAuthLoginDialogFragment extends AbstractDialogFragment implements OAuthLoginTaskListener {
	private static final String STATE_PROGRESS_VISIBLE = "STATE_PROGRESS_VISIBLE";
	public static final String TAG = UpdateDialogFragment.class.getName();

	public interface OnTaskFinishedListener {
		void onTaskFinished(Intent errorIntent);
	}

	protected OAuthLoginTask mTask;
	protected WeakReference<OnTaskFinishedListener> taskFinishedListenerRef;
	protected WebView webView = null;
	protected View progressHolder = null;
	protected Bundle lastInstanceState;

	public static OAuthLoginDialogFragment newInstance() {
		return new OAuthLoginDialogFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRetainInstance(true);

		mTask = new OAuthLoginTask();
		mTask.setOAuthLoginTaskListener(this);
		mTask.execute();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			taskFinishedListenerRef = new WeakReference<OnTaskFinishedListener>((OnTaskFinishedListener) activity);
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnTaskFinishListener");
		}
	}

	@Override
	public void onLoginUrlAvailable(String url) {
		if (webView != null) {
			webView.loadUrl(url);
		}
	}
	
	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
		
		if (mTask != null)
			mTask.cancel(false);
		
		OnTaskFinishedListener listener = taskFinishedListenerRef.get();
		if (listener != null) {
			listener.onTaskFinished(null);
		}
	}

	@Override
	public void onOAuthTaskFinished(String userName, String token) {
		dismiss();

		final AuthenticatorHelper helper = Geocaching4LocusApplication.getAuthenticatorHelper();

		if (helper.hasAccount()) {
			helper.removeAccount();
		}

		final Account account = new Account(userName, AuthenticatorHelper.ACCOUNT_TYPE);

		helper.addAccountExplicitly(account, null);
		helper.setAuthToken(account, AuthenticatorHelper.ACCOUNT_TYPE, token);

		OnTaskFinishedListener listener = taskFinishedListenerRef.get();
		if (listener != null) {
			listener.onTaskFinished(null);
		}
	}

	@Override
	public void onTaskError(Intent errorIntent) {
		dismiss();

		OnTaskFinishedListener listener = taskFinishedListenerRef.get();
		if (listener != null) {
			listener.onTaskFinished(errorIntent);
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		if (webView != null) {		
			webView.saveState(outState);
		}
		
		if (progressHolder != null) {
			outState.putInt(STATE_PROGRESS_VISIBLE, progressHolder.getVisibility());
			Log.d(TAG, "setVisibility: " + progressHolder.getVisibility());
		}
		
		lastInstanceState = outState;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// FIX savedInstanceState is null after rotation change
		if (savedInstanceState == null)
			savedInstanceState = lastInstanceState;
		
		getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		View view = inflater.inflate(R.layout.dialog_login, container);
		progressHolder = view.findViewById(R.id.progressHolder);
		progressHolder.setVisibility(View.VISIBLE);
		
		if (savedInstanceState != null) {
			progressHolder.setVisibility(savedInstanceState.getInt(STATE_PROGRESS_VISIBLE, View.VISIBLE));
		}
			
		webView = createWebView(savedInstanceState);

		FrameLayout webViewHolder = (FrameLayout) view.findViewById(R.id.webViewPlaceholder);
		webViewHolder.addView(webView, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		return view;
	}
	
	@SuppressLint("SetJavaScriptEnabled")
	public WebView createWebView(Bundle savedInstanceState) {
		WebView webView = new WebView(getActivity()) {
			@Override
			public boolean onCheckIsTextEditor() {
				return true;
			}
		};
		
    //webView.setVerticalScrollBarEnabled(false);
    webView.setHorizontalScrollBarEnabled(false);
    webView.setWebViewClient(new DialogWebViewClient());
    webView.getSettings().setJavaScriptEnabled(true);
    webView.getSettings().setSavePassword(false);
    
    if (savedInstanceState != null)
    	webView.restoreState(savedInstanceState);
    
    // FIX on screen keyboard issue
    /*webView.requestFocus(View.FOCUS_DOWN);
    webView.setOnTouchListener(new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_UP:
                    if (!v.hasFocus()) {
                        v.requestFocus();
                    }
                    break;
            }
            return false;
        }
    });*/
    
    return webView;
	}

	private class DialogWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if (url.startsWith(AppConstants.OAUTH_CALLBACK_URL)) {
				Uri uri = Uri.parse(url);
				
				if (progressHolder != null) {
					progressHolder.setVisibility(View.VISIBLE);
				}
				
				mTask = new OAuthLoginTask();
				mTask.setOAuthLoginTaskListener(OAuthLoginDialogFragment.this);
				mTask.execute(uri.getQueryParameter(OAuth.OAUTH_VERIFIER));
				
				return true;
			}

			if (!isOAuthUrl(url)) {
				Log.d(TAG, "External URL: " + url);
				
				// launch external URLs in a full browser
				getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
				return true;
			}
			
			return false;
		}
		
		protected boolean isOAuthUrl(String url) {
			url = url.toLowerCase(Locale.US);
			
			return url.contains("/oauth/") ||	
					url.contains("/mobileoauth/") || 
					url.contains("/mobilesignin/") || 
					url.contains("/mobilecontent/") || 
					url.contains("//m.facebook");
		}

		@Override
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);

			onTaskError(ErrorActivity.createErrorIntent(getActivity(), R.string.error_login, description, false, null));
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);			
			if (progressHolder != null) {
				progressHolder.setVisibility(View.VISIBLE);
			}
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);

			if (progressHolder != null && !url.startsWith(AppConstants.OAUTH_CALLBACK_URL)) {
				progressHolder.setVisibility(View.GONE);
			}
		}
	}
}
