package com.arcao.geocaching4locus.base.usecase

import com.arcao.geocaching4locus.base.constants.AppConstants
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.data.api.GeocachingApiRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.produce
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
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend operator fun invoke(
        scope: CoroutineScope,
        referenceCode: String,
        start: Int = 0,
        count: Int = AppConstants.TRACKABLES_MAX
    ) = scope.produce(dispatcherProvider.io) {
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
                return@produce

            send(trackableConverter.createLocusGeocachingTrackables(logs))

            current += logs.size
        }
    }
}
