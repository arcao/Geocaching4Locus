package com.arcao.geocaching4locus.dashboard

sealed class DashboardAction {
    object SearchNearest : DashboardAction()
    object ImportGcCode : DashboardAction()
    object ImportBookmarks : DashboardAction()
    object DownloadLiveMapGeocaches : DashboardAction()
    object UsersGuide : DashboardAction()
    object Preferences : DashboardAction()
    object NavigationBack : DashboardAction()
    object LocusMapNotInstalled : DashboardAction()
    object SignIn : DashboardAction()
    object WarnPowerSaveActive : DashboardAction()
}