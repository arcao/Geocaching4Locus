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
        return MaterialDialog.Builder(activity!!)
            .title(R.string.error_location_not_allowed)
            .content(R.string.error_location_disabled)
            .positiveText(R.string.button_ok)
            .neutralText(R.string.button_settings)

            .onNeutral { materialDialog, dialogAction -> activity!!.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
            .build()
    }

    companion object {
        fun newInstance(): AbstractDialogFragment {
            return NoLocationProviderDialogFragment()
        }
    }
}
