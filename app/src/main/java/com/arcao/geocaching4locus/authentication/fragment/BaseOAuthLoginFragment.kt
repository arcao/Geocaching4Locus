package com.arcao.geocaching4locus.authentication.fragment

import android.app.Activity
import androidx.fragment.app.Fragment
import com.arcao.geocaching4locus.authentication.LoginAction
import com.arcao.geocaching4locus.authentication.LoginViewModel
import com.arcao.geocaching4locus.base.util.exhaustive
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

abstract class BaseOAuthLoginFragment : Fragment() {
    val viewModel by sharedViewModel<LoginViewModel>()

    fun handleAction(action: LoginAction) {
        when (action) {
            is LoginAction.LoginUrlAvailable -> onLoginUrlAvailable(action.url)
            is LoginAction.Finish -> {
                finishAction(action.showBasicMembershipWarning)
            }
            is LoginAction.Error -> {
                startActivity(action.intent)
                cancelAction()
            }
            LoginAction.Cancel -> cancelAction()
        }.exhaustive
    }

    abstract fun onLoginUrlAvailable(url: String)

    fun cancelAction() {
        requireActivity().apply {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private fun finishAction(showBasicMembershipWarning: Boolean) {
        if (showBasicMembershipWarning) {
            BasicMembershipWarningDialogFragment.newInstance().show(requireFragmentManager())
            return
        }

        requireActivity().apply {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }
}