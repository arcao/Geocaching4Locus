package com.arcao.geocaching4locus.base.usecase

import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.util.isGeocache
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import locus.api.manager.LocusMapManager
import locus.api.objects.extra.Point

class GetPointsFromPointIndexesUseCase(
    private val locusMapManager: LocusMapManager,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) {
    @UseExperimental(ExperimentalCoroutinesApi::class)
    suspend operator fun invoke(pointIndexes: LongArray) = flow<Point> {
        for (pointIndex in pointIndexes) {
            val point = locusMapManager.getPoint(pointIndex)
            if (!point.isGeocache()) continue

            emit(point)
        }
    }.flowOn(dispatcherProvider.computation)
}
