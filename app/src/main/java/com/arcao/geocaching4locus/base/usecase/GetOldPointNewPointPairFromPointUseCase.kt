package com.arcao.geocaching4locus.base.usecase

import com.arcao.geocaching4locus.authentication.util.restrictions
import com.arcao.geocaching4locus.base.constants.AppConstants
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.util.DownloadingUtil
import com.arcao.geocaching4locus.base.util.takeListVariable
import com.arcao.geocaching4locus.data.account.AccountManager
import com.arcao.geocaching4locus.data.api.GeocachingApiRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.yield
import locus.api.mapper.DataMapper
import locus.api.objects.geoData.Point

class GetOldPointNewPointPairFromPointUseCase(
    private val repository: GeocachingApiRepository,
    private val geocachingApiLogin: GeocachingApiLoginUseCase,
    private val accountManager: AccountManager,
    private val mapper: DataMapper,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) {
    @Suppress("BlockingMethodInNonBlockingContext")
    suspend operator fun invoke(
        flow: Flow<Point>,
        liteData: Boolean = true,
        geocacheLogsCount: Int = 0
    ) = flow {
        geocachingApiLogin()

        flow.takeListVariable(AppConstants.ITEMS_PER_REQUEST) { points ->
            val requestedCacheIds = points.map { it.gcData?.cacheID }.filterNotNull().toTypedArray()

            val startTimeMillis = System.currentTimeMillis()

            val cachesToAdd = repository.geocaches(
                referenceCodes = requestedCacheIds,
                logsCount = geocacheLogsCount,
                lite = liteData
            )

            accountManager.restrictions().updateLimits(repository.userLimits())

            yield()

            if (cachesToAdd.isNotEmpty()) {
                val receivedPoints = mapper.createLocusPoints(cachesToAdd)
                for (oldPoint in points) {
                    val newPoint =
                        receivedPoints.find { it.gcData?.cacheID == oldPoint.gcData?.cacheID }
                    emit(Pair(oldPoint, newPoint))
                }
            }

            DownloadingUtil.computeItemsPerRequest(points.size, startTimeMillis)
        }
    }.flowOn(dispatcherProvider.io)
}
