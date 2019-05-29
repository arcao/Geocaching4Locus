package com.arcao.geocaching4locus.import_bookmarks

import com.arcao.geocaching4locus.authentication.util.isPremium
import com.arcao.geocaching4locus.base.BaseViewModel
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.usecase.entity.GeocacheListEntity
import com.arcao.geocaching4locus.base.util.Command
import com.arcao.geocaching4locus.base.util.invoke
import com.arcao.geocaching4locus.data.account.AccountManager
import locus.api.manager.LocusMapManager

class ImportBookmarkViewModel(
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

    fun chooseBookmarks(geocacheList: GeocacheListEntity) {
        action(ImportBookmarkAction.ChooseBookmark(geocacheList))
    }
}