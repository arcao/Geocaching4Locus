package com.arcao.geocaching4locus.base.fragment

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.core.os.bundleOf
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.base.util.runIfIs

class ProgressDialogFragment : AbstractDialogFragment() {
    private var requestId = 0
    private var message: CharSequence = ""
    private var progress: Int = 0
    private var maxProgress: Int = 0

    interface DialogListener {
        fun onProgressCancel(requestId: Int)
    }

    override fun onCancel(dialog: DialogInterface?) {
        super.onCancel(dialog)

        performCancel()
    }

    private fun performCancel() {
        activity.runIfIs(DialogListener::class) {
            onProgressCancel(requestId)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val arguments = requireArguments()

        requestId = arguments.getInt(ARGS_REQUEST_ID, 0)
    }

    @NonNull
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val arguments = requireArguments()

        message = savedInstanceState?.getCharSequence(STATE_MESSAGE)
                ?: arguments.getCharSequence(ARGS_MESSAGE, "")
        progress = savedInstanceState?.getInt(STATE_PROGRESS) ?: arguments.getInt(ARGS_PROGRESS)
        maxProgress = savedInstanceState?.getInt(STATE_MAX_PROGRESS)
                ?: arguments.getInt(ARGS_MAX_PROGRESS)

        val dialog = MaterialDialog(requireContext())
                .message(text = message)
                .negativeButton(R.string.button_cancel) {
                    performCancel()
                }
                .cancelOnTouchOutside(false)
                .customView(R.layout.dialog_progress)

        val view = dialog.getCustomView()
        updateProgressViews(view, progress, maxProgress)

        return dialog
    }

    private fun updateProgressViews(view: View, progress: Int, maxProgress: Int) {
        val progressView = view.findViewById<ProgressBar>(R.id.progressBar)
        val percentView = view.findViewById<TextView>(R.id.percent)
        val counterView = view.findViewById<TextView>(R.id.counter)

        if (progress == 0 || maxProgress == 0) {
            progressView.isIndeterminate = true

            percentView.visibility = View.GONE
            counterView.visibility = View.GONE
        } else {
            progressView.isIndeterminate = false
            progressView.max = maxProgress
            progressView.progress = progress

            percentView.text = "%d%%".format(progress * 100 / maxProgress)
            percentView.visibility = View.VISIBLE

            counterView.text = "%d / %d".format(progress, maxProgress)
            counterView.visibility = View.VISIBLE
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putCharSequence(STATE_MESSAGE, message)
        outState.putInt(STATE_PROGRESS, progress)
        outState.putInt(STATE_MAX_PROGRESS, maxProgress)
    }

    fun updateProgress(message: CharSequence, progress: Int, maxProgress: Int) {
        this.progress = progress
        this.message = message
        this.maxProgress = maxProgress

        (dialog as? MaterialDialog)?.apply {
            updateProgressViews(getCustomView(), progress, maxProgress)
            message(text = message)
        }
    }

    companion object {
        val FRAGMENT_TAG: String = ProgressDialogFragment::class.java.name

        fun newInstance(requestId: Int, message: CharSequence, progress: Int, maxProgress: Int) =
                ProgressDialogFragment().apply {
                    arguments = bundleOf(
                            ARGS_REQUEST_ID to requestId,
                            ARGS_MESSAGE to message,
                            ARGS_PROGRESS to progress,
                            ARGS_MAX_PROGRESS to maxProgress
                    )
                }

        private const val ARGS_REQUEST_ID = "requestId"
        private const val ARGS_MESSAGE = "message"
        private const val ARGS_PROGRESS = "progress"
        private const val ARGS_MAX_PROGRESS = "max_progress"

        private const val STATE_MESSAGE = "message"
        private const val STATE_PROGRESS = "progress"
        private const val STATE_MAX_PROGRESS = "max_progress"
    }
}