package com.arcao.geocaching4locus.dashboard

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.arcao.geocaching4locus.authentication.util.AccountManager
import com.arcao.geocaching4locus.base.BaseViewModel
import com.arcao.geocaching4locus.base.util.*
import com.arcao.geocaching4locus.live_map.util.LiveMapNotificationManager
import org.koin.standalone.inject

class DashboardViewModel(private val calledFromLocusMap: Boolean) : BaseViewModel(), LiveMapNotificationManager.LiveMapStateChangeListener {
    private val context by inject<Context>()
    private val notificationManager by inject<LiveMapNotificationManager>()
    private val accountManager by inject<AccountManager>()
    val premium by lazy { accountManager.isPremium }

    val action = Command<DashboardAction>()
    val liveMapEnabled = MutableLiveData<Boolean>()

    init {
        notificationManager.addLiveMapStateChangeListener(this)
        liveMapEnabled(notificationManager.isLiveMapEnabled)

        AnalyticsUtil.actionDashboard(calledFromLocusMap)
    }

    fun onClickSearchNearest() {
        action(DashboardAction.SearchNearest)
    }

    fun onClickImportGcCode() {
        action(DashboardAction.ImportGcCode)
    }

    fun onClickImportBookmarks() {
        action(DashboardAction.ImportBookmarks)
    }

    fun onClickLiveMap() {
        if (context.isLocusNotInstalled()) {
            action(DashboardAction.LocusMapNotInstalled)
            return
        }

        if (accountManager.account != null) {
            action(DashboardAction.SignIn)
            return
        }

        if (!context.hidePowerManagementWarning) {
            action(DashboardAction.WarnPowerSaveActive)
        }

        onPowerSaveWarningConfirmed()
    }

    fun onClickImportLiveMapGc() {
        action(DashboardAction.DownloadLiveMapGeocaches)
    }

    fun onClickPreferences() {
        action(DashboardAction.Preferences)
    }

    fun onClickUsersGuide() {
        action(DashboardAction.UsersGuide)
    }

    fun onClickNavigationBack() {
        action(DashboardAction.NavigationBack)
    }

    override fun onCleared() {
        notificationManager.removeLiveMapStateChangeListener(this)
        super.onCleared()
    }

    override fun onLiveMapStateChange(newState: Boolean) {
        liveMapEnabled(newState)
    }

    fun onPowerSaveWarningConfirmed() {
        notificationManager.isLiveMapEnabled = !notificationManager.isLiveMapEnabled

        if (calledFromLocusMap) action(DashboardAction.NavigationBack)
    }
}
