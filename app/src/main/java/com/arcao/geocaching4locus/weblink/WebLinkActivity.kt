package com.arcao.geocaching4locus.weblink

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.annotation.Nullable
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.authentication.util.AccountManager
import com.arcao.geocaching4locus.base.AbstractActionBarActivity
import com.arcao.geocaching4locus.base.util.exhaustive
import com.arcao.geocaching4locus.base.util.observe
import com.arcao.geocaching4locus.base.util.showWebPage
import com.arcao.geocaching4locus.error.ErrorActivity
import com.arcao.geocaching4locus.weblink.fragment.WebLinkProgressDialogFragment
import locus.api.android.utils.LocusUtils
import org.koin.android.ext.android.inject
import timber.log.Timber

abstract class WebLinkActivity : AbstractActionBarActivity(), WebLinkProgressDialogFragment.DialogListener {
    protected abstract val viewModel: WebLinkViewModel
    private val accountManager by inject<AccountManager>()

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.progressVisible.observe(this) { visible ->
            if (visible)
                showProgressDialog()
            else
                dismissProgresDialog()
        }

        viewModel.action.observe(this, ::handleAction)

        processIntent()
    }

    private fun dismissProgresDialog() {
        val f = supportFragmentManager.findFragmentByTag(WebLinkProgressDialogFragment.FRAGMENT_TAG) as WebLinkProgressDialogFragment?
        f?.dismiss()
    }

    override fun onProgressCancel() {
        viewModel.cancelRetrieveUri()

        setResult(Activity.RESULT_CANCELED)
        finish()
        return
    }

    private fun showProgressDialog() {
        if (supportFragmentManager.findFragmentByTag(WebLinkProgressDialogFragment.FRAGMENT_TAG) != null)
            return

        val f = WebLinkProgressDialogFragment.newInstance()
        f.show(supportFragmentManager, WebLinkProgressDialogFragment.FRAGMENT_TAG)
    }

    private fun processIntent() {
        try {
            if (LocusUtils.isIntentPointTools(intent)) {
                val point = LocusUtils.handleIntentPointTools(this, intent)

                if (point == null) {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                    return
                }

                viewModel.retrieveUri(point)
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
            is WebLinkAction.ResolvedUri ->
                showWebPage(action.uri)
            is WebLinkAction.Error -> {
                startActivity(action.intent)
                finish()
            }
            WebLinkAction.NavigationBack ->
                onBackPressed()
            WebLinkAction.PremiumMembershipRequired -> {
                startActivity(ErrorActivity.IntentBuilder(this).message(R.string.error_premium_feature).build())
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }.exhaustive
    }

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
