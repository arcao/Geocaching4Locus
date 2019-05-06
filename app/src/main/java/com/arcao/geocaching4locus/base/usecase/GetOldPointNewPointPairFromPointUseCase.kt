package com.arcao.geocaching4locus.base.usecase

import com.arcao.geocaching.api.GeocachingApi
import com.arcao.geocaching.api.data.SearchForGeocachesRequest
import com.arcao.geocaching.api.filter.CacheCodeFilter
import com.arcao.geocaching4locus.authentication.util.AccountManager
import com.arcao.geocaching4locus.base.constants.AppConstants
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.util.DownloadingUtil
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import locus.api.mapper.DataMapper
import locus.api.objects.extra.Point

class GetOldPointNewPointPairFromPointUseCase(
    private val geocachingApi: GeocachingApi,
    private val geocachingApiLogin: GeocachingApiLoginUseCase,
    private val accountManager: AccountManager,
    private val mapper: DataMapper,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) {
    @UseExperimental(ExperimentalCoroutinesApi::class)
    suspend operator fun invoke(
        channel: ReceiveChannel<Point>,
        liteData: Boolean = true,
        geocacheLogsCount: Int = 0,
        trackableLogsCount: Int = 0
    ) = coroutineScope {
        produce(dispatcherProvider.io) {
            geocachingApiLogin(geocachingApi)

            val resultQuality = if (liteData) {
                GeocachingApi.ResultQuality.LITE
            } else {
                GeocachingApi.ResultQuality.FULL
            }

            var itemsPerRequest = AppConstants.ITEMS_PER_REQUEST

            while (!channel.isClosedForReceive) {
                val points = channel.takeList(itemsPerRequest)
                val requestedCacheIds = points.map { it.gcData.cacheID }.toTypedArray()

                val startTimeMillis = System.currentTimeMillis()

                val cachesToAdd = geocachingApi.searchForGeocaches(
                    SearchForGeocachesRequest.builder()
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

                if (!cachesToAdd.isEmpty()) {
                    val receivedPoints = mapper.createLocusPoints(cachesToAdd)
                    for (oldPoint in points) {
                        val newPoint = receivedPoints.find { it.gcData.cacheID == oldPoint.gcData.cacheID }
                        send(Pair(oldPoint, newPoint))
                    }
                }

                itemsPerRequest = DownloadingUtil.computeItemsPerRequest(itemsPerRequest, startTimeMillis)
            }
        }
    }
}

private suspend fun <E> ReceiveChannel<E>.takeList(count: Int): List<E> {
    if (count <= 0) return emptyList()

    val list = mutableListOf<E>()
    var received = 0

    for (item in this) {
        list.add(item)
        if (++received >= count) return list
    }

    return emptyList()
}
