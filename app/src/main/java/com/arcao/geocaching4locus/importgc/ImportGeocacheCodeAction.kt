package com.arcao.geocaching4locus.importgc

import android.content.Intent

sealed class ImportGeocacheCodeAction {
    object SignIn : ImportGeocacheCodeAction()
    data class Error(val intent: Intent) : ImportGeocacheCodeAction()
    data class Finish(val intent: Intent) : ImportGeocacheCodeAction()
    object Cancel : ImportGeocacheCodeAction()
    object LocusMapNotInstalled : ImportGeocacheCodeAction()
    object RequestExternalStoragePermission : ImportGeocacheCodeAction()
    object GeocacheCodesInput : ImportGeocacheCodeAction()
}