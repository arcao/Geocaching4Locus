package com.arcao.geocaching4locus.importgc

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.annotation.NonNull
import com.arcao.geocaching4locus.authentication.util.requestSignOn
import com.arcao.geocaching4locus.base.AbstractActionBarActivity
import com.arcao.geocaching4locus.base.util.exhaustive
import com.arcao.geocaching4locus.base.util.showLocusMissingError
import com.arcao.geocaching4locus.base.util.withObserve
import com.arcao.geocaching4locus.data.account.AccountManager
import com.arcao.geocaching4locus.error.hasPositiveAction
import com.arcao.geocaching4locus.importgc.fragment.GeocacheCodesInputDialogFragment
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ImportGeocacheCodeActivity : AbstractActionBarActivity(), GeocacheCodesInputDialogFragment.DialogListener {
    private val viewModel by viewModel<ImportGeocacheCodeViewModel>()
    private val accountManager by inject<AccountManager>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.progress.withObserve(this, ::handleProgress)
        viewModel.action.withObserve(this, ::handleAction)

        viewModel.init()
    }

    override fun onProgressCancel(requestId: Int) {
        viewModel.cancelImport()
    }

    @Suppress("IMPLICIT_CAST_TO_ANY")
    fun handleAction(action: ImportGeocacheCodeAction) {
        when (action) {
            ImportGeocacheCodeAction.SignIn -> {
                accountManager.requestSignOn(this, REQUEST_SIGN_ON)
            }
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

    override fun onInputFinished(@NonNull input: Array<String>) {
        viewModel.importGeocacheCodes(input)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // restart update process after log in
        if (requestCode == REQUEST_SIGN_ON) {
            if (resultCode == Activity.RESULT_OK) {
                viewModel.init()
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
