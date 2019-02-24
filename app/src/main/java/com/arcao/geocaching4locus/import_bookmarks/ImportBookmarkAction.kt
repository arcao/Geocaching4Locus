package com.arcao.geocaching4locus.import_bookmarks

import com.arcao.geocaching4locus.base.usecase.entity.BookmarkListEntity

sealed class ImportBookmarkAction {
    object LocusMapNotInstalled : ImportBookmarkAction()
    object SignIn : ImportBookmarkAction()
    object ShowList : ImportBookmarkAction()
    class ChooseBookmark(val bookmarkList: BookmarkListEntity) : ImportBookmarkAction()
    object PremiumMembershipRequired : ImportBookmarkAction()
    object RequestExternalStoragePermission : ImportBookmarkAction()
}