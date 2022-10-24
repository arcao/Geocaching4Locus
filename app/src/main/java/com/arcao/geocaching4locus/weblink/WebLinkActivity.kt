package com.arcao.geocaching4locus.weblink

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.authentication.util.requestSignOn
import com.arcao.geocaching4locus.base.AbstractActionBarActivity
import com.arcao.geocaching4locus.base.util.exhaustive
import com.arcao.geocaching4locus.base.util.showWebPage
import com.arcao.geocaching4locus.base.util.withObserve
import com.arcao.geocaching4locus.data.account.AccountManager
import com.arcao.geocaching4locus.error.ErrorActivity
import locus.api.android.utils.IntentHelper
import org.koin.android.ext.android.inject
import timber.log.Timber

abstract class WebLinkActivity : AbstractActionBarActivity() {
    protected abstract val viewModel: WebLinkViewModel
    private val accountManager by inject<AccountManager>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.progress.withObserve(this, ::handleProgress)
        viewModel.action.withObserve(this, ::handleAction)

        processIntent()
    }

    override fun onProgressCancel(requestId: Int) {
        viewModel.cancelRetrieveUri()
    }

    private fun processIntent() {
        try {
            if (IntentHelper.isIntentPointTools(intent)) {
                val point = IntentHelper.getPointFromIntent(this, intent)

                if (point == null) {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                    return
                }

                viewModel.resolveUri(point)
            }
        } catch (e: Exception) {
            Timber.e(e)
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    @Suppress("IMPLICIT_CAST_TO_ANY")
    private fun handleAction(action: WebLinkAction) {
        when (action) {
            WebLinkAction.SignIn ->
                accountManager.requestSignOn(this, REQUEST_SIGN_ON)
            is WebLinkAction.ShowUri -> {
                if (showWebPage(action.uri)) {
                    setResult(Activity.RESULT_OK)
                } else {
                    setResult(Activity.RESULT_CANCELED)
                }
                finish()
            }
            is WebLinkAction.Error -> {
                startActivity(action.intent)
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
            WebLinkAction.Cancel -> {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
            WebLinkAction.PremiumMembershipRequired -> {
                startActivity(ErrorActivity.IntentBuilder(this).message(R.string.error_premium_feature).build())
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }.exhaustive
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // restart update process after log in
        if (requestCode == REQUEST_SIGN_ON) {
            if (resultCode == Activity.RESULT_OK) {
                processIntent()
            } else {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }
    }

    companion object {
        private const val REQUEST_SIGN_ON = 1
    }
}
