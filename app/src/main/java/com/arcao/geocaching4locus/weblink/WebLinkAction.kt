package com.arcao.geocaching4locus.weblink

import android.content.Intent
import android.net.Uri

sealed class WebLinkAction {
    object SignIn : WebLinkAction()
    data class ResolvedUri(val uri : Uri) : WebLinkAction()
    data class Error(val intent : Intent) : WebLinkAction()
    object NavigationBack : WebLinkAction()
    object PremiumMembershipRequired : WebLinkAction()
}

