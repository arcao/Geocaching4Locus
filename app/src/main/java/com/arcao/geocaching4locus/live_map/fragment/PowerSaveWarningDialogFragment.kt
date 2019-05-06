package com.arcao.geocaching4locus.live_map.fragment

import com.afollestad.materialdialogs.MaterialDialog
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.base.constants.AppConstants
import com.arcao.geocaching4locus.base.fragment.AbstractErrorDialogFragment
import com.arcao.geocaching4locus.base.util.hidePowerManagementWarning
import com.arcao.geocaching4locus.base.util.showWebPage

class PowerSaveWarningDialogFragment : AbstractErrorDialogFragment() {
    interface OnPowerSaveWarningConfirmedListener {
        fun onPowerSaveWarningConfirmed()
    }

    override fun onPositiveButtonClick() {
        val activity = requireActivity()

        if (activity is OnPowerSaveWarningConfirmedListener) {
            activity.onPowerSaveWarningConfirmed()
        }
    }

    override fun onDialogBuild(builder: MaterialDialog.Builder) {
        super.onDialogBuild(builder)

        builder.neutralText(R.string.button_more_info)
            .onNeutral { _, _ -> requireActivity().showWebPage(AppConstants.POWER_SAVE_INFO_URI) }

        builder.checkBoxPromptRes(R.string.checkbox_do_not_show_again, false) { _, isChecked ->
            requireContext().hidePowerManagementWarning = isChecked
        }
    }

    companion object {
        @JvmField
        val FRAGMENT_TAG: String = PowerSaveWarningDialogFragment::class.java.name

        @JvmStatic
        fun newInstance(): PowerSaveWarningDialogFragment {
            return PowerSaveWarningDialogFragment().apply {
                prepareDialog(R.string.title_warning, R.string.warning_power_management)
            }
        }
    }
}
