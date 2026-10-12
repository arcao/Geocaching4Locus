package com.arcao.geocaching4locus.base.usecase

import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.util.isGeocache
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import locus.api.manager.LocusMapManager
import locus.api.objects.geoData.Point

class GetPointsFromPointIndexesUseCase(
    private val locusMapManager: LocusMapManager,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) {
    operator fun invoke(pointIndexes: LongArray): Flow<Point> =
        pointIndexes.asFlow()
            .mapNotNull { locusMapManager.getPoint(it) }
            .filter { it.isGeocache() }
            .flowOn(dispatcherProvider.io)
}
