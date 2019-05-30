package com.arcao.geocaching4locus.import_bookmarks.fragment

import android.content.Intent
import com.arcao.geocaching4locus.base.usecase.entity.GeocacheListEntity

sealed class BookmarkListAction {
    class LoadingError(val intent: Intent) : BookmarkListAction()
    class Error(val intent: Intent) : BookmarkListAction()
    class Finish(val intent: Intent) : BookmarkListAction()
    class ChooseBookmarks(val geocacheList: GeocacheListEntity) : BookmarkListAction()
    object Cancel : BookmarkListAction()
}