package com.arcao.geocaching4locus.base.usecase

import com.arcao.geocaching.api.GeocachingApi
import com.arcao.geocaching.api.data.SearchForGeocachesRequest
import com.arcao.geocaching.api.data.coordinates.Coordinates
import com.arcao.geocaching.api.data.type.ContainerType
import com.arcao.geocaching.api.data.type.GeocacheType
import com.arcao.geocaching4locus.authentication.util.AccountManager
import com.arcao.geocaching4locus.base.constants.AppConstants
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.util.DownloadingUtil
import com.arcao.geocaching4locus.error.exception.NoResultFoundException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import locus.api.mapper.DataMapper
import timber.log.Timber

@ExperimentalCoroutinesApi
class GetPointsFromRectangleCoordinatesUseCase(
    private val geocachingApi: GeocachingApi,
    private val geocachingApiLogin: GeocachingApiLoginUseCase,
    private val accountManager: AccountManager,
    private val geocachingApiFilterProvider: GeocachingApiFilterProvider,
    private val mapper: DataMapper,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) {
    suspend operator fun invoke(
        centerCoordinates: Coordinates,
        topLeftCoordinates: Coordinates,
        bottomRightCoordinates: Coordinates,
        liteData: Boolean = true,
        summaryData: Boolean = false,
        geocacheLogsCount: Int = 0,
        trackableLogsCount: Int = 0,
        downloadDisabled: Boolean = false,
        downloadFound: Boolean = false,
        downloadOwn: Boolean = false,
        geocacheTypes: Array<GeocacheType> = emptyArray(),
        containerTypes: Array<ContainerType> = emptyArray(),
        difficultyMin : Float = 1F,
        difficultyMax: Float = 5F,
        terrainMin : Float = 1F,
        terrainMax: Float = 5F,
        excludeIgnoreList : Boolean = true,
        maxCount : Int = 50,
        countHandler: (Int) -> Unit = {}
    ) = coroutineScope {
        produce(dispatcherProvider.io) {
            geocachingApiLogin(geocachingApi)

            val resultQuality = when {
                liteData && !summaryData -> GeocachingApi.ResultQuality.LITE
                !liteData && !summaryData -> GeocachingApi.ResultQuality.FULL
                summaryData -> GeocachingApi.ResultQuality.SUMMARY
                else -> throw IllegalStateException("Invalid ResultQuality combination.")
            }

            var count = AppConstants.ITEMS_PER_REQUEST
            var current = 0

            var itemsPerRequest = AppConstants.ITEMS_PER_REQUEST
            while (current < count) {
                val startTimeMillis = System.currentTimeMillis()

                val geocaches = if (current == 0) {
                    geocachingApi.searchForGeocaches(
                        SearchForGeocachesRequest.builder()
                            .resultQuality(resultQuality)
                            .maxPerPage(Math.min(itemsPerRequest, count - current))
                            .geocacheLogCount(geocacheLogsCount)
                            .trackableLogCount(trackableLogsCount)
                            .addFilters(
                                geocachingApiFilterProvider(
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
                                    terrainMax,
                                    excludeIgnoreList
                                )
                            )
                            .build()
                    ).also {
                        count = Math.min(geocachingApi.lastSearchResultsFound, maxCount)
                        withContext(dispatcherProvider.computation) {
                            countHandler(count)
                        }
                    }
                } else {
                    geocachingApi.getMoreGeocaches(
                        resultQuality,
                        current,
                        Math.min(itemsPerRequest, count - current),
                        geocacheLogsCount,
                        trackableLogsCount
                    )
                }

                accountManager.restrictions.updateLimits(geocachingApi.lastGeocacheLimits)

                if (!isActive)
                    return@produce

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
}
