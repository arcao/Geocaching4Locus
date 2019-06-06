package com.arcao.geocaching4locus.update

import android.content.Intent

sealed class UpdateAction {
    object SignIn : UpdateAction()
    class Error(val intent: Intent) : UpdateAction()
    class Finish(val intent: Intent? = null) : UpdateAction()
    object Cancel : UpdateAction()
    object LocusMapNotInstalled : UpdateAction()
    object PremiumMembershipRequired : UpdateAction()
}