package com.arcao.geocaching4locus.download_rectangle

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.authentication.LoginActivity
import com.arcao.geocaching4locus.base.AbstractActionBarActivity
import com.arcao.geocaching4locus.base.util.exhaustive
import com.arcao.geocaching4locus.base.util.showLocusMissingError
import com.arcao.geocaching4locus.base.util.withObserve
import com.arcao.geocaching4locus.error.ErrorActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class DownloadRectangleActivity : AbstractActionBarActivity() {
    val viewModel by viewModel<DownloadRectangleViewModel>()

    private val loginActivity = registerForActivityResult(LoginActivity.Contract) { success ->
        if (success) {
            viewModel.startDownload()
        } else {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.progress.withObserve(this, ::handleProgress)
        viewModel.action.withObserve(this, ::handleAction)

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
                finish()
            }
            is DownloadRectangleAction.Error -> {
                startActivity(action.intent)
                setResult(Activity.RESULT_CANCELED)
                finish()
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
            is DownloadRectangleAction.SignIn -> loginActivity.launch(null)
            DownloadRectangleAction.LastLiveMapDataInvalid -> {
                startActivity(
                    ErrorActivity.IntentBuilder(this)
                        .message(R.string.error_live_map_geocaches_not_visible).build()
                )
                finish()
            }
        }.exhaustive
    }

    object Contract : ActivityResultContract<Void?, Boolean>() {
        override fun createIntent(context: Context, input: Void?) =
            Intent(context, DownloadRectangleActivity::class.java)

        override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
            return resultCode == Activity.RESULT_OK
        }
    }
}
