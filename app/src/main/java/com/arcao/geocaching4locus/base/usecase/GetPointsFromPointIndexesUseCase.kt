package com.arcao.geocaching4locus.base.usecase

import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.util.isGeocache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.produce
import locus.api.manager.LocusMapManager

class GetPointsFromPointIndexesUseCase(
    private val locusMapManager: LocusMapManager,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) {
    @UseExperimental(ExperimentalCoroutinesApi::class)
    suspend operator fun invoke(scope: CoroutineScope, pointIndexes: LongArray) =
        scope.produce(dispatcherProvider.io, capacity = 50) {
            for (pointIndex in pointIndexes) {
                val point = locusMapManager.getPoint(pointIndex)
                if (!point.isGeocache()) continue

                send(point!!)
            }
        }
}

