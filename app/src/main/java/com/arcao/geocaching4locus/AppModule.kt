package com.arcao.geocaching4locus

import com.arcao.geocaching4locus.authentication.LoginViewModel
import com.arcao.geocaching4locus.authentication.usecase.CreateAccountUseCase
import com.arcao.geocaching4locus.authentication.usecase.RetrieveAuthorizationUrlUseCase
import com.arcao.geocaching4locus.authentication.usecase.UpdateAccountUseCase
import com.arcao.geocaching4locus.authentication.util.PreferenceAccountManager
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.usecase.GeocachingApiFilterProvider
import com.arcao.geocaching4locus.base.usecase.GeocachingApiLoginUseCase
import com.arcao.geocaching4locus.base.usecase.GetGeocacheCodeFromGuidUseCase
import com.arcao.geocaching4locus.base.usecase.GetGeocachingLogsUseCase
import com.arcao.geocaching4locus.base.usecase.GetGeocachingTrackablesUseCase
import com.arcao.geocaching4locus.base.usecase.GetGpsLocationUseCase
import com.arcao.geocaching4locus.base.usecase.GetLastKnownLocationUseCase
import com.arcao.geocaching4locus.base.usecase.GetListGeocachesUseCase
import com.arcao.geocaching4locus.base.usecase.GetLiveMapPointsFromRectangleCoordinatesUseCase
import com.arcao.geocaching4locus.base.usecase.GetOldPointNewPointPairFromPointUseCase
import com.arcao.geocaching4locus.base.usecase.GetPointFromGeocacheCodeUseCase
import com.arcao.geocaching4locus.base.usecase.GetPointsFromCoordinatesUseCase
import com.arcao.geocaching4locus.base.usecase.GetPointsFromGeocacheCodesUseCase
import com.arcao.geocaching4locus.base.usecase.GetPointsFromPointIndexesUseCase
import com.arcao.geocaching4locus.base.usecase.GetPointsFromRectangleCoordinatesUseCase
import com.arcao.geocaching4locus.base.usecase.GetUserListsUseCase
import com.arcao.geocaching4locus.base.usecase.GetWifiLocationUseCase
import com.arcao.geocaching4locus.base.usecase.RemoveLocusMapPointsUseCase
import com.arcao.geocaching4locus.base.usecase.RequireLocationPermissionRequestUseCase
import com.arcao.geocaching4locus.base.usecase.SendPointsSilentToLocusMapUseCase
import com.arcao.geocaching4locus.base.usecase.WritePointToPackPointsFileUseCase
import com.arcao.geocaching4locus.base.util.AnalyticsManager
import com.arcao.geocaching4locus.dashboard.DashboardViewModel
import com.arcao.geocaching4locus.data.account.AccountManager
import com.arcao.geocaching4locus.download_rectangle.DownloadRectangleViewModel
import com.arcao.geocaching4locus.error.handler.ExceptionHandler
import com.arcao.geocaching4locus.import_bookmarks.ImportBookmarkViewModel
import com.arcao.geocaching4locus.import_bookmarks.fragment.BookmarkListViewModel
import com.arcao.geocaching4locus.import_bookmarks.fragment.BookmarkViewModel
import com.arcao.geocaching4locus.importgc.ImportGeocacheCodeViewModel
import com.arcao.geocaching4locus.importgc.ImportUrlViewModel
import com.arcao.geocaching4locus.live_map.LiveMapViewModel
import com.arcao.geocaching4locus.live_map.util.LiveMapNotificationManager
import com.arcao.geocaching4locus.search_nearest.SearchNearestViewModel
import com.arcao.geocaching4locus.settings.manager.DefaultPreferenceManager
import com.arcao.geocaching4locus.settings.manager.FilterPreferenceManager
import com.arcao.geocaching4locus.update.UpdateMoreViewModel
import com.arcao.geocaching4locus.update.UpdateViewModel
import com.arcao.geocaching4locus.weblink.BookmarkGeocacheWebLinkViewModel
import com.arcao.geocaching4locus.weblink.WatchGeocacheWebLinkViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal val appModule = module {
    single { androidApplication() as App }
    singleOf(::PreferenceAccountManager) bind AccountManager::class
    single { CoroutinesDispatcherProvider() }
    single { AnalyticsManager(androidContext()) }

    singleOf(::GeocachingApiFilterProvider)
    singleOf(::FilterPreferenceManager)
    singleOf(::DefaultPreferenceManager)
    singleOf(::ExceptionHandler)
    singleOf(::LiveMapNotificationManager)

    // ---- Usecases ----
    factoryOf(::CreateAccountUseCase)
    factoryOf(::UpdateAccountUseCase)
    factoryOf(::RetrieveAuthorizationUrlUseCase)
    factoryOf(::GeocachingApiLoginUseCase)
    factoryOf(::GetListGeocachesUseCase)
    factoryOf(::GetGeocacheCodeFromGuidUseCase)
    factoryOf(::GetGeocachingLogsUseCase)
    factoryOf(::GetGeocachingTrackablesUseCase)
    factoryOf(::GetGpsLocationUseCase)
    factoryOf(::GetLastKnownLocationUseCase)
    factoryOf(::GetLiveMapPointsFromRectangleCoordinatesUseCase)
    factoryOf(::GetOldPointNewPointPairFromPointUseCase)
    factoryOf(::GetPointFromGeocacheCodeUseCase)
    factoryOf(::GetPointsFromCoordinatesUseCase)
    factoryOf(::GetPointsFromGeocacheCodesUseCase)
    factoryOf(::GetPointsFromPointIndexesUseCase)
    factoryOf(::GetPointsFromRectangleCoordinatesUseCase)
    factoryOf(::GetUserListsUseCase)
    factoryOf(::GetWifiLocationUseCase)
    factoryOf(::RemoveLocusMapPointsUseCase)
    factoryOf(::RequireLocationPermissionRequestUseCase)
    factoryOf(::SendPointsSilentToLocusMapUseCase)
    factoryOf(::WritePointToPackPointsFileUseCase)

    // ---- View models ----
    // login
    viewModelOf(::LoginViewModel)
    // dashboard
    viewModel { params ->
        DashboardViewModel(
            params.get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    // download live map rectangles
    viewModelOf(::DownloadRectangleViewModel)
    // import geocache codes
    viewModelOf(::ImportGeocacheCodeViewModel)
    // import url
    viewModelOf(::ImportUrlViewModel)
    // import bookmarks
    viewModelOf(::ImportBookmarkViewModel)
    viewModelOf(::BookmarkListViewModel)
    viewModel { params ->
        BookmarkViewModel(
            params.get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    // live map
    factoryOf(::LiveMapViewModel)
    // search nearest
    viewModel { params ->
        SearchNearestViewModel(
            params.get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    // update
    viewModelOf(::UpdateViewModel)
    viewModelOf(::UpdateMoreViewModel)
    // web link
    viewModelOf(::BookmarkGeocacheWebLinkViewModel)
    viewModelOf(::WatchGeocacheWebLinkViewModel)
}
