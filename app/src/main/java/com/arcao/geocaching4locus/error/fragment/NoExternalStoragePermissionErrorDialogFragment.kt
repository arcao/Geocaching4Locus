package com.arcao.geocaching4locus.error.fragment

import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.base.fragment.AbstractErrorDialogFragment

class NoExternalStoragePermissionErrorDialogFragment : AbstractErrorDialogFragment() {

    override fun onPositiveButtonClick() {
        super.onPositiveButtonClick()
        if (requireArguments().getBoolean(PARAM_CLOSE_PARENT))
            requireActivity().finish()
    }

    companion object {
        @JvmField
        val FRAGMENT_TAG: String = NoExternalStoragePermissionErrorDialogFragment::class.java.name

        private const val PARAM_CLOSE_PARENT = "close_parent"

        @JvmStatic
        fun newInstance(closeParent: Boolean): NoExternalStoragePermissionErrorDialogFragment {
            return NoExternalStoragePermissionErrorDialogFragment().apply {
                prepareDialog(message = R.string.error_no_external_storage_permission)
                requireArguments().putBoolean(PARAM_CLOSE_PARENT, closeParent)
            }
        }
    }

}
