package com.arcao.geocaching4locus.dashboard.fragment

import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.base.fragment.AbstractErrorDialogFragment

class NoPostNotificationPermissionErrorDialogFragment : AbstractErrorDialogFragment() {
    companion object {
        fun newInstance() = NoPostNotificationPermissionErrorDialogFragment().apply {
            prepareDialog(
                message = R.string.error_no_post_notification_permission
            )
        }
    }
}
