package com.arcao.geocaching4locus.importgc

import android.content.Intent

sealed class ImportUrlAction {
    object SignIn : ImportUrlAction()
    data class Error(val intent: Intent) : ImportUrlAction()
    data class Finish(val intent: Intent) : ImportUrlAction()
    object Cancel : ImportUrlAction()
    object LocusMapNotInstalled : ImportUrlAction()
    object RequestExternalStoragePermission : ImportUrlAction()
}