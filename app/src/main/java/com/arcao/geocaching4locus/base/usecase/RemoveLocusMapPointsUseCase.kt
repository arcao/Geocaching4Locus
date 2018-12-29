package com.arcao.geocaching4locus.base.usecase

import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import kotlinx.coroutines.withContext
import locus.api.manager.LocusMapManager

class RemoveLocusMapPointsUseCase(
    private val locusMapManager: LocusMapManager,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) {
    suspend operator fun invoke(
        packPrefix: String,
        fromPackId: Int,
        toPackId: Int
    ) = withContext(dispatcherProvider.computation) {
        try {
            for (id in fromPackId..toPackId) {
                locusMapManager.removePackFromLocus("$packPrefix$id")
            }
        } catch (ignored: Exception) {
            // ignored
        }
    }
}