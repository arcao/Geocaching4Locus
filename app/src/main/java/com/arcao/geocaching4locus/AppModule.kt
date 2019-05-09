package com.arcao.geocaching4locus

import android.content.Intent
import com.arcao.geocaching4locus.authentication.LoginViewModel
import com.arcao.geocaching4locus.authentication.usecase.CreateAccountUseCase
import com.arcao.geocaching4locus.authentication.usecase.RetrieveAuthorizationUrlUseCase
import com.arcao.geocaching4locus.authentication.util.AccountManager
import com.arcao.geocaching4locus.authentication.util.PreferenceAccountManager
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.usecase.GeocachingApiFilterProvider
import com.arcao.geocaching4locus.base.usecase.GeocachingApiLoginUseCase
import com.arcao.geocaching4locus.base.usecase.GetBookmarkUseCase
import com.arcao.geocaching4locus.base.usecase.GetGeocacheCodeFromGuidUseCase
import com.arcao.geocaching4locus.base.usecase.GetGeocachingLogsUseCase
import com.arcao.geocaching4locus.base.usecase.GetGeocachingTrackablesUseCase
import com.arcao.geocaching4locus.base.usecase.GetGpsLocationUseCase
import com.arcao.geocaching4locus.base.usecase.GetLastKnownLocationUseCase
import com.arcao.geocaching4locus.base.usecase.GetOldPointNewPointPairFromPointUseCase
import com.arcao.geocaching4locus.base.usecase.GetPointFromGeocacheCodeUseCase
import com.arcao.geocaching4locus.base.usecase.GetPointsFromCoordinatesUseCase
import com.arcao.geocaching4locus.base.usecase.GetPointsFromGeocacheCodesUseCase
import com.arcao.geocaching4locus.base.usecase.GetPointsFromPointIndexesUseCase
import com.arcao.geocaching4locus.base.usecase.GetPointsFromRectangleCoordinatesUseCase
import com.arcao.geocaching4locus.base.usecase.GetUserBookmarkListsUseCase
import com.arcao.geocaching4locus.base.usecase.GetWifiLocationUseCase
import com.arcao.geocaching4locus.base.usecase.RemoveLocusMapPointsUseCase
import com.arcao.geocaching4locus.base.usecase.RequireLocationPermissionRequestUseCase
import com.arcao.geocaching4locus.base.usecase.SendPointsSilentToLocusMapUseCase
import com.arcao.geocaching4locus.base.usecase.WritePointToPackPointsFileUseCase
import com.arcao.geocaching4locus.base.usecase.entity.BookmarkListEntity
import com.arcao.geocaching4locus.dashboard.DashboardViewModel
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
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

internal val appModule = module {
    single { androidApplication() as App }
    single<AccountManager> { PreferenceAccountManager(get()) }
    single { CoroutinesDispatcherProvider() }

    single { GeocachingApiFilterProvider(get()) }
    single { FilterPreferenceManager(get(), get()) }
    single { DefaultPreferenceManager(get()) }
    single { ExceptionHandler(get(), get()) }
    single { LiveMapNotificationManager(get(), get(), get(), get(), get()) }

    // ---- Usecases ----
    factory { CreateAccountUseCase(get(), get(), get(), get(), get()) }
    factory { RetrieveAuthorizationUrlUseCase(get(), get(), get()) }
    factory { GeocachingApiLoginUseCase(get(), get(), get(), get()) }
    factory { GetBookmarkUseCase(get(), get(), get()) }
    factory { GetGeocacheCodeFromGuidUseCase(get(), get()) }
    factory { GetGeocachingLogsUseCase(get(), get(), get(), get()) }
    factory { GetGeocachingTrackablesUseCase(get(), get(), get(), get()) }
    factory { GetGpsLocationUseCase(get(), get()) }
    factory { GetLastKnownLocationUseCase(get(), get()) }
    factory { GetOldPointNewPointPairFromPointUseCase(get(), get(), get(), get(), get()) }
    factory { GetPointFromGeocacheCodeUseCase(get(), get(), get(), get(), get()) }
    factory { GetPointsFromCoordinatesUseCase(get(), get(), get(), get(), get(), get()) }
    factory { GetPointsFromGeocacheCodesUseCase(get(), get(), get(), get(), get()) }
    factory { GetPointsFromPointIndexesUseCase(get(), get()) }
    factory { GetPointsFromRectangleCoordinatesUseCase(get(), get(), get(), get(), get(), get()) }
    factory { GetUserBookmarkListsUseCase(get(), get(), get()) }
    factory { GetWifiLocationUseCase(get(), get()) }
    factory { RemoveLocusMapPointsUseCase(get(), get()) }
    factory { RequireLocationPermissionRequestUseCase(get()) }
    factory { SendPointsSilentToLocusMapUseCase(get(), get()) }
    factory { WritePointToPackPointsFileUseCase(get()) }

    // ---- View models ----
    // login
    viewModel { LoginViewModel(get(), get(), get(), get(), get(), get()) }
    // dashboard
    viewModel { (calledFromLocusMap: Boolean) -> DashboardViewModel(calledFromLocusMap, get(), get(), get(), get()) }
    // download live map rectangles
    viewModel { DownloadRectangleViewModel(get(), get(), get(), get(), get(), get(), get()) }
    // import geocache codes
    viewModel { ImportGeocacheCodeViewModel(get(), get(), get(), get(), get(), get(), get()) }
    // import url
    viewModel { ImportUrlViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    // import bookmarks
    viewModel { ImportBookmarkViewModel(get(), get(), get()) }
    viewModel { BookmarkListViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { (bl: BookmarkListEntity) -> BookmarkViewModel(bl, get(), get(), get(), get(), get(), get(), get()) }
    // live map
    viewModel { LiveMapViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    // search nearest
    viewModel { (intent: Intent) ->
        SearchNearestViewModel(
            intent,
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
    viewModel { UpdateViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { UpdateMoreViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    // web link
    viewModel { BookmarkGeocacheWebLinkViewModel(get(), get(), get(), get()) }
    viewModel { WatchGeocacheWebLinkViewModel(get(), get(), get(), get()) }
}