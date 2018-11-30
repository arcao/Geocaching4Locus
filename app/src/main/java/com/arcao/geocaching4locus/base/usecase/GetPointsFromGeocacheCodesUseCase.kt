package com.arcao.geocaching4locus.base.usecase

import com.arcao.geocaching.api.GeocachingApi
import com.arcao.geocaching.api.data.Geocache
import com.arcao.geocaching.api.data.SearchForGeocachesRequest
import com.arcao.geocaching.api.filter.CacheCodeFilter
import com.arcao.geocaching4locus.authentication.util.AccountManager
import com.arcao.geocaching4locus.base.constants.AppConstants
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.util.DownloadingUtil
import com.arcao.geocaching4locus.error.exception.CacheNotFoundException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.isActive
import locus.api.mapper.DataMapper
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

class GetPointsFromGeocacheCodesUseCase(
    private val geocachingApi: GeocachingApi,
    private val geocachingApiLoginUseCase: GeocachingApiLoginUseCase,
    private val accountManager: AccountManager,
    private val mapper: DataMapper,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = dispatcherProvider.io

    @ExperimentalCoroutinesApi
    suspend operator fun invoke(geocacheCodes: Array<String>, liteData: Boolean = true, geocacheLogsCount: Int = 0, trackableLogsCount: Int = 0) = produce {
        geocachingApiLoginUseCase(geocachingApi)

        val resultQuality = if (liteData) {
            GeocachingApi.ResultQuality.LITE
        } else {
            GeocachingApi.ResultQuality.FULL
        }

        val notFoundGeocacheCodes = ArrayList<String>()

        val count = geocacheCodes.size
        var current = 0

        var itemsPerRequest = AppConstants.ITEMS_PER_REQUEST
        while (current < count) {
            val startTimeMillis = System.currentTimeMillis()

            val requestedCacheIds = getRequestedGeocacheIds(geocacheCodes, current, itemsPerRequest)

            val cachesToAdd = geocachingApi.searchForGeocaches(SearchForGeocachesRequest.builder()
                    .resultQuality(resultQuality)
                    .maxPerPage(itemsPerRequest)
                    .geocacheLogCount(geocacheLogsCount)
                    .trackableLogCount(trackableLogsCount)
                    .addFilter(CacheCodeFilter(*requestedCacheIds))
                    .build()
            )

            accountManager.restrictions.updateLimits(geocachingApi.lastGeocacheLimits)

            if (!isActive)
                return@produce

            addNotFoundCaches(notFoundGeocacheCodes, requestedCacheIds, cachesToAdd)

            if (!cachesToAdd.isEmpty()) {
                val points = mapper.createLocusPoints(cachesToAdd)
                send(points)
            }

            current += requestedCacheIds.size

            itemsPerRequest = DownloadingUtil.computeItemsPerRequest(itemsPerRequest, startTimeMillis)
        }

        Timber.v("found geocaches: %d", current)
        Timber.v("not found geocaches: %s", notFoundGeocacheCodes)

        // throw error if some geocache hasn't found
        if (notFoundGeocacheCodes.isNotEmpty()) {
            throw CacheNotFoundException(*notFoundGeocacheCodes.toTypedArray())
        }
    }

    private fun addNotFoundCaches(notFoundCacheIds: MutableList<String>, requestedCacheIds: Array<String>, cachesToAdd: List<Geocache>) {
        if (requestedCacheIds.size == cachesToAdd.size) {
            return
        }

        val foundCacheIds = arrayOfNulls<String>(cachesToAdd.size)
        for (i in cachesToAdd.indices) {
            foundCacheIds[i] = cachesToAdd[i].code()
        }

        for (cacheId in requestedCacheIds) {
            if (!foundCacheIds.contains(cacheId)) {
                notFoundCacheIds.add(cacheId)
            }
        }
    }

    private fun getRequestedGeocacheIds(cacheIds: Array<String>, current: Int, cachesPerRequest: Int): Array<String> {
        val count = Math.min(cacheIds.size - current, cachesPerRequest)
        return cacheIds.copyOfRange(current, current + count)
    }
}
