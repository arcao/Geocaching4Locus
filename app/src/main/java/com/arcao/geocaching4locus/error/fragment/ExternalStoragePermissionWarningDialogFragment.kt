package com.arcao.geocaching4locus.error.fragment

import androidx.core.app.ActivityCompat
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.base.fragment.AbstractErrorDialogFragment
import com.arcao.geocaching4locus.base.util.PermissionUtil

class ExternalStoragePermissionWarningDialogFragment : AbstractErrorDialogFragment() {
    override fun onPositiveButtonClick() {
        super.onPositiveButtonClick()
        ActivityCompat.requestPermissions(
            requireActivity(),
            PermissionUtil.PERMISSION_EXTERNAL_STORAGE,
            PermissionUtil.REQUEST_EXTERNAL_STORAGE_PERMISSION
        )
    }

    companion object {
        fun newInstance(): ExternalStoragePermissionWarningDialogFragment {
            return ExternalStoragePermissionWarningDialogFragment().apply {
                prepareDialog(message = R.string.warning_external_storage_permission)
            }
        }
    }
}
