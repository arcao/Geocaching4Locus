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

    override fun onDialogBuild(builder: MaterialDialog.Builder) {
        super.onDialogBuild(builder)

        // disable auto dismiss
        builder.autoDismiss(false)

        builder.neutralText(R.string.button_show_users_guide)
            .onNeutral { _, _ -> requireActivity().showWebPage(AppConstants.USERS_GUIDE_URI) }
    }

    companion object {
        val FRAGMENT_TAG: String = BasicMembershipWarningDialogFragment::class.java.name

        fun newInstance(): BasicMembershipWarningDialogFragment {
            val fragment = BasicMembershipWarningDialogFragment()
            fragment.prepareDialog(R.string.title_basic_member_warning, R.string.warning_basic_member, null)
            return fragment
        }
    }
}
