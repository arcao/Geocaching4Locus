package com.arcao.geocaching4locus.base.usecase

import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import locus.api.manager.LocusMapManager
import locus.api.objects.geoData.Point

class SendPointsSilentToLocusMapUseCase(
    private val locusMapManager: LocusMapManager,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) {
    suspend operator fun invoke(
        packPrefix: String,
        pointListFlow: Flow<List<Point>>
    ) = withContext(dispatcherProvider.computation) {
        var id = 1

        pointListFlow.collect { pointList ->
            locusMapManager.sendPointsSilent("$packPrefix$id", pointList)
            id++
        }
    }
}
