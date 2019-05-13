package com.arcao.geocaching4locus.import_bookmarks

import android.content.Context
import com.arcao.geocaching4locus.authentication.util.AccountManager
import com.arcao.geocaching4locus.base.BaseViewModel
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.usecase.entity.BookmarkListEntity
import com.arcao.geocaching4locus.base.util.Command
import com.arcao.geocaching4locus.base.util.invoke
import locus.api.manager.LocusMapManager

class ImportBookmarkViewModel(
    private val context: Context,
    private val accountManager: AccountManager,
    private val locusMapManager: LocusMapManager,
    dispatcherProvider: CoroutinesDispatcherProvider
) : BaseViewModel(dispatcherProvider) {
    val action = Command<ImportBookmarkAction>()

    fun init() {
        if (locusMapManager.isLocusMapNotInstalled) {
            action(ImportBookmarkAction.LocusMapNotInstalled)
            return
        }

        if (accountManager.account == null) {
            action(ImportBookmarkAction.SignIn)
            return
        }

        if (!accountManager.isPremium) {
            action(ImportBookmarkAction.PremiumMembershipRequired)
            return
        }

        action(ImportBookmarkAction.ShowList)
    }

    fun chooseBookmarks(bookmarkList: BookmarkListEntity) {
        action(ImportBookmarkAction.ChooseBookmark(bookmarkList))
    }
}