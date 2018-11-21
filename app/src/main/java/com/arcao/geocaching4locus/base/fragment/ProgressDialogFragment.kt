package com.arcao.geocaching4locus.base.fragment

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.annotation.NonNull
import androidx.core.os.bundleOf
import com.afollestad.materialdialogs.MaterialDialog
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.base.util.runIfIs

class ProgressDialogFragment : AbstractDialogFragment() {
    private var message: CharSequence = ""
    private var progress: Int = 0

    interface DialogListener {
        fun onProgressCancel()
    }

    override fun onCancel(dialog: DialogInterface?) {
        super.onCancel(dialog)

        activity.runIfIs(DialogListener::class) {
            onProgressCancel()
        }
    }

    @NonNull
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val arguments = requireArguments()

        message = savedInstanceState?.getCharSequence(STATE_MESSAGE) ?: arguments.getCharSequence(ARGS_MESSAGE, "")
        progress = savedInstanceState?.getInt(STATE_PROGRESS) ?: arguments.getInt(ARGS_PROGRESS)

        val maxProgress = arguments.getInt(ARGS_MAX_PROGRESS)

        return MaterialDialog.Builder(requireContext())
                .content(message)
                .negativeText(R.string.button_cancel)
                .progress(maxProgress == 0, maxProgress)
                .build().apply {
                    if (maxProgress != 0) setProgress(progress)
                }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putCharSequence(STATE_MESSAGE, message)
        outState.putInt(STATE_PROGRESS, progress)
    }

    fun updateProgress(message: CharSequence, progress: Int) {
        this.progress = progress
        this.message = message

        (dialog as MaterialDialog?)?.apply {
            if (!isIndeterminateProgress) setProgress(progress)
            setContent(message)
        }
    }

    companion object {
        val FRAGMENT_TAG: String = ProgressDialogFragment::class.java.name

        fun newInstance(message: CharSequence, progress: Int, maxProgress: Int) = ProgressDialogFragment().apply {
            arguments = bundleOf(
                    ARGS_MESSAGE to message,
                    ARGS_PROGRESS to progress,
                    ARGS_MAX_PROGRESS to maxProgress
            )
        }

        private const val ARGS_MESSAGE = "message"
        private const val ARGS_PROGRESS = "progress"
        private const val ARGS_MAX_PROGRESS = "max_progress"

        private const val STATE_MESSAGE = "message"
        private const val STATE_PROGRESS = "progress"
    }
}