package com.arcao.geocaching4locus.error.fragment

import android.content.Context
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.base.constants.AppConstants
import com.arcao.geocaching4locus.base.fragment.AbstractErrorDialogFragment
import locus.api.android.utils.LocusUtils

class LocusTestingErrorDialogFragment : AbstractErrorDialogFragment() {
    public override fun onPositiveButtonClick() {
        LocusUtils.callInstallLocus(requireContext())
        requireActivity().finish()
    }

    companion object {
        @JvmField
        val FRAGMENT_TAG: String = LocusTestingErrorDialogFragment::class.java.name

        @JvmStatic
        fun newInstance(context: Context): LocusTestingErrorDialogFragment {
            return LocusTestingErrorDialogFragment().apply {
                prepareDialog(
                    message = if (LocusUtils.isLocusAvailable(context))
                        R.string.error_old_locus_map
                    else
                        R.string.error_locus_not_found,
                    additionalMessage = AppConstants.LOCUS_MIN_VERSION
                )
            }
        }
    }
}
