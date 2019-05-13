package com.arcao.geocaching4locus.search_nearest.fragment

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings

import com.afollestad.materialdialogs.MaterialDialog
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.base.fragment.AbstractDialogFragment

class NoLocationProviderDialogFragment : AbstractDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        @Suppress("DEPRECATION")
        return MaterialDialog(requireActivity())
            .title(R.string.error_location_not_allowed)
            .message(R.string.error_location_disabled)
            .positiveButton(R.string.button_ok)
            .neutralButton(R.string.button_settings) {
                requireActivity().startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
    }

    companion object {
        fun newInstance(): AbstractDialogFragment {
            return NoLocationProviderDialogFragment()
        }
    }
}
