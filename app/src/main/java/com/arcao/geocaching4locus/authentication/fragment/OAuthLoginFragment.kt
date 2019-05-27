package com.arcao.geocaching4locus.authentication.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.base.ProgressState
import com.arcao.geocaching4locus.base.constants.AppConstants
import com.arcao.geocaching4locus.base.util.exhaustive
import com.arcao.geocaching4locus.base.util.showWebPage
import com.arcao.geocaching4locus.base.util.withObserve
import com.arcao.geocaching4locus.error.ErrorActivity
import com.github.scribejava.core.model.OAuthConstants
import timber.log.Timber

class OAuthLoginFragment : BaseOAuthLoginFragment() {
    private lateinit var webView: WebView
    private lateinit var progressLayout: View
    private var lastInstanceState: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.progress.withObserve(this) {
            handleProgress(it)
        }
        viewModel.action.withObserve(this, ::handleAction)

        if (savedInstanceState == null)
            viewModel.startLogin()
    }

    private fun handleProgress(state: ProgressState) {
        when (state) {
            is ProgressState.ShowProgress -> progressLayout.visibility = View.VISIBLE
            ProgressState.HideProgress -> progressLayout.visibility = View.GONE
        }.exhaustive
    }

    override fun onLoginUrlAvailable(url: String) {
        webView.loadUrl(url)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt(STATE_PROGRESS_VISIBLE, progressLayout.visibility)
        webView.saveState(outState)
        lastInstanceState = outState
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // FIX savedInstanceState is null after rotation change
        if (savedInstanceState != null)
            lastInstanceState = savedInstanceState

        val view = inflater.inflate(R.layout.fragment_login_oauth, container, false)
        progressLayout = view.findViewById(R.id.progressHolder)
        viewModel.showProgress()

        webView = createWebView(lastInstanceState)

        val webViewHolder = view.findViewById<FrameLayout>(R.id.webViewPlaceholder)
        webViewHolder.addView(webView, FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        return view
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun createWebView(savedInstanceState: Bundle?): WebView {
        val webView = FixedWebView(requireActivity())

        webView.isHorizontalScrollBarEnabled = false
        webView.webViewClient = DialogWebViewClient()
        webView.settings.javaScriptEnabled = true

        if (savedInstanceState != null)
            webView.restoreState(savedInstanceState)

        return webView
    }

    private inner class DialogWebViewClient internal constructor() : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            if (url.startsWith(AppConstants.OAUTH_CALLBACK_URL)) {
                val uri = Uri.parse(url)

                viewModel.finishLogin(uri.getQueryParameter(OAuthConstants.CODE) ?: return true)
                return true
            }

            if (!isWebViewSafeUrl(url)) {
                Timber.d("External URL: %s", url)

                // launch external URLs in a full browser
                requireActivity().showWebPage(Uri.parse(url))
                return true
            }

            return false
        }

        private fun isWebViewSafeUrl(url: String): Boolean {
            return true
        }

        override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
            super.onReceivedError(view, errorCode, description, failingUrl)

            startActivity(ErrorActivity.IntentBuilder(requireActivity()).message(description).build())
            cancelAction()
        }

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            viewModel.showProgress()
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            viewModel.hideProgress()
        }
    }

    private class FixedWebView : WebView {
        constructor(context: Context) : super(context)

        constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

        constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

        override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
            try {
                super.onWindowFocusChanged(hasWindowFocus)
            } catch (e: NullPointerException) {
                // Catch null pointer exception
                // suggested workaround from Web
            }
        }
    }

    companion object {
        private const val STATE_PROGRESS_VISIBLE = "STATE_PROGRESS_VISIBLE"

        fun newInstance(): OAuthLoginFragment {
            return OAuthLoginFragment()
        }
    }
}
