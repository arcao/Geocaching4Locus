package com.arcao.geocaching4locus.data.api.internal.okhttp

import com.arcao.geocaching4locus.data.api.internal.Factory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber
import java.util.concurrent.TimeUnit

class OkHttpClientFactory(private val debug: Boolean) : Factory<OkHttpClient> {
    override fun create(): OkHttpClient {
        return OkHttpClient.Builder().apply {
            readTimeout(TIMEOUT, TimeUnit.SECONDS)
            connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            enableTls12()

            addInterceptor(HttpLoggingInterceptor { message ->
                Timber.d(message)
            }.apply {
                level = if (debug) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.BASIC
                }
            })
        }.build()
    }

    companion object {
        private const val TIMEOUT = 60L
    }
}

