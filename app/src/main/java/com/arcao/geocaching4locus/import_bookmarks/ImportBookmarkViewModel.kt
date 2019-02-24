package com.arcao.geocaching4locus.import_bookmarks

import android.content.Context
import com.arcao.geocaching4locus.authentication.util.AccountManager
import com.arcao.geocaching4locus.base.BaseViewModel
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.usecase.entity.BookmarkListEntity
import com.arcao.geocaching4locus.base.util.Command
import com.arcao.geocaching4locus.base.util.hasExternalStoragePermission
import com.arcao.geocaching4locus.base.util.invoke
import com.arcao.geocaching4locus.base.util.isLocusNotInstalled

class ImportBookmarkViewModel(
    private val context: Context,
    private val accountManager: AccountManager,
    dispatcherProvider: CoroutinesDispatcherProvider
) : BaseViewModel(dispatcherProvider) {
    val action = Command<ImportBookmarkAction>()

    fun init() {
        if (context.isLocusNotInstalled()) {
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

        if (!context.hasExternalStoragePermission) {
            action(ImportBookmarkAction.RequestExternalStoragePermission)
            return
        }

        action(ImportBookmarkAction.ShowList)
    }

    fun chooseBookmarks(bookmarkList: BookmarkListEntity) {
        action(ImportBookmarkAction.ChooseBookmark(bookmarkList))
    }
}