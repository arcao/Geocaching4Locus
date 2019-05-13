package com.arcao.geocaching4locus.base.usecase

import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.withContext
import locus.api.manager.LocusMapManager
import locus.api.objects.extra.Point

class SendPointsSilentToLocusMapUseCase(
    private val locusMapManager: LocusMapManager,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) {
    suspend operator fun invoke(
        packPrefix: String,
        pointListChannel: ReceiveChannel<List<Point>>
    ) = withContext(dispatcherProvider.computation) {
        var id = 1
        for (pointList in pointListChannel) {
            locusMapManager.sendPointsSilent("$packPrefix$id", pointList)
            id++
        }
    }
}