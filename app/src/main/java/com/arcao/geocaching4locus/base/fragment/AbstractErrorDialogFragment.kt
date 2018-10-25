package com.arcao.geocaching4locus.base.fragment

import android.app.Dialog
import android.os.Bundle
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import com.afollestad.materialdialogs.MaterialDialog
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.base.util.ResourcesUtil

abstract class AbstractErrorDialogFragment : AbstractDialogFragment() {

    protected fun prepareDialog(@StringRes resTitle: Int, @StringRes resErrorMessage: Int, @Nullable additionalMessage: CharSequence) {
        arguments = bundleOf(
                PARAM_TITLE to resTitle,
                PARAM_ERROR_MESSAGE to resErrorMessage,
                PARAM_ADDITIONAL_MESSAGE to additionalMessage
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isCancelable = false
    }

    protected open fun onPositiveButtonClick() {
        // do nothing
    }

    protected open fun onDialogBuild(builder: MaterialDialog.Builder) {
        val args = requireArguments()

        builder.content(ResourcesUtil.getText(requireContext(), args.getInt(PARAM_ERROR_MESSAGE),
                args.getString(PARAM_ADDITIONAL_MESSAGE) ?: ""))
                .positiveText(R.string.button_ok)
                .onPositive { _, _ -> onPositiveButtonClick() }

        val title = args.getInt(PARAM_TITLE)
        if (title != 0) {
            builder.title(title)
        }
    }

    @NonNull
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialDialog.Builder(requireActivity())
        onDialogBuild(builder)
        return builder.build()
    }

    companion object {
        private const val PARAM_TITLE = "TITLE"
        private const val PARAM_ERROR_MESSAGE = "ERROR_MESSAGE"
        private const val PARAM_ADDITIONAL_MESSAGE = "ADDITIONAL_MESSAGE"
    }
}
