package com.arcao.geocaching4locus.importgc

import android.app.Activity
import android.os.Bundle
import com.arcao.geocaching4locus.authentication.LoginActivity
import com.arcao.geocaching4locus.base.AbstractActionBarActivity
import com.arcao.geocaching4locus.base.util.exhaustive
import com.arcao.geocaching4locus.base.util.showLocusMissingError
import com.arcao.geocaching4locus.base.util.withObserve
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class ImportUrlActivity : AbstractActionBarActivity() {
    private val viewModel by viewModel<ImportUrlViewModel>()

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

        viewModel.action.withObserve(this, ::handleAction)
        viewModel.progress.withObserve(this, ::handleProgress)

        if (savedInstanceState == null) {
            processIntent()
        }
    }

    override fun onProgressCancel(requestId: Int) {
        viewModel.cancelImport()
    }

    private fun processIntent() {
        val uri = intent.data
        if (uri == null) {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }

        viewModel.startImport(uri)
    }

    @Suppress("IMPLICIT_CAST_TO_ANY")
    private fun handleAction(action: ImportUrlAction) {
        Timber.v("handleAction: %s", action)

        when (action) {
            is ImportUrlAction.Cancel -> {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
            is ImportUrlAction.Error -> {
                startActivity(action.intent)
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
            is ImportUrlAction.Finish -> {
                startActivity(action.intent)
                setResult(Activity.RESULT_OK)
                finish()
            }
            is ImportUrlAction.LocusMapNotInstalled -> {
                showLocusMissingError()
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
            is ImportUrlAction.SignIn -> loginActivity.launch(null)
        }.exhaustive
    }
}
