package com.arcao.geocaching4locus.download_rectangle

import android.content.Intent

sealed class DownloadRectangleAction {
    object LocusMapNotInstalled : DownloadRectangleAction()
    object SignIn : DownloadRectangleAction()
    object Cancel : DownloadRectangleAction()
    object LastLiveMapDataInvalid : DownloadRectangleAction()
    data class Error(val intent: Intent) : DownloadRectangleAction()
    data class Finish(val intent: Intent) : DownloadRectangleAction()
}