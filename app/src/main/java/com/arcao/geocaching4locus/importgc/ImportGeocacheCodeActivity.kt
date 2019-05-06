package com.arcao.geocaching4locus.importgc

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.annotation.NonNull
import com.arcao.geocaching4locus.authentication.util.AccountManager
import com.arcao.geocaching4locus.base.AbstractActionBarActivity
import com.arcao.geocaching4locus.base.util.PermissionUtil
import com.arcao.geocaching4locus.base.util.exhaustive
import com.arcao.geocaching4locus.base.util.observe
import com.arcao.geocaching4locus.base.util.showLocusMissingError
import com.arcao.geocaching4locus.error.fragment.ExternalStoragePermissionWarningDialogFragment
import com.arcao.geocaching4locus.error.fragment.NoExternalStoragePermissionErrorDialogFragment
import com.arcao.geocaching4locus.error.hasPositiveAction
import com.arcao.geocaching4locus.importgc.fragment.GeocacheCodesInputDialogFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ImportGeocacheCodeActivity : AbstractActionBarActivity(), GeocacheCodesInputDialogFragment.DialogListener {
    private val viewModel by viewModel<ImportGeocacheCodeViewModel>()
    private val accountManager by inject<AccountManager>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.progress.observe(this, ::handleProgress)
        viewModel.action.observe(this, ::handleAction)

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
            is ImportGeocacheCodeAction.RequestExternalStoragePermission -> {
                ExternalStoragePermissionWarningDialogFragment.newInstance().show(
                        supportFragmentManager,
                        ExternalStoragePermissionWarningDialogFragment.FRAGMENT_TAG
                )
            }
            ImportGeocacheCodeAction.GeocacheCodesInput -> {
                GeocacheCodesInputDialogFragment.newInstance()
                        .show(supportFragmentManager, GeocacheCodesInputDialogFragment.FRAGMENT_TAG)
            }
        }.exhaustive
    }

    @ExperimentalCoroutinesApi
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

    override fun onRequestPermissionsResult(requestCode: Int, @NonNull permissions: Array<String>, @NonNull grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PermissionUtil.REQUEST_EXTERNAL_STORAGE_PERMISSION) {
            if (PermissionUtil.verifyPermissions(grantResults)) {
                viewModel.init()
            } else {
                NoExternalStoragePermissionErrorDialogFragment.newInstance(true).show(supportFragmentManager, NoExternalStoragePermissionErrorDialogFragment.FRAGMENT_TAG)
            }
        }
    }

    companion object {
        private const val REQUEST_SIGN_ON = 1
    }
}
