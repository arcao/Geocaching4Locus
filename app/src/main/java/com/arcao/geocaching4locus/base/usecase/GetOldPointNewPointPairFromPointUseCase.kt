package com.arcao.geocaching4locus.base.usecase

import com.arcao.geocaching4locus.authentication.util.restrictions
import com.arcao.geocaching4locus.base.constants.AppConstants
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.util.DownloadingUtil
import com.arcao.geocaching4locus.data.account.AccountManager
import com.arcao.geocaching4locus.data.api.GeocachingApiRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.yield
import locus.api.mapper.DataMapper
import locus.api.objects.extra.Point

class GetOldPointNewPointPairFromPointUseCase(
    private val repository: GeocachingApiRepository,
    private val geocachingApiLogin: GeocachingApiLoginUseCase,
    private val accountManager: AccountManager,
    private val mapper: DataMapper,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) {
    @UseExperimental(ExperimentalCoroutinesApi::class)
    suspend operator fun invoke(
        source: Flow<Point>,
        liteData: Boolean = true,
        geocacheLogsCount: Int = 0
    ) = flow {
        geocachingApiLogin()

        var itemsPerRequest = AppConstants.ITEMS_PER_REQUEST

        while (true) {
            val points = source.takeList(itemsPerRequest)
            if (points.isEmpty()) {
                break
            }

            val requestedCacheIds = points.map { it.gcData.cacheID }.toTypedArray()

            val startTimeMillis = System.currentTimeMillis()

            val cachesToAdd = repository.geocaches(
                referenceCodes = *requestedCacheIds,
                logsCount = geocacheLogsCount,
                lite = liteData
            )

            accountManager.restrictions().updateLimits(repository.userLimits())

            yield()

            if (cachesToAdd.isNotEmpty()) {
                val receivedPoints = mapper.createLocusPoints(cachesToAdd)
                for (oldPoint in points) {
                    val newPoint = receivedPoints.find { it.gcData.cacheID == oldPoint.gcData.cacheID }
                    emit(Pair(oldPoint, newPoint))
                }
            }

            itemsPerRequest = DownloadingUtil.computeItemsPerRequest(itemsPerRequest, startTimeMillis)
        }
    }.flowOn(dispatcherProvider.io)
}

private suspend fun <E> Flow<E>.takeList(count: Int): List<E> {
    if (count <= 0) return emptyList()

    val list = mutableListOf<E>()
    var received = 0

    collect { item ->
        list.add(item)
        if (++received >= count) return@collect
    }

    return list
}
