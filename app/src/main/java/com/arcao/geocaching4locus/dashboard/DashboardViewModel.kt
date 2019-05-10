package com.arcao.geocaching4locus.dashboard

import android.content.Context
import androidx.annotation.UiThread
import androidx.lifecycle.MutableLiveData
import com.arcao.geocaching4locus.authentication.util.AccountManager
import com.arcao.geocaching4locus.base.BaseViewModel
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.util.*
import com.arcao.geocaching4locus.live_map.util.LiveMapNotificationManager
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val calledFromLocusMap: Boolean,
    private val context: Context,
    private val notificationManager: LiveMapNotificationManager,
    private val accountManager: AccountManager,
    dispatcherProvider: CoroutinesDispatcherProvider
) : BaseViewModel(dispatcherProvider), LiveMapNotificationManager.LiveMapStateChangeListener {
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

    @UiThread
    fun onClickLiveMap() = mainLaunch {
        if (context.isLocusNotInstalled()) {
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

    fun onPowerSaveWarningConfirmed() = launch {
        toggleLiveMap()
    }
}
