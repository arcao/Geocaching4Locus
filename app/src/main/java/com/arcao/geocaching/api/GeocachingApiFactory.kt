package com.arcao.geocaching.api

import com.arcao.geocaching.api.downloader.Downloader
import okhttp3.OkHttpClient
import org.koin.standalone.KoinComponent
import org.koin.standalone.get
import org.koin.standalone.inject

@Deprecated("Use Koin")
object GeocachingApiFactory : KoinComponent {
    @JvmStatic
    val okHttpClient by inject<OkHttpClient>()

    @JvmStatic
    val downloader by inject<Downloader>()

    @JvmStatic
    fun create() = get<GeocachingApi>()
}
