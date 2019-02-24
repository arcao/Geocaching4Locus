package com.arcao.geocaching4locus.import_bookmarks.fragment

import android.content.Intent
import com.arcao.geocaching4locus.base.usecase.entity.BookmarkListEntity

sealed class BookmarkListAction {
    class Error(val intent: Intent) : BookmarkListAction()
    class Finish(val intent: Intent) : BookmarkListAction()
    class ChooseBookmarks(val bookmarkList: BookmarkListEntity) : BookmarkListAction()
    object Cancel : BookmarkListAction()
}