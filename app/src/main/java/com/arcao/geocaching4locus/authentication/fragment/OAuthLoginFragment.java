package com.arcao.geocaching4locus.authentication.fragment;

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
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.authentication.task.OAuthLoginTask;
import com.arcao.geocaching4locus.authentication.task.OAuthLoginTask.TaskListener;
import com.arcao.geocaching4locus.base.constants.AppConstants;
import com.arcao.geocaching4locus.base.util.IntentUtil;
import com.arcao.geocaching4locus.error.ErrorActivity;

import java.lang.ref.WeakReference;

import timber.log.Timber;

public class OAuthLoginFragment extends Fragment implements TaskListener {
    private static final String STATE_PROGRESS_VISIBLE = "STATE_PROGRESS_VISIBLE";
    private static final String OAUTH_VERIFIER = "oauth_verifier";

    public interface DialogListener {
        void onLoginFinished(Intent errorIntent);
    }

    @Nullable OAuthLoginTask task;
    private WeakReference<DialogListener> dialogListenerRef;
    private WebView webView;
    View progressLayout;
    private Bundle lastInstanceState;

    public static OAuthLoginFragment newInstance() {
        return new OAuthLoginFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        // clear geocaching.com cookies
        App.get(getActivity()).clearGeocachingCookies();

        task = new OAuthLoginTask(getActivity(), this);
        task.execute();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            dialogListenerRef = new WeakReference<>((DialogListener) activity);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnTaskFinishListener");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (task != null) {
            task.cancel(true);
            task = null;
        }
    }

    @Override
    public void onLoginUrlAvailable(@NonNull String url) {
        if (webView != null) {
            webView.loadUrl(url);
        }
    }

    @Override
    public void onTaskFinished(Intent errorIntent) {
        DialogListener listener = dialogListenerRef.get();
        if (listener != null) {
            listener.onLoginFinished(errorIntent);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (webView != null) {
            webView.saveState(outState);
        }

        if (progressLayout != null) {
            outState.putInt(STATE_PROGRESS_VISIBLE, progressLayout.getVisibility());
            Timber.d("setVisibility: %d", progressLayout.getVisibility());
        }

        lastInstanceState = outState;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // FIX savedInstanceState is null after rotation change
        if (savedInstanceState != null)
            lastInstanceState = savedInstanceState;

        View view = inflater.inflate(R.layout.fragment_login_oauth, container, false);
        progressLayout = view.findViewById(R.id.progressHolder);
        progressLayout.setVisibility(View.VISIBLE);

        if (lastInstanceState != null) {
            progressLayout.setVisibility(lastInstanceState.getInt(STATE_PROGRESS_VISIBLE, View.VISIBLE));
        }

        webView = createWebView(lastInstanceState);

        FrameLayout webViewHolder = view.findViewById(R.id.webViewPlaceholder);
        webViewHolder.addView(webView, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

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
        DialogWebViewClient() {
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith(AppConstants.OAUTH_CALLBACK_URL)) {
                Uri uri = Uri.parse(url);

                if (progressLayout != null) {
                    progressLayout.setVisibility(View.VISIBLE);
                }

                task = new OAuthLoginTask(getActivity(), OAuthLoginFragment.this);
                task.execute(uri.getQueryParameter(OAUTH_VERIFIER));
                return true;
            }

            if (!isOAuthUrl(url)) {
                Timber.d("External URL: %s", url);

                // launch external URLs in a full browser
                IntentUtil.showWebPage(getActivity(), Uri.parse(url));
                return true;
            }

            return false;
        }

        private boolean isOAuthUrl(String url) {
            return  true;
//            String urlLowerCase = url.toLowerCase(Locale.US);
//
//            return urlLowerCase.contains("/oauth/") ||
//                    urlLowerCase.contains("/mobileoauth/") ||
//                    urlLowerCase.contains("/mobilesignin/") ||
//                    urlLowerCase.contains("/mobilecontent/") ||
//                    urlLowerCase.contains("//m.facebook") ||
//                    urlLowerCase.contains("//www.facebook") ||
//                    urlLowerCase.contains("//facebook") ||
//                    urlLowerCase.contains("/account/login") ||
//                    urlLowerCase.contains("/account/register/createaccountwithfacebook") ||
//                    urlLowerCase.contains("/api/beginfacebook") ||
//                    urlLowerCase.contains("/account/join/fb")
//                    ;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);

            if (getActivity() != null)
                onTaskFinished(new ErrorActivity.IntentBuilder(getActivity()).message(description).build());
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if (progressLayout != null) {
                progressLayout.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            if (progressLayout != null && !url.startsWith(AppConstants.OAUTH_CALLBACK_URL)) {
                progressLayout.setVisibility(View.GONE);
            }
        }
    }

    private static class FixedWebView extends WebView {
        public FixedWebView(Context context) {
            super(context);
        }

        public FixedWebView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        public FixedWebView(Context context, AttributeSet attrs) {
            super(context, attrs);
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
