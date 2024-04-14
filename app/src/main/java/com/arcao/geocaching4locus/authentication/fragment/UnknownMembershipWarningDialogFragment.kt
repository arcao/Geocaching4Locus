package com.arcao.geocaching4locus.authentication.fragment

import com.afollestad.materialdialogs.MaterialDialog
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.base.constants.AppConstants
import com.arcao.geocaching4locus.base.fragment.AbstractErrorDialogFragment
import com.arcao.geocaching4locus.base.util.showWebPage

class UnknownMembershipWarningDialogFragment : AbstractErrorDialogFragment() {
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
            .neutralButton(R.string.button_privacy_settings) {
                requireActivity().showWebPage(AppConstants.GEOCACHING_PRIVACY_SETTING_URI)
            }
    }

    companion object {
        fun newInstance(): UnknownMembershipWarningDialogFragment {
            val fragment = UnknownMembershipWarningDialogFragment()
            fragment.prepareDialog(
                R.string.title_unknown_member,
                R.string.warning_unknown_member
            )
            return fragment
        }
    }
}
