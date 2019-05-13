package com.arcao.geocaching4locus.data.api.internal.okhttp

import com.arcao.geocaching4locus.data.api.internal.Factory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class OkHttpClientFactory(private val debug: Boolean) : Factory<OkHttpClient> {
    override fun create(): OkHttpClient =
            OkHttpClient.Builder().apply {
                readTimeout(60, TimeUnit.SECONDS)
                if (debug) {
                    addInterceptor(HttpLoggingInterceptor { message ->
                        println(message)
                    }.apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    })
                }
            }.build()
}

