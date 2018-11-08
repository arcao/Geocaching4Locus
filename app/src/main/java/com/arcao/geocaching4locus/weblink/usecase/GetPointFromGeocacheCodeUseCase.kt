package com.arcao.geocaching4locus.weblink.usecase

import com.arcao.geocaching.api.GeocachingApi
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.usecase.GeocachingApiLoginUseCase
import com.arcao.geocaching4locus.error.exception.CacheNotFoundException
import kotlinx.coroutines.withContext
import locus.api.mapper.DataMapper
import locus.api.objects.extra.Point

class GetPointFromGeocacheCodeUseCase(
        private val geocachingApi: GeocachingApi,
        private val geocachingApiLoginUseCase: GeocachingApiLoginUseCase,
        private val mapper : DataMapper,
        private val dispatcherProvider: CoroutinesDispatcherProvider
) {
    suspend operator fun invoke(geocacheCode : String, liteData : Boolean = true) : Point =
            withContext(dispatcherProvider.io) {
                geocachingApiLoginUseCase(geocachingApi)

                val resultQuality = if (liteData)
                    GeocachingApi.ResultQuality.LITE
                else
                    GeocachingApi.ResultQuality.FULL

                val geocache = geocachingApi.getGeocache(resultQuality, geocacheCode, 0, 0)
                        ?: throw CacheNotFoundException(geocacheCode);

                mapper.createLocusPoint(geocache)
            }
}

