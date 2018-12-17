package com.arcao.geocaching4locus

import com.arcao.geocaching4locus.authentication.util.AccountManager
import com.arcao.geocaching4locus.authentication.util.PreferenceAccountManager
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.usecase.GeocachingApiFilterProvider
import com.arcao.geocaching4locus.base.usecase.GeocachingApiLoginUseCase
import com.arcao.geocaching4locus.base.usecase.GetGeocacheCodeFromGuidUseCase
import com.arcao.geocaching4locus.base.usecase.GetPointFromGeocacheCodeUseCase
import com.arcao.geocaching4locus.base.usecase.GetPointsFromGeocacheCodesUseCase
import com.arcao.geocaching4locus.base.usecase.WritePointToPackPointsFileUseCase
import com.arcao.geocaching4locus.dashboard.DashboardViewModel
import com.arcao.geocaching4locus.download_rectangle.DownloadRectangleViewModel
import com.arcao.geocaching4locus.base.usecase.GetPointsFromRectangleCoordinatesUseCase
import com.arcao.geocaching4locus.error.handler.ExceptionHandler
import com.arcao.geocaching4locus.importgc.ImportGeocacheCodeViewModel
import com.arcao.geocaching4locus.importgc.ImportUrlViewModel
import com.arcao.geocaching4locus.live_map.util.LiveMapNotificationManager
import com.arcao.geocaching4locus.settings.manager.DefaultPreferenceManager
import com.arcao.geocaching4locus.settings.manager.FilterPreferenceManager
import com.arcao.geocaching4locus.weblink.BookmarkGeocacheWebLinkViewModel
import com.arcao.geocaching4locus.weblink.WatchGeocacheWebLinkViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.experimental.builder.viewModel
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module
import org.koin.experimental.builder.create
import org.koin.experimental.builder.factory
import org.koin.experimental.builder.single

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
    single<GeocachingApiLoginUseCase>()
    single<GetGeocacheCodeFromGuidUseCase>()
    single<GetPointFromGeocacheCodeUseCase>()
    single<GetPointsFromGeocacheCodesUseCase>()
    single<GetPointsFromRectangleCoordinatesUseCase>()
    single<WritePointToPackPointsFileUseCase>()

    // ---- View models ----
    // dashboard
    viewModel {
        (calledFromLocusMap: Boolean) -> DashboardViewModel(calledFromLocusMap, get(), get(), get(), get())
    }
    // web link
    viewModel<BookmarkGeocacheWebLinkViewModel>()
    viewModel<WatchGeocacheWebLinkViewModel>()
    // import geocache codes
    viewModel<ImportGeocacheCodeViewModel>()
    // import url
    viewModel<ImportUrlViewModel>()
    // download live map rectangles
    viewModel<DownloadRectangleViewModel>()
}