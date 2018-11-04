package com.arcao.geocaching4locus.weblink.fragment

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.annotation.NonNull
import com.afollestad.materialdialogs.MaterialDialog
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.base.fragment.AbstractDialogFragment
import com.arcao.geocaching4locus.base.util.ifIs

class WebLinkProgressDialogFragment : AbstractDialogFragment() {
    interface DialogListener {
        fun onProgressCancel()
    }

    override fun onCancel(dialog: DialogInterface?) {
        super.onCancel(dialog)

        activity.ifIs(DialogListener::class) {
            onProgressCancel()
        }
    }

    @NonNull
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialDialog.Builder(requireContext())
                .content(R.string.progress_download_geocache)
                .negativeText(R.string.button_cancel)
                .progress(true, 0)
                .build()
    }

    companion object {
        val FRAGMENT_TAG: String = WebLinkProgressDialogFragment::class.java.name

        fun newInstance(): WebLinkProgressDialogFragment = WebLinkProgressDialogFragment()
    }
}
