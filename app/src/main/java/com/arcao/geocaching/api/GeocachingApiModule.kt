package com.arcao.geocaching.api

import com.arcao.geocaching.api.configuration.GeocachingApiConfiguration
import com.arcao.geocaching.api.configuration.impl.DefaultProductionGeocachingApiConfiguration
import com.arcao.geocaching.api.configuration.impl.DefaultStagingGeocachingApiConfiguration
import com.arcao.geocaching.api.downloader.Downloader
import com.arcao.geocaching.api.downloader.OkHttpClientDownloader
import com.arcao.geocaching4locus.BuildConfig
import okhttp3.OkHttpClient
import org.koin.dsl.module.module
import org.koin.experimental.builder.create
import java.util.concurrent.TimeUnit

internal val geocachingApiModule = module {
    single<GeocachingApiConfiguration> {
        if (BuildConfig.GEOCACHING_API_STAGING)
            DefaultStagingGeocachingApiConfiguration()
        else
            DefaultProductionGeocachingApiConfiguration()
    }

    single {
        val apiConfiguration: GeocachingApiConfiguration = get()

        OkHttpClient.Builder()
                .connectTimeout(apiConfiguration.connectTimeout.toLong(), TimeUnit.MILLISECONDS)
                .readTimeout(apiConfiguration.readTimeout.toLong(), TimeUnit.MILLISECONDS)
                .enableTls12()
                .build()
    }

    single<Downloader> { create<OkHttpClientDownloader>() }

    factory<GeocachingApi> {
        LiveGeocachingApi.builder()
                .configuration(get())
                .downloader(get())
                .build()
    }
}
