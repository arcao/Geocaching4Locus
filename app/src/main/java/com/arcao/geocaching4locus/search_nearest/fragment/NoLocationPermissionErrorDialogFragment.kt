package com.arcao.geocaching4locus.search_nearest.fragment

import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.base.fragment.AbstractErrorDialogFragment
import com.arcao.geocaching4locus.error.fragment.LocusTestingErrorDialogFragment

class NoLocationPermissionErrorDialogFragment : AbstractErrorDialogFragment() {
    companion object {
        val FRAGMENT_TAG = LocusTestingErrorDialogFragment::class.java.name

        fun newInstance(): NoLocationPermissionErrorDialogFragment {
            val fragment = NoLocationPermissionErrorDialogFragment()
            fragment.prepareDialog(0, R.string.error_no_location_permission, null)

            return fragment
        }
    }
}
