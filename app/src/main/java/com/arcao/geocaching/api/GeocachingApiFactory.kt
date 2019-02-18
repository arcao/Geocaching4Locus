package com.arcao.geocaching.api

import org.koin.standalone.KoinComponent
import org.koin.standalone.get

@Deprecated("Use Koin")
object GeocachingApiFactory : KoinComponent {
    @JvmStatic
    fun create() = get<GeocachingApi>()
}
