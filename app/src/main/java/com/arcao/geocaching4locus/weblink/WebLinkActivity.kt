package com.arcao.geocaching4locus.weblink

import android.app.Activity
import android.os.Bundle
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.authentication.LoginActivity
import com.arcao.geocaching4locus.base.AbstractActionBarActivity
import com.arcao.geocaching4locus.base.util.exhaustive
import com.arcao.geocaching4locus.base.util.showWebPage
import com.arcao.geocaching4locus.base.util.withObserve
import com.arcao.geocaching4locus.error.ErrorActivity
import locus.api.android.utils.IntentHelper
import timber.log.Timber

abstract class WebLinkActivity : AbstractActionBarActivity() {
    protected abstract val viewModel: WebLinkViewModel

    private val loginActivity = registerForActivityResult(LoginActivity.Contract) { success ->
        if (success) {
            processIntent()
        } else {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

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
            WebLinkAction.SignIn -> loginActivity.launch(null)
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
                startActivity(
                    ErrorActivity.IntentBuilder(this).message(R.string.error_premium_feature)
                        .build()
                )
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }.exhaustive
    }
}
