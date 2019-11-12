package com.arcao.geocaching4locus.base.usecase

import com.arcao.geocaching4locus.authentication.util.restrictions
import com.arcao.geocaching4locus.base.constants.AppConstants
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.util.DownloadingUtil
import com.arcao.geocaching4locus.data.account.AccountManager
import com.arcao.geocaching4locus.data.api.GeocachingApiRepository
import com.arcao.geocaching4locus.data.api.model.Coordinates
import com.arcao.geocaching4locus.error.exception.NoResultFoundException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import locus.api.mapper.DataMapper
import timber.log.Timber
import kotlin.math.min

class GetPointsFromRectangleCoordinatesUseCase(
    private val repository: GeocachingApiRepository,
    private val geocachingApiLogin: GeocachingApiLoginUseCase,
    private val accountManager: AccountManager,
    private val geocachingApiFilterProvider: GeocachingApiFilterProvider,
    private val mapper: DataMapper,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) {
    @UseExperimental(ExperimentalCoroutinesApi::class)
    suspend operator fun invoke(
        scope: CoroutineScope,
        centerCoordinates: Coordinates,
        topLeftCoordinates: Coordinates,
        bottomRightCoordinates: Coordinates,
        liteData: Boolean = true,
        geocacheLogsCount: Int = 0,
        downloadDisabled: Boolean = false,
        downloadFound: Boolean = false,
        downloadOwn: Boolean = false,
        geocacheTypes: IntArray = intArrayOf(),
        containerTypes: IntArray = intArrayOf(),
        difficultyMin: Float = 1F,
        difficultyMax: Float = 5F,
        terrainMin: Float = 1F,
        terrainMax: Float = 5F,
        excludeIgnoreList: Boolean = true,
        maxCount: Int = 50,
        countHandler: (Int) -> Unit = {}
    ) = scope.produce(dispatcherProvider.io) {
        geocachingApiLogin()

        var count = AppConstants.ITEMS_PER_REQUEST
        var current = 0

        var itemsPerRequest = AppConstants.ITEMS_PER_REQUEST
        while (current < count) {
            val startTimeMillis = System.currentTimeMillis()

            val geocaches = repository.search(
                filters = geocachingApiFilterProvider(
                    centerCoordinates,
                    topLeftCoordinates,
                    bottomRightCoordinates,
                    downloadDisabled,
                    downloadFound,
                    downloadOwn,
                    geocacheTypes,
                    containerTypes,
                    difficultyMin,
                    difficultyMax,
                    terrainMin,
                    terrainMax
                ),
                logsCount = geocacheLogsCount,
                lite = liteData,
                skip = current,
                take = min(itemsPerRequest, count - current)
            ).also {
                count = min(it.totalCount, maxCount.toLong()).toInt()
                withContext(dispatcherProvider.computation) {
                    countHandler(count)
                }
            }

            accountManager.restrictions().updateLimits(repository.userLimits())

            yield()

            if (geocaches.isEmpty())
                break

            send(mapper.createLocusPoints(geocaches))
            current += geocaches.size

            itemsPerRequest = DownloadingUtil.computeItemsPerRequest(itemsPerRequest, startTimeMillis)
        }

        Timber.v("found geocaches: %d", current)

        if (current == 0) {
            throw NoResultFoundException()
        }
    }
}
