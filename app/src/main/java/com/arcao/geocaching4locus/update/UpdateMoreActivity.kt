package com.arcao.geocaching4locus.update

import android.app.Activity
import android.os.Bundle
import com.arcao.geocaching4locus.authentication.LoginActivity
import com.arcao.geocaching4locus.base.AbstractActionBarActivity
import com.arcao.geocaching4locus.base.util.exhaustive
import com.arcao.geocaching4locus.base.util.showLocusMissingError
import com.arcao.geocaching4locus.base.util.withObserve
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class UpdateMoreActivity : AbstractActionBarActivity() {
    private val viewModel by viewModel<UpdateMoreViewModel>()

    private val loginActivity = registerForActivityResult(LoginActivity.Contract) { success ->
        if (success) {
            viewModel.processIntent(intent)
        } else {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.progress.withObserve(this, ::handleProgress)
        viewModel.action.withObserve(this, ::handleAction)

        if (savedInstanceState == null) {
            viewModel.processIntent(intent)
        }
    }

    @Suppress("IMPLICIT_CAST_TO_ANY")
    fun handleAction(action: UpdateMoreAction) {
        when (action) {
            UpdateMoreAction.SignIn -> loginActivity.launch(null)
            is UpdateMoreAction.Error -> {
                Timber.d("UpdateMoreAction.Error intent: %s", action.intent)
                startActivity(action.intent)
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
            UpdateMoreAction.Finish -> {
                Timber.d("UpdateMoreAction.Finish")
                setResult(Activity.RESULT_OK)
                finish()
            }
            UpdateMoreAction.Cancel -> {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
            UpdateMoreAction.LocusMapNotInstalled -> {
                showLocusMissingError()
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }.exhaustive
    }

    override fun onProgressCancel(requestId: Int) {
        viewModel.cancelProgress()
    }
}
