package com.arcao.geocaching4locus.update

import android.content.Intent

sealed class UpdateMoreAction {
    object SignIn : UpdateMoreAction()
    class Error(val intent: Intent) : UpdateMoreAction()
    object Finish : UpdateMoreAction()
    object Cancel : UpdateMoreAction()
    object LocusMapNotInstalled : UpdateMoreAction()
}