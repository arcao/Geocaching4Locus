package com.arcao.geocaching4locus.update

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.arcao.geocaching4locus.authentication.util.AccountManager
import com.arcao.geocaching4locus.base.AbstractActionBarActivity
import com.arcao.geocaching4locus.base.util.exhaustive
import com.arcao.geocaching4locus.base.util.observe
import com.arcao.geocaching4locus.base.util.showLocusMissingError
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class UpdateMoreActivity : AbstractActionBarActivity() {
    private val viewModel by viewModel<UpdateMoreViewModel>()
    private val accountManager by inject<AccountManager>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.progress.observe(this, ::handleProgress)
        viewModel.action.observe(this, ::handleAction)

        if (savedInstanceState == null) {
            viewModel.processIntent(intent)
        }
    }

    @Suppress("IMPLICIT_CAST_TO_ANY")
    fun handleAction(action: UpdateMoreAction) {
        when (action) {
            UpdateMoreAction.SignIn -> {
                accountManager.requestSignOn(this, REQUEST_SIGN_ON)
            }
            is UpdateMoreAction.Error -> {
                Timber.d("UpdateMoreAction.Error intent: %s", action.intent)
                startActivity(action.intent)
                setResult(Activity.RESULT_CANCELED)
                onBackPressed()
            }
            UpdateMoreAction.Finish -> {
                Timber.d("UpdateMoreAction.Finish")
                setResult(Activity.RESULT_OK)
                finish()
            }
            UpdateMoreAction.Cancel -> {
                setResult(Activity.RESULT_CANCELED)
                onBackPressed()
            }
            UpdateMoreAction.LocusMapNotInstalled -> {
                showLocusMissingError()
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
                viewModel.processIntent(intent)
            } else {
                setResult(Activity.RESULT_CANCELED)
                onBackPressed()
            }
        }
    }

    companion object {
        private const val REQUEST_SIGN_ON = 1
    }
}
