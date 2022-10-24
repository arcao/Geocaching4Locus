package com.arcao.geocaching4locus.importgc

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import com.arcao.geocaching4locus.authentication.LoginActivity
import com.arcao.geocaching4locus.base.AbstractActionBarActivity
import com.arcao.geocaching4locus.base.util.exhaustive
import com.arcao.geocaching4locus.base.util.showLocusMissingError
import com.arcao.geocaching4locus.base.util.withObserve
import com.arcao.geocaching4locus.error.hasPositiveAction
import com.arcao.geocaching4locus.importgc.fragment.GeocacheCodesInputDialogFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class ImportGeocacheCodeActivity : AbstractActionBarActivity(), GeocacheCodesInputDialogFragment.DialogListener {
    private val viewModel by viewModel<ImportGeocacheCodeViewModel>()

    private val loginActivity = registerForActivityResult(LoginActivity.Contract) { success ->
        if (success) {
            viewModel.init(intent.getStringArrayExtra(PARAM_GEOCACHES))
        } else {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.progress.withObserve(this, ::handleProgress)
        viewModel.action.withObserve(this, ::handleAction)

        viewModel.init(intent.getStringArrayExtra(PARAM_GEOCACHES))
    }

    override fun onProgressCancel(requestId: Int) {
        viewModel.cancelImport()
    }

    @Suppress("IMPLICIT_CAST_TO_ANY")
    fun handleAction(action: ImportGeocacheCodeAction) {
        when (action) {
            ImportGeocacheCodeAction.SignIn -> loginActivity.launch(null)
            is ImportGeocacheCodeAction.Error -> {
                startActivity(action.intent)
                setResult(
                    if (intent.hasPositiveAction())
                        Activity.RESULT_OK
                    else
                        Activity.RESULT_CANCELED
                )
                finish()
            }
            is ImportGeocacheCodeAction.Finish -> {
                startActivity(action.intent)
                setResult(Activity.RESULT_OK)
                finish()
            }
            ImportGeocacheCodeAction.Cancel -> {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
            is ImportGeocacheCodeAction.LocusMapNotInstalled -> {
                showLocusMissingError()
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
            ImportGeocacheCodeAction.GeocacheCodesInput -> {
                GeocacheCodesInputDialogFragment.newInstance().show(supportFragmentManager)
            }
        }.exhaustive
    }

    override fun onInputFinished(input: Array<String>) {
        viewModel.importGeocacheCodes(input)
    }

    companion object {
        private const val PARAM_GEOCACHES = "com.arcao.geocaching4locus.GEOCACHES"
    }

    object Contract : ActivityResultContract<Void?, Boolean>() {
        override fun createIntent(context: Context, input: Void?) =
            Intent(context, ImportGeocacheCodeActivity::class.java)

        override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
            return resultCode == Activity.RESULT_OK
        }
    }
}
