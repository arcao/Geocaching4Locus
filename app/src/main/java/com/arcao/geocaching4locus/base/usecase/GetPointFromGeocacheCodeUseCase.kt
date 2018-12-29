package com.arcao.geocaching4locus.base.usecase

import com.arcao.geocaching.api.GeocachingApi
import com.arcao.geocaching4locus.authentication.util.AccountManager
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.error.exception.CacheNotFoundException
import kotlinx.coroutines.withContext
import locus.api.mapper.DataMapper

class GetPointFromGeocacheCodeUseCase(
    private val geocachingApi: GeocachingApi,
    private val geocachingApiLoginUseCase: GeocachingApiLoginUseCase,
    private val accountManager: AccountManager,
    private val mapper: DataMapper,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) {
    suspend operator fun invoke(
        geocacheCode: String,
        liteData: Boolean = true,
        geocacheLogsCount: Int = 0,
        trackableLogsCount: Int = 0
    ) = withContext(dispatcherProvider.io) {
        geocachingApiLoginUseCase(geocachingApi)

        val resultQuality = if (liteData) {
            GeocachingApi.ResultQuality.LITE
        } else {
            GeocachingApi.ResultQuality.FULL
        }

        val geocache = geocachingApi.getGeocache(resultQuality, geocacheCode, geocacheLogsCount, trackableLogsCount)
            ?: throw CacheNotFoundException(geocacheCode)

        accountManager.restrictions.updateLimits(geocachingApi.lastGeocacheLimits)

        mapper.createLocusPoint(geocache)
    }
}
