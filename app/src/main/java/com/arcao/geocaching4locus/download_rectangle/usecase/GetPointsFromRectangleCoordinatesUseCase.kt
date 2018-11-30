package com.arcao.geocaching4locus.download_rectangle.usecase

import com.arcao.geocaching.api.GeocachingApi
import com.arcao.geocaching.api.data.SearchForGeocachesRequest
import com.arcao.geocaching.api.data.coordinates.Coordinates
import com.arcao.geocaching.api.data.type.ContainerType
import com.arcao.geocaching.api.data.type.GeocacheType
import com.arcao.geocaching4locus.authentication.util.AccountManager
import com.arcao.geocaching4locus.base.constants.AppConstants
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.usecase.GeocachingApiFiltersGenerator
import com.arcao.geocaching4locus.base.usecase.GeocachingApiLoginUseCase
import com.arcao.geocaching4locus.base.util.DownloadingUtil
import com.arcao.geocaching4locus.error.exception.NoResultFoundException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import locus.api.mapper.DataMapper
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

class GetPointsFromRectangleCoordinatesUseCase(
    private val geocachingApi: GeocachingApi,
    private val geocachingApiLoginUseCase: GeocachingApiLoginUseCase,
    private val accountManager: AccountManager,
    private val geocachingApiFiltersGenerator: GeocachingApiFiltersGenerator,
    private val mapper: DataMapper,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = dispatcherProvider.io

    @ExperimentalCoroutinesApi
    suspend operator fun invoke(
        centerCoordinates: Coordinates,
        topLeftCoordinates: Coordinates,
        bottomRightCoordinates: Coordinates,
        liteData: Boolean = true,
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
        countHandler: (Int) -> Unit = {}
    ) = produce {
        geocachingApiLoginUseCase(geocachingApi)

        val resultQuality = if (liteData) {
            GeocachingApi.ResultQuality.LITE
        } else {
            GeocachingApi.ResultQuality.FULL
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
                        .addFilters(geocachingApiFiltersGenerator(
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
                        ))
                        .build()
                ).also {
                    count = Math.min(geocachingApi.lastSearchResultsFound, AppConstants.LIVEMAP_CACHES_COUNT)
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
