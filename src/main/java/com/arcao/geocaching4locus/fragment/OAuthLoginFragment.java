package com.arcao.geocaching4locus.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.ErrorActivity;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.task.OAuthLoginTask;
import com.arcao.geocaching4locus.task.OAuthLoginTask.TaskListener;
import com.arcao.geocaching4locus.util.IntentUtil;
import timber.log.Timber;

import java.lang.ref.WeakReference;
import java.util.Locale;

public class OAuthLoginFragment extends Fragment implements TaskListener {
	private static final String STATE_PROGRESS_VISIBLE = "STATE_PROGRESS_VISIBLE";
	private static final String OAUTH_VERIFIER = "oauth_verifier";

	public interface DialogListener {
		void onLoginFinished(Intent errorIntent);
	}

	@Nullable private OAuthLoginTask mTask;
	private WeakReference<DialogListener> mDialogListenerRef;
	private WebView mWebView = null;
	private View mProgressHolder = null;
	private Bundle mLastInstanceState;

	public static OAuthLoginFragment newInstance() {
		return new OAuthLoginFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRetainInstance(true);

		// clear geocaching.com cookies
		App.get(getActivity()).clearGeocachingCookies();

		mTask = new OAuthLoginTask(getActivity(), this);
		mTask.execute();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mDialogListenerRef = new WeakReference<>((DialogListener) activity);
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnTaskFinishListener");
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (mTask != null) {
			mTask.cancel(true);
			mTask = null;
		}
	}

	@Override
	public void onLoginUrlAvailable(@NonNull String url) {
		if (mWebView != null) {
			mWebView.loadUrl(url);
		}
	}

	@Override
	public void onTaskFinished(Intent errorIntent) {
		DialogListener listener = mDialogListenerRef.get();
		if (listener != null) {
			listener.onLoginFinished(errorIntent);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mWebView != null) {
			mWebView.saveState(outState);
		}

		if (mProgressHolder != null) {
			outState.putInt(STATE_PROGRESS_VISIBLE, mProgressHolder.getVisibility());
			Timber.d("setVisibility: " + mProgressHolder.getVisibility());
		}

		mLastInstanceState = outState;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// FIX savedInstanceState is null after rotation change
		if (savedInstanceState != null)
			mLastInstanceState = savedInstanceState;

		View view = inflater.inflate(R.layout.fragment_login_oauth, container, false);
		mProgressHolder = view.findViewById(R.id.progressHolder);
		mProgressHolder.setVisibility(View.VISIBLE);

		if (mLastInstanceState != null) {
			//noinspection ResourceType
			mProgressHolder.setVisibility(mLastInstanceState.getInt(STATE_PROGRESS_VISIBLE, View.VISIBLE));
		}

		mWebView = createWebView(mLastInstanceState);

		FrameLayout webViewHolder = (FrameLayout) view.findViewById(R.id.webViewPlaceholder);
		webViewHolder.addView(mWebView, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		return view;
	}

	@SuppressLint("SetJavaScriptEnabled")
	private WebView createWebView(Bundle savedInstanceState) {
		WebView webView = new FixedWebView(getActivity());

		webView.setHorizontalScrollBarEnabled(false);
		webView.setWebViewClient(new DialogWebViewClient());
		webView.getSettings().setJavaScriptEnabled(true);

		if (savedInstanceState != null)
			webView.restoreState(savedInstanceState);

		return webView;
	}

	private class DialogWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if (url.startsWith(AppConstants.OAUTH_CALLBACK_URL)) {
				Uri uri = Uri.parse(url);

				if (mProgressHolder != null) {
					mProgressHolder.setVisibility(View.VISIBLE);
				}

				mTask = new OAuthLoginTask(getActivity(), OAuthLoginFragment.this);
				mTask.execute(uri.getQueryParameter(OAUTH_VERIFIER));
				return true;
			}

			if (!isOAuthUrl(url)) {
				Timber.d("External URL: " + url);

				// launch external URLs in a full browser
				IntentUtil.showWebPage(getActivity(), Uri.parse(url));
				return true;
			}

			return false;
		}

		private boolean isOAuthUrl(String url) {
			String urlLowerCase = url.toLowerCase(Locale.US);

			return urlLowerCase.contains("/oauth/") ||
							urlLowerCase.contains("/mobileoauth/") ||
							urlLowerCase.contains("/mobilesignin/") ||
							urlLowerCase.contains("/mobilecontent/") ||
							urlLowerCase.contains("//m.facebook") ||
							urlLowerCase.contains("/login/fbc.aspx");
		}

		@Override
		@SuppressWarnings("deprecation")
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);

			if (getActivity() != null)
				onTaskFinished(new ErrorActivity.IntentBuilder(getActivity()).message(description).build());
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			if (mProgressHolder != null) {
				mProgressHolder.setVisibility(View.VISIBLE);
			}
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);

			if (mProgressHolder != null && !url.startsWith(AppConstants.OAUTH_CALLBACK_URL)) {
				mProgressHolder.setVisibility(View.GONE);
			}
		}
	}

	private static class FixedWebView extends WebView {
		public FixedWebView(Context context) {
			super (context);
		}

		public FixedWebView(Context context, AttributeSet attrs, int defStyle) {
			super (context, attrs, defStyle);
		}

		public FixedWebView(Context context, AttributeSet attrs) {
			super (context, attrs);
		}

		@Override
		public void onWindowFocusChanged(boolean hasWindowFocus) {
			try {
				super.onWindowFocusChanged(hasWindowFocus);
			} catch (NullPointerException e) {
				// Catch null pointer exception
				// suggested workaround from Web
			}
		}
	}
}
