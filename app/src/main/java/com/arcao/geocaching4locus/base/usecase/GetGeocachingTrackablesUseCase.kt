package com.arcao.geocaching4locus.base.usecase

import com.arcao.geocaching4locus.base.constants.AppConstants
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.data.api.GeocachingApiRepository
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.yield
import locus.api.mapper.TrackableConverter
import kotlin.math.min

/**
 * Created by Arcao on 30.12.2018.
 */
class GetGeocachingTrackablesUseCase(
    private val repository: GeocachingApiRepository,
    private val geocachingApiLogin: GeocachingApiLoginUseCase,
    private val trackableConverter: TrackableConverter,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) {
    @Suppress("BlockingMethodInNonBlockingContext")
    suspend operator fun invoke(
        referenceCode: String,
        start: Int = 0,
        count: Int = AppConstants.TRACKABLES_MAX
    ) = flow {
        geocachingApiLogin()

        var current = start

        while (current < count) {
            val logs = repository.geocacheTrackables(
                referenceCode = referenceCode,
                skip = current,
                take = min(count - current, AppConstants.TRACKEBLES_PER_REQUEST)
            )

            yield()

            if (logs.isEmpty())
                return@flow

            emit(trackableConverter.createLocusGeocachingTrackables(logs))

            current += logs.size
        }
    }
}
