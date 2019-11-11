package com.arcao.geocaching4locus.base.usecase

import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import locus.api.android.objects.PackPoints
import locus.api.manager.LocusMapManager
import locus.api.objects.extra.Point
import locus.api.utils.StoreableWriter

class WritePointToPackPointsFileUseCase(
    private val locusMapManager: LocusMapManager,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) {
    suspend operator fun invoke(point: Point) = withContext(dispatcherProvider.io) {
        StoreableWriter(locusMapManager.cacheFileOutputStream).use { writer ->
            val pack = PackPoints()
            pack.addWaypoint(point)
            writer.write(pack)
        }
    }

    suspend operator fun invoke(flow: Flow<Collection<Point>>) =
        withContext(dispatcherProvider.io) {
            StoreableWriter(locusMapManager.cacheFileOutputStream).use { writer ->
                flow.collect { points ->
                    val pack = PackPoints()
                    points.forEach(pack::addWaypoint)
                    writer.write(pack)
                }
            }
        }
}
