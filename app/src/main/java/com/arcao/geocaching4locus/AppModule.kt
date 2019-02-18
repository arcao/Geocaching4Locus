package com.arcao.geocaching4locus

import com.arcao.geocaching4locus.authentication.LoginViewModel
import com.arcao.geocaching4locus.authentication.usecase.CreateAccountUseCase
import com.arcao.geocaching4locus.authentication.usecase.RetrieveAuthorizationUrlUseCase
import com.arcao.geocaching4locus.authentication.util.AccountManager
import com.arcao.geocaching4locus.authentication.util.PreferenceAccountManager
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.usecase.GeocachingApiFilterProvider
import com.arcao.geocaching4locus.base.usecase.GeocachingApiLoginUseCase
import com.arcao.geocaching4locus.base.usecase.GetGeocacheCodeFromGuidUseCase
import com.arcao.geocaching4locus.base.usecase.GetGeocachingLogsUseCase
import com.arcao.geocaching4locus.base.usecase.GetGeocachingTrackablesUseCase
import com.arcao.geocaching4locus.base.usecase.GetOldPointNewPointPairFromPointUseCase
import com.arcao.geocaching4locus.base.usecase.GetPointFromGeocacheCodeUseCase
import com.arcao.geocaching4locus.base.usecase.GetPointsFromGeocacheCodesUseCase
import com.arcao.geocaching4locus.base.usecase.GetPointsFromPointIndexesUseCase
import com.arcao.geocaching4locus.base.usecase.GetPointsFromRectangleCoordinatesUseCase
import com.arcao.geocaching4locus.base.usecase.RemoveLocusMapPointsUseCase
import com.arcao.geocaching4locus.base.usecase.SendPointsSilentToLocusMapUseCase
import com.arcao.geocaching4locus.base.usecase.WritePointToPackPointsFileUseCase
import com.arcao.geocaching4locus.dashboard.DashboardViewModel
import com.arcao.geocaching4locus.download_rectangle.DownloadRectangleViewModel
import com.arcao.geocaching4locus.error.handler.ExceptionHandler
import com.arcao.geocaching4locus.importgc.ImportGeocacheCodeViewModel
import com.arcao.geocaching4locus.importgc.ImportUrlViewModel
import com.arcao.geocaching4locus.live_map.LiveMapViewModel
import com.arcao.geocaching4locus.live_map.util.LiveMapNotificationManager
import com.arcao.geocaching4locus.settings.manager.DefaultPreferenceManager
import com.arcao.geocaching4locus.settings.manager.FilterPreferenceManager
import com.arcao.geocaching4locus.update.UpdateMoreViewModel
import com.arcao.geocaching4locus.update.UpdateViewModel
import com.arcao.geocaching4locus.weblink.BookmarkGeocacheWebLinkViewModel
import com.arcao.geocaching4locus.weblink.WatchGeocacheWebLinkViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module

@ExperimentalCoroutinesApi
internal val appModule = module {
    single { androidApplication() as App }
    single<AccountManager> { PreferenceAccountManager(get()) }
    single { CoroutinesDispatcherProvider() }

    single { GeocachingApiFilterProvider(get()) }
    single { FilterPreferenceManager(get(), get()) }
    single { DefaultPreferenceManager(get()) }
    single { ExceptionHandler(get(), get()) }
    factory { LiveMapNotificationManager(get(), get(), get(), get(), get()) }

    // ---- Usecases ----
    single { CreateAccountUseCase(get(), get(), get(), get(), get()) }
    single { RetrieveAuthorizationUrlUseCase(get(), get(), get()) }
    single { GeocachingApiLoginUseCase(get(), get(), get(), get()) }
    single { GetGeocacheCodeFromGuidUseCase(get(), get()) }
    single { GetGeocachingLogsUseCase(get(), get(), get(), get()) }
    single { GetGeocachingTrackablesUseCase(get(), get(), get(), get()) }
    single { GetOldPointNewPointPairFromPointUseCase(get(), get(), get(), get(), get()) }
    single { GetPointFromGeocacheCodeUseCase(get(), get(), get(), get(), get()) }
    single { GetPointsFromGeocacheCodesUseCase(get(), get(), get(), get(), get()) }
    single { GetPointsFromPointIndexesUseCase(get(), get()) }
    single { GetPointsFromRectangleCoordinatesUseCase(get(), get(), get(), get(), get(), get()) }
    single { RemoveLocusMapPointsUseCase(get(), get()) }
    single { SendPointsSilentToLocusMapUseCase(get(), get()) }
    single { WritePointToPackPointsFileUseCase(get()) }

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
    // live map
    viewModel { LiveMapViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    // update
    viewModel { UpdateViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { UpdateMoreViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    // web link
    viewModel { BookmarkGeocacheWebLinkViewModel(get(), get(), get(), get()) }
    viewModel { WatchGeocacheWebLinkViewModel(get(), get(), get(), get()) }
}