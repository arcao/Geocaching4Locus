package com.arcao.geocaching4locus.base.usecase

import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.withContext
import locus.api.android.ActionDisplayPointsExtended
import locus.api.android.objects.PackPoints
import locus.api.objects.extra.Point
import locus.api.utils.StoreableWriter

class WritePointToPackPointsFileUseCase(
    private val dispatcherProvider: CoroutinesDispatcherProvider
) {
    suspend operator fun invoke(point: Point) = withContext(dispatcherProvider.io) {
        StoreableWriter(ActionDisplayPointsExtended.cacheFileOutputStream).use { writer ->
            val pack = PackPoints()
            pack.addWaypoint(point)
            writer.write(pack)
        }
    }

    suspend operator fun invoke(channel: ReceiveChannel<List<Point>>) = withContext(dispatcherProvider.io) {
        StoreableWriter(ActionDisplayPointsExtended.cacheFileOutputStream).use { writer ->
            for (points in channel) {
                val pack = PackPoints()
                points.forEach(pack::addWaypoint)
                writer.write(pack)
            }
        }
    }
}