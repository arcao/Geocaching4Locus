package com.arcao.geocaching4locus.data

import com.arcao.geocaching4locus.data.account.AccountManager
import com.arcao.geocaching4locus.data.account.PreferenceAccountManager
import com.arcao.geocaching4locus.data.account.oauth.GeocachingOAuthServiceFactory
import com.arcao.geocaching4locus.data.api.GeocachingApiRepository
import com.arcao.geocaching4locus.data.api.endpoint.GeocachingApiEndpointFactory
import com.arcao.geocaching4locus.data.api.internal.moshi.MoshiFactory
import com.arcao.geocaching4locus.data.api.internal.okhttp.OkHttpClientFactory
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

val geocachingApiModule = module {
    single { OkHttpClientFactory(true).create() }
    single { MoshiFactory.create() }
    single { GeocachingOAuthServiceFactory(get()).create() }
    single { PreferenceAccountManager(androidContext(), get()) } bind AccountManager::class
    single { GeocachingApiEndpointFactory(get(), get(), get()).create() }
    single { GeocachingApiRepository(get()) }
}
