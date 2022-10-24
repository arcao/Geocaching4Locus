package com.arcao.geocaching4locus.base.usecase

import com.arcao.geocaching4locus.authentication.util.restrictions
import com.arcao.geocaching4locus.base.constants.AppConstants
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.util.DownloadingUtil
import com.arcao.geocaching4locus.data.account.AccountManager
import com.arcao.geocaching4locus.data.api.GeocachingApiRepository
import com.arcao.geocaching4locus.data.api.model.Geocache
import com.arcao.geocaching4locus.error.exception.CacheNotFoundException
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
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
    @Suppress("BlockingMethodInNonBlockingContext")
    suspend operator fun invoke(
        geocacheCodes: Array<String>,
        liteData: Boolean = true,
        geocacheLogsCount: Int = 0
    ) = flow {
        geocachingApiLogin()

        val notFoundGeocacheCodes = ArrayList<String>()

        val count = geocacheCodes.size
        var current = 0

        var itemsPerRequest = AppConstants.INITIAL_REQUEST_SIZE
        while (current < count) {
            val startTimeMillis = System.currentTimeMillis()

            val requestedCacheIds = getRequestedGeocacheIds(geocacheCodes, current, itemsPerRequest)

            val cachesToAdd = repository.geocaches(
                referenceCodes = requestedCacheIds,
                logsCount = geocacheLogsCount,
                lite = liteData
            )

            accountManager.restrictions().updateLimits(repository.userLimits())

            yield()

            addNotFoundCaches(notFoundGeocacheCodes, requestedCacheIds, cachesToAdd)

            if (cachesToAdd.isNotEmpty()) {
                val points = mapper.createLocusPoints(cachesToAdd)
                emit(points)
            }

            current += requestedCacheIds.size

            itemsPerRequest = DownloadingUtil.computeRequestSize(
                itemsPerRequest,
                AppConstants.GEOCACHES_MAX_REQUEST_SIZE,
                startTimeMillis
            )
        }

        Timber.v("found geocaches: %d", current)
        Timber.v("not found geocaches: %s", notFoundGeocacheCodes)

        // throw error if some geocache hasn't found
        if (notFoundGeocacheCodes.isNotEmpty()) {
            throw CacheNotFoundException(*notFoundGeocacheCodes.toTypedArray())
        }
    }.flowOn(dispatcherProvider.io)

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

    private fun getRequestedGeocacheIds(
        cacheIds: Array<String>,
        current: Int,
        cachesPerRequest: Int
    ): Array<String> {
        val count = min(cacheIds.size - current, cachesPerRequest)
        return cacheIds.copyOfRange(current, current + count)
    }
}
