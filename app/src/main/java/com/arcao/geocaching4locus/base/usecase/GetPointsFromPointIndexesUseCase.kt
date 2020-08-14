package com.arcao.geocaching4locus.base.usecase

import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.util.isGeocache
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import locus.api.manager.LocusMapManager
import locus.api.objects.extra.Point

class GetPointsFromPointIndexesUseCase(
    private val locusMapManager: LocusMapManager,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) {
    operator fun invoke(pointIndexes: LongArray): Flow<Point> =
        pointIndexes.asFlow()
            .map { locusMapManager.getPoint(it) }
            .filterNotNull()
            .filter { it.isGeocache() }
            .flowOn(dispatcherProvider.io)
}
