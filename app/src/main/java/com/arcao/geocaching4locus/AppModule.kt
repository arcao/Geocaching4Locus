package com.arcao.geocaching4locus

import com.arcao.geocaching4locus.authentication.util.AccountManager
import com.arcao.geocaching4locus.authentication.util.PreferenceAccountManager
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.usecase.GeocachingApiLoginUseCase
import com.arcao.geocaching4locus.dashboard.DashboardViewModel
import com.arcao.geocaching4locus.error.handler.ExceptionHandler
import com.arcao.geocaching4locus.live_map.util.LiveMapNotificationManager
import com.arcao.geocaching4locus.weblink.WatchGeocacheWebLinkViewModel
import com.arcao.geocaching4locus.weblink.usecase.GetPointFromGeocacheCodeUseCase
import locus.api.mapper.DataMapper
import org.koin.android.ext.koin.androidApplication
import org.koin.android.viewmodel.experimental.builder.viewModel
import org.koin.android.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module
import org.koin.experimental.builder.create
import org.koin.experimental.builder.factory
import org.koin.experimental.builder.single

internal val appModule = module {
    single { androidApplication() as App }
    single<AccountManager> { create<PreferenceAccountManager>() }
    single<DataMapper>()
    single<CoroutinesDispatcherProvider>()

    single<ExceptionHandler>()
    factory<LiveMapNotificationManager>()
    single<GeocachingApiLoginUseCase>()

    // dashboard
    viewModel {
        (calledFromLocusMap : Boolean) -> DashboardViewModel(calledFromLocusMap, get(), get(), get(), get())
    }

    // weblink
    viewModel<WatchGeocacheWebLinkViewModel>()
    single<GetPointFromGeocacheCodeUseCase>()
}