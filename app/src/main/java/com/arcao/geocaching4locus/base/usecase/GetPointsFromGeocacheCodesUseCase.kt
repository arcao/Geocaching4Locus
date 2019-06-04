package com.arcao.geocaching4locus.base.usecase

import com.arcao.geocaching4locus.authentication.util.restrictions
import com.arcao.geocaching4locus.base.constants.AppConstants
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.util.DownloadingUtil
import com.arcao.geocaching4locus.data.account.AccountManager
import com.arcao.geocaching4locus.data.api.GeocachingApiRepository
import com.arcao.geocaching4locus.data.api.model.Geocache
import com.arcao.geocaching4locus.error.exception.CacheNotFoundException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.yield
import locus.api.mapper.DataMapper
import timber.log.Timber
import kotlin.math.min

class GetPointsFromGeocacheCodesUseCase(
    private val repository: GeocachingApiRepository,
    private val geocachingApiLogin: GeocachingApiLoginUseCase,
    private val accountManager: AccountManager,
    private val mapper: DataMapper,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) {
    @UseExperimental(ExperimentalCoroutinesApi::class)
    suspend operator fun invoke(
        scope: CoroutineScope,
        geocacheCodes: Array<String>,
        liteData: Boolean = true,
        geocacheLogsCount: Int = 0
    ) = scope.produce(dispatcherProvider.io) {
        geocachingApiLogin()

        val notFoundGeocacheCodes = ArrayList<String>()

        val count = geocacheCodes.size
        var current = 0

        var itemsPerRequest = AppConstants.ITEMS_PER_REQUEST
        while (current < count) {
            val startTimeMillis = System.currentTimeMillis()

            val requestedCacheIds = getRequestedGeocacheIds(geocacheCodes, current, itemsPerRequest)

            val cachesToAdd = repository.geocaches(
                referenceCodes = *requestedCacheIds,
                logsCount = geocacheLogsCount,
                lite = liteData
            )

            accountManager.restrictions().updateLimits(repository.userLimits())

            yield()

            addNotFoundCaches(notFoundGeocacheCodes, requestedCacheIds, cachesToAdd)

            if (cachesToAdd.isNotEmpty()) {
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

    private fun addNotFoundCaches(
        notFoundCacheIds: MutableList<String>,
        requestedCacheIds: Array<String>,
        cachesToAdd: List<Geocache>
    ) {
        if (requestedCacheIds.size == cachesToAdd.size) {
            return
        }

        val foundCacheIds = arrayOfNulls<String>(cachesToAdd.size)
        for (i in cachesToAdd.indices) {
            foundCacheIds[i] = cachesToAdd[i].referenceCode
        }

        for (cacheId in requestedCacheIds) {
            if (!foundCacheIds.contains(cacheId)) {
                notFoundCacheIds.add(cacheId)
            }
        }
    }

    private fun getRequestedGeocacheIds(cacheIds: Array<String>, current: Int, cachesPerRequest: Int): Array<String> {
        val count = min(cacheIds.size - current, cachesPerRequest)
        return cacheIds.copyOfRange(current, current + count)
    }
}
