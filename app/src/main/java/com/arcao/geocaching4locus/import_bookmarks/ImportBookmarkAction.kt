package com.arcao.geocaching4locus.import_bookmarks

import com.arcao.geocaching4locus.base.usecase.entity.GeocacheListEntity

sealed class ImportBookmarkAction {
    object LocusMapNotInstalled : ImportBookmarkAction()
    object SignIn : ImportBookmarkAction()
    object ShowList : ImportBookmarkAction()
    class ChooseBookmark(val geocacheList: GeocacheListEntity) : ImportBookmarkAction()
    object PremiumMembershipRequired : ImportBookmarkAction()
}