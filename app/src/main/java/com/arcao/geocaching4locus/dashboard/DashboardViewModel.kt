package com.arcao.geocaching4locus.dashboard

import android.app.Application
import androidx.annotation.UiThread
import androidx.lifecycle.MutableLiveData
import com.arcao.geocaching4locus.authentication.util.isPremium
import com.arcao.geocaching4locus.base.BaseViewModel
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.util.AnalyticsManager
import com.arcao.geocaching4locus.base.util.Command
import com.arcao.geocaching4locus.base.util.hidePowerManagementWarning
import com.arcao.geocaching4locus.base.util.invoke
import com.arcao.geocaching4locus.data.account.AccountManager
import com.arcao.geocaching4locus.live_map.util.LiveMapNotificationManager
import locus.api.manager.LocusMapManager

class DashboardViewModel(
    private val calledFromLocusMap: Boolean,
    private val context: Application,
    private val notificationManager: LiveMapNotificationManager,
    private val accountManager: AccountManager,
    private val locusMapManager: LocusMapManager,
    analyticsManager: AnalyticsManager,
    dispatcherProvider: CoroutinesDispatcherProvider
) : BaseViewModel(dispatcherProvider), LiveMapNotificationManager.LiveMapStateChangeListener {
    val premium
        get() = accountManager.isPremium

    val action = Command<DashboardAction>()
    val liveMapEnabled = MutableLiveData<Boolean>()

    init {
        notificationManager.addLiveMapStateChangeListener(this)
        liveMapEnabled(notificationManager.isLiveMapEnabled)

        analyticsManager.actionDashboard(calledFromLocusMap)
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

    @UiThread
    fun onClickLiveMap() = mainLaunch {
        if (locusMapManager.isLocusMapNotInstalled) {
            action(DashboardAction.LocusMapNotInstalled)
            return@mainLaunch
        }

        if (accountManager.account == null) {
            action(DashboardAction.SignIn)
            return@mainLaunch
        }

        if (!context.hidePowerManagementWarning) {
            action(DashboardAction.WarnPowerSaveActive)
            return@mainLaunch
        }

        toggleLiveMap()
    }

    @UiThread
    private suspend fun toggleLiveMap() {
        computationContext {
            notificationManager.isLiveMapEnabled = !notificationManager.isLiveMapEnabled
        }

        if (calledFromLocusMap) {
            action(DashboardAction.NavigationBack)
        }
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

    fun onPowerSaveWarningConfirmed() = mainLaunch {
        toggleLiveMap()
    }
}
