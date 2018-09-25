package com.arcao.geocaching.api

import android.os.Build
import com.arcao.geocaching.api.configuration.GeocachingApiConfiguration
import com.arcao.geocaching.api.configuration.impl.DefaultProductionGeocachingApiConfiguration
import com.arcao.geocaching.api.configuration.impl.DefaultStagingGeocachingApiConfiguration
import com.arcao.geocaching.api.downloader.Downloader
import com.arcao.geocaching.api.downloader.OkHttpClientDownloader
import com.arcao.geocaching4locus.BuildConfig
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext

object GeocachingApiFactory {
    private val apiConfiguration: GeocachingApiConfiguration by lazy {
        if (BuildConfig.GEOCACHING_API_STAGING)
            DefaultStagingGeocachingApiConfiguration()
        else
            DefaultProductionGeocachingApiConfiguration()
    }

    @JvmStatic
    val okHttpClient: OkHttpClient by lazy {
        val apiConfiguration = apiConfiguration
        enableTls12OnPreLollipop(OkHttpClient.Builder()
                .connectTimeout(apiConfiguration.connectTimeout.toLong(), TimeUnit.MILLISECONDS)
                .readTimeout(apiConfiguration.readTimeout.toLong(), TimeUnit.MILLISECONDS)
        ).build()
    }

    @JvmStatic
    val downloader: Downloader by lazy {
        OkHttpClientDownloader(okHttpClient)
    }

    @JvmStatic
    fun create(): GeocachingApi {
        return LiveGeocachingApi.builder()
                .configuration(apiConfiguration)
                .downloader(downloader)
                .build()
    }

    private fun enableTls12OnPreLollipop(client: OkHttpClient.Builder): OkHttpClient.Builder {
        if (Build.VERSION.SDK_INT in 16..21) {
            try {
                val sc = SSLContext.getInstance("TLSv1.2")
                sc.init(null, null, null)

                @Suppress("DEPRECATION")
                client.sslSocketFactory(Tls12SocketFactory(sc.socketFactory))

                client.connectionSpecs(listOf(
                        ConnectionSpec.MODERN_TLS,
                        ConnectionSpec.COMPATIBLE_TLS,
                        ConnectionSpec.CLEARTEXT
                ))
            } catch (e: Exception) {
                Timber.e(e, "Error while setting TLS 1.2")
            }
        }

        return client
    }
}
