package com.arcao.geocaching4locus.import_bookmarks.fragment

import android.content.Intent

sealed class BookmarkAction {
    data class Error(val intent: Intent) : BookmarkAction()
    data class Finish(val intent: Intent) : BookmarkAction()
    object Cancel : BookmarkAction()
}