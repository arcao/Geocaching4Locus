package com.arcao.geocaching4locus.search_nearest.fragment

import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.base.fragment.AbstractErrorDialogFragment

class NoLocationPermissionErrorDialogFragment : AbstractErrorDialogFragment() {
    companion object {
        fun newInstance() = NoLocationPermissionErrorDialogFragment().apply {
            prepareDialog(
                message = R.string.error_no_location_permission
            )
        }
    }
}
