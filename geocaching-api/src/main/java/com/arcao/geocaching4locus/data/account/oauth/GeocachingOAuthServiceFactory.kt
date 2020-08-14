package com.arcao.geocaching4locus.data.account.oauth

import com.arcao.geocaching4locus.data.account.oauth.client.OkHttp3HttpClient
import com.arcao.geocaching4locus.data.api.internal.Factory
import com.arcao.geocaching4locus.geocaching_api.BuildConfig
import com.github.scribejava.core.builder.ServiceBuilder
import com.github.scribejava.core.oauth.OAuth20Service
import okhttp3.OkHttpClient

class GeocachingOAuthServiceFactory(
    private val okHttpClient: OkHttpClient
) : Factory<OAuth20Service> {
    companion object {
        const val API_KEY = BuildConfig.GEOCACHING_API_KEY
        const val API_SECRET = BuildConfig.GEOCACHING_API_SECRET
        const val API_STAGING = BuildConfig.GEOCACHING_API_STAGING

        const val CALLBACK_URL = "https://geocaching4locus.eu/oauth"
    }

    override fun create(): OAuth20Service {
        return when {
            API_STAGING ->
                ServiceBuilder(API_KEY)
                    .apiSecret(API_SECRET)
                    .callback(CALLBACK_URL)
                    .defaultScope("*")
                    .responseType("code")
                    .httpClient(OkHttp3HttpClient(okHttpClient))
                    .build(GeocachingOAuthApi.Staging())
            else ->
                ServiceBuilder(API_KEY)
                    .apiSecret(API_SECRET)
                    .callback(CALLBACK_URL)
                    .defaultScope("*")
                    .responseType("code")
                    .httpClient(OkHttp3HttpClient(okHttpClient))
                    .build(GeocachingOAuthApi())
        }
    }
}
