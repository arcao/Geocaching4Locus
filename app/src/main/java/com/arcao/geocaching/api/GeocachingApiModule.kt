package com.arcao.geocaching.api

import android.os.Build
import com.arcao.geocaching.api.configuration.GeocachingApiConfiguration
import com.arcao.geocaching.api.configuration.impl.DefaultProductionGeocachingApiConfiguration
import com.arcao.geocaching.api.configuration.impl.DefaultStagingGeocachingApiConfiguration
import com.arcao.geocaching.api.downloader.Downloader
import com.arcao.geocaching.api.downloader.OkHttpClientDownloader
import com.arcao.geocaching.api.oauth.GeocachingOAuthProvider
import com.arcao.geocaching4locus.BuildConfig
import com.arcao.geocaching4locus.authentication.util.DeviceInfoFactory
import com.arcao.geocaching4locus.base.constants.AppConstants
import com.github.scribejava.core.builder.ServiceBuilder
import com.github.scribejava.core.oauth.OAuth10aService
import com.github.scribejava.httpclient.okhttp.OkHttpHttpClient
import okhttp3.OkHttpClient
import org.koin.dsl.module
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

    single<Downloader> { OkHttpClientDownloader(get()) }

    factory<GeocachingApi> {
        LiveGeocachingApi.builder()
            .configuration(get())
            .downloader(get())
            .build()
    }

    single { DeviceInfoFactory(get()) }

    // OAuth service
    factory<OAuth10aService> {
        val serviceBuilder = ServiceBuilder(BuildConfig.GEOCACHING_API_KEY)
            .apiSecret(BuildConfig.GEOCACHING_API_SECRET)
            .callback(AppConstants.OAUTH_CALLBACK_URL)
            .httpClient(OkHttpHttpClient(get<OkHttpClient>()))

        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // for debugging is required API 19
            serviceBuilder.debug()
        }

        if (BuildConfig.GEOCACHING_API_STAGING) {
            serviceBuilder.build(GeocachingOAuthProvider.Staging())
        } else {
            serviceBuilder.build(GeocachingOAuthProvider())
        }
    }
}
