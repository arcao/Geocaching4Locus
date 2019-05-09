package com.arcao.geocaching4locus.live_map.fragment

import android.annotation.SuppressLint
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.checkbox.checkBoxPrompt
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
        dismiss()
    }

    @Suppress("DEPRECATION")
    @SuppressLint("CheckResult")
    override fun onDialogBuild(dialog: MaterialDialog) {
        super.onDialogBuild(dialog)

        dialog.noAutoDismiss()
            .neutralButton(R.string.button_more_info) {
                requireActivity().showWebPage(AppConstants.POWER_SAVE_INFO_URI)
            }.checkBoxPrompt(R.string.checkbox_do_not_show_again) { checked ->
                requireContext().hidePowerManagementWarning = checked
            }
    }

    companion object {
        fun newInstance(): PowerSaveWarningDialogFragment {
            return PowerSaveWarningDialogFragment().apply {
                prepareDialog(R.string.title_warning, R.string.warning_power_management)
            }
        }
    }
}
