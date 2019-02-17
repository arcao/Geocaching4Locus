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
import org.koin.androidx.viewmodel.experimental.builder.viewModel
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module
import org.koin.experimental.builder.create
import org.koin.experimental.builder.factory
import org.koin.experimental.builder.single

@ExperimentalCoroutinesApi
internal val appModule = module {
    single { androidApplication() as App }
    single<AccountManager> { create<PreferenceAccountManager>() }
    single<CoroutinesDispatcherProvider>()

    single<GeocachingApiFilterProvider>()
    single<FilterPreferenceManager>()
    single<DefaultPreferenceManager>()
    single<ExceptionHandler>()
    factory<LiveMapNotificationManager>()

    // ---- Usecases ----
    single<CreateAccountUseCase>()
    single<RetrieveAuthorizationUrlUseCase>()
    single<GeocachingApiLoginUseCase>()
    single<GetGeocacheCodeFromGuidUseCase>()
    single<GetGeocachingLogsUseCase>()
    single<GetGeocachingTrackablesUseCase>()
    single<GetOldPointNewPointPairFromPointUseCase>()
    single<GetPointFromGeocacheCodeUseCase>()
    single<GetPointsFromGeocacheCodesUseCase>()
    single<GetPointsFromPointIndexesUseCase>()
    single<GetPointsFromRectangleCoordinatesUseCase>()
    single<RemoveLocusMapPointsUseCase>()
    single<SendPointsSilentToLocusMapUseCase>()
    single<WritePointToPackPointsFileUseCase>()

    // ---- View models ----
    // login
    viewModel<LoginViewModel>()
    // dashboard
    viewModel {
        (calledFromLocusMap: Boolean) -> DashboardViewModel(calledFromLocusMap, get(), get(), get(), get())
    }
    // download live map rectangles
    viewModel<DownloadRectangleViewModel>()
    // import geocache codes
    viewModel<ImportGeocacheCodeViewModel>()
    // import url
    viewModel<ImportUrlViewModel>()
    // live map
    viewModel<LiveMapViewModel>()
    // update
    viewModel<UpdateViewModel>()
    viewModel<UpdateMoreViewModel>()
    // web link
    viewModel<BookmarkGeocacheWebLinkViewModel>()
    viewModel<WatchGeocacheWebLinkViewModel>()
}