package com.arcao.geocaching4locus.error.fragment

import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.base.fragment.AbstractErrorDialogFragment

class NoExternalStoragePermissionErrorDialogFragment : AbstractErrorDialogFragment() {

    override fun onPositiveButtonClick() {
        super.onPositiveButtonClick()
        if (requireArguments().getBoolean(ARGS_CLOSE_PARENT))
            requireActivity().finish()
    }

    companion object {
        private const val ARGS_CLOSE_PARENT = "close_parent"

        fun newInstance(closeParent: Boolean): NoExternalStoragePermissionErrorDialogFragment {
            return NoExternalStoragePermissionErrorDialogFragment().apply {
                prepareDialog(message = R.string.error_no_external_storage_permission)
                requireArguments().putBoolean(ARGS_CLOSE_PARENT, closeParent)
            }
        }
    }
}
