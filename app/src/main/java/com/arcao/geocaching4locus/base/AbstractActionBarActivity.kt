package com.arcao.geocaching4locus.base

import androidx.appcompat.app.AppCompatActivity
import com.arcao.geocaching4locus.base.fragment.ProgressDialogFragment
import com.arcao.geocaching4locus.base.util.exhaustive
import com.arcao.geocaching4locus.base.util.getText

abstract class AbstractActionBarActivity : AppCompatActivity(), ProgressDialogFragment.DialogListener {
    @Suppress("IMPLICIT_CAST_TO_ANY")
    fun handleProgress(state: ProgressState) {
        val f = supportFragmentManager.findFragmentByTag(ProgressDialogFragment.FRAGMENT_TAG) as? ProgressDialogFragment

        when (state) {
            is ProgressState.ShowProgress -> {
                if (f != null && f.isShowing) {
                    f.updateProgress(
                        getText(state.message, state.messageArgs ?: emptyArray<Any>()),
                        state.progress,
                        state.maxProgress
                    )
                } else {
                    ProgressDialogFragment.newInstance(
                        state.requestId,
                        getText(state.message, state.messageArgs ?: emptyArray<Any>()),
                        state.progress,
                        state.maxProgress
                    ).show(supportFragmentManager, ProgressDialogFragment.FRAGMENT_TAG)
                    supportFragmentManager.executePendingTransactions()
                }
            }
            is ProgressState.HideProgress -> {
                f?.apply {
                    dismiss()
                }
                supportFragmentManager.executePendingTransactions()
            }
        }.exhaustive
    }

    override fun onProgressCancel(requestId: Int) {
    }
}
