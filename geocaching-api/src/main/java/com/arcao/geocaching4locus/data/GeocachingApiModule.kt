package com.arcao.geocaching4locus.data

import com.arcao.geocaching4locus.data.account.oauth.GeocachingOAuthServiceFactory
import com.arcao.geocaching4locus.data.api.GeocachingApiRepository
import com.arcao.geocaching4locus.data.api.WherigoApiRepository
import com.arcao.geocaching4locus.data.api.endpoint.GeocachingApiEndpointFactory
import com.arcao.geocaching4locus.data.api.endpoint.WherigoApiEndpointFactory
import com.arcao.geocaching4locus.data.api.internal.moshi.MoshiFactory
import com.arcao.geocaching4locus.data.api.internal.okhttp.OkHttpClientFactory
import org.koin.dsl.module

val geocachingApiModule = module {
    single { OkHttpClientFactory(getOrNull(),true).create() }
    single { MoshiFactory.create() }
    single { GeocachingOAuthServiceFactory(get()).create() }
    single { GeocachingApiEndpointFactory(get(), get(), get()).create() }
    single { WherigoApiEndpointFactory(get(), get()).create() }
    single { GeocachingApiRepository(inject()) }
    single { WherigoApiRepository(inject()) }
}
