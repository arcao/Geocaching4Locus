package com.arcao.geocaching4locus.download_rectangle

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.authentication.util.requestSignOn
import com.arcao.geocaching4locus.base.AbstractActionBarActivity
import com.arcao.geocaching4locus.base.util.exhaustive
import com.arcao.geocaching4locus.base.util.showLocusMissingError
import com.arcao.geocaching4locus.data.account.AccountManager
import com.arcao.geocaching4locus.error.ErrorActivity
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class DownloadRectangleActivity : AbstractActionBarActivity() {
    val viewModel by viewModel<DownloadRectangleViewModel>()
    private val accountManager by inject<AccountManager>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.progress.observe(this, ::handleProgress)
        viewModel.action.observe(this, ::handleAction)

        if (savedInstanceState == null)
            viewModel.startDownload()
    }

    override fun onProgressCancel(requestId: Int) {
        viewModel.cancelDownload()
    }

    @Suppress("IMPLICIT_CAST_TO_ANY")
    fun handleAction(action: DownloadRectangleAction) {
        when (action) {
            is DownloadRectangleAction.Cancel -> {
                setResult(Activity.RESULT_CANCELED)
                onBackPressed()
            }
            is DownloadRectangleAction.Error -> {
                startActivity(action.intent)
                setResult(Activity.RESULT_CANCELED)
                onBackPressed()
            }
            is DownloadRectangleAction.Finish -> {
                startActivity(action.intent)
                setResult(Activity.RESULT_OK)
                finish()
            }
            is DownloadRectangleAction.LocusMapNotInstalled -> {
                showLocusMissingError()
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
            is DownloadRectangleAction.SignIn -> {
                accountManager.requestSignOn(this, REQUEST_SIGN_ON)
            }
            DownloadRectangleAction.LastLiveMapDataInvalid -> {
                startActivity(ErrorActivity.IntentBuilder(this).message(R.string.error_live_map_geocaches_not_visible).build())
                finish()
            }
        }.exhaustive
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // restart update process after log in
        if (requestCode == REQUEST_SIGN_ON) {
            if (resultCode == Activity.RESULT_OK) {
                viewModel.startDownload()
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
