package com.arcao.geocaching4locus.data.api.endpoint

import com.arcao.geocaching4locus.data.account.AccountManager
import com.arcao.geocaching4locus.data.account.oauth.GeocachingOAuthServiceFactory
import com.arcao.geocaching4locus.data.api.internal.Factory
import com.arcao.geocaching4locus.data.api.internal.okhttp.interceptor.GeocachingAuthenticationInterceptor
import com.arcao.geocaching4locus.data.api.internal.retrofit.adapter.GeocachingApiCoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import org.jetbrains.annotations.TestOnly
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class GeocachingApiEndpointFactory @TestOnly internal constructor(
        private val endpointUrl: String,
        private val accountManager: AccountManager,
        private val okHttpClient: OkHttpClient,
        private val moshi: Moshi
) : Factory<GeocachingApiEndpoint> {

    companion object {
        const val PRODUCTION_ENDPOINT_URL = "https://api.groundspeak.com"
        const val STAGING_ENDPOINT_URL = "https://staging.api.groundspeak.com"
    }

    constructor(accountManager: AccountManager, okHttpClient: OkHttpClient, moshi: Moshi) : this(
            if (GeocachingOAuthServiceFactory.API_STAGING) STAGING_ENDPOINT_URL else PRODUCTION_ENDPOINT_URL,
            accountManager,
            okHttpClient,
            moshi
    )

    override fun create(): GeocachingApiEndpoint {
        val authenticationInterceptor = GeocachingAuthenticationInterceptor(accountManager)

        val okHttp = okHttpClient.newBuilder().apply {
            addInterceptor(authenticationInterceptor)
        }.build()

        return Retrofit.Builder().apply {
            client(okHttp)
            baseUrl(endpointUrl)
            addCallAdapterFactory(GeocachingApiCoroutineCallAdapterFactory.create())
            addConverterFactory(MoshiConverterFactory.create(moshi))
        }.build().create(GeocachingApiEndpoint::class.java).also {
            authenticationInterceptor.endpoint = it
        }
    }
}