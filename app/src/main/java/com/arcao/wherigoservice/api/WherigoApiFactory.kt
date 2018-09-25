package com.arcao.wherigoservice.api

import com.arcao.geocaching.api.GeocachingApiFactory

object WherigoApiFactory {
    @JvmStatic
    fun create(): WherigoService {
        return WherigoServiceImpl(GeocachingApiFactory.downloader)
    }
}
