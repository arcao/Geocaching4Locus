package com.arcao.geocaching4locus.authentication.fragment

import com.afollestad.materialdialogs.MaterialDialog
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.base.constants.AppConstants
import com.arcao.geocaching4locus.base.fragment.AbstractErrorDialogFragment
import com.arcao.geocaching4locus.base.util.showWebPage

class BasicMembershipWarningDialogFragment : AbstractErrorDialogFragment() {

    override fun onPositiveButtonClick() {
        super.onPositiveButtonClick()
        requireActivity().finish()
        dismiss()
    }

    @Suppress("DEPRECATION")
    override fun onDialogBuild(dialog: MaterialDialog) {
        super.onDialogBuild(dialog)

        // disable auto dismiss
        dialog.noAutoDismiss()
            .neutralButton(R.string.button_show_users_guide) {
                requireActivity().showWebPage(AppConstants.USERS_GUIDE_URI)
            }
    }

    companion object {
        fun newInstance(): BasicMembershipWarningDialogFragment {
            val fragment = BasicMembershipWarningDialogFragment()
            fragment.prepareDialog(R.string.title_basic_member_warning, R.string.warning_basic_member, null)
            return fragment
        }
    }
}
