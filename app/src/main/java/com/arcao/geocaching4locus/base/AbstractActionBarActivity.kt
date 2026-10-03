package com.arcao.geocaching4locus.base

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.base.fragment.ProgressDialogFragment
import com.arcao.geocaching4locus.base.util.exhaustive
import com.arcao.geocaching4locus.base.util.getText

abstract class AbstractActionBarActivity : AppCompatActivity(), ProgressDialogFragment.DialogListener {
    override fun onContentChanged() {
        super.onContentChanged()
        applyWindowInsets()
    }

    /**
     * Apps targeting Android 15+ are displayed edge-to-edge, system bars are drawn over the app.
     * The toolbar is extended under the status bar and the content is moved away from
     * the navigation bar and display cutouts.
     */
    private fun applyWindowInsets() {
        val insetTypes = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()

        val toolbar = findViewById<Toolbar?>(R.id.toolbar)
        if (toolbar != null) {
            val toolbarHeight = toolbar.minimumHeight
            val toolbarPaddingTop = toolbar.paddingTop
            ViewCompat.setOnApplyWindowInsetsListener(toolbar) { view, insets ->
                val top = insets.getInsets(insetTypes).top
                view.updatePadding(top = toolbarPaddingTop + top)
                view.minimumHeight = toolbarHeight + top
                insets
            }
        }

        val content = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(content) { view, insets ->
            val bars = insets.getInsets(insetTypes)
            view.updatePadding(left = bars.left, right = bars.right, bottom = bars.bottom)
            insets
        }
    }

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
