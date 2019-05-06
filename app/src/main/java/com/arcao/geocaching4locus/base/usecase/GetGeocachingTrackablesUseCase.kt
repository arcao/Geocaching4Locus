package com.arcao.geocaching4locus.base.usecase

import com.arcao.geocaching.api.GeocachingApi
import com.arcao.geocaching4locus.base.constants.AppConstants
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import locus.api.mapper.TrackableConverter

/**
 * Created by Arcao on 30.12.2018.
 */
class GetGeocachingTrackablesUseCase(
    private val geocachingApi: GeocachingApi,
    private val geocachingApiLogin: GeocachingApiLoginUseCase,
    private val trackableConverter: TrackableConverter,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) {
    @UseExperimental(ExperimentalCoroutinesApi::class)
    suspend operator fun invoke(geocacheCode: String, start: Int = 0, count: Int = AppConstants.TRACKABLES_MAX) =
        coroutineScope {
            produce(dispatcherProvider.io) {
                geocachingApiLogin(geocachingApi)

                var current = start

                while (current < count) {
                    val logs = geocachingApi.getTrackablesByCacheCode(
                        geocacheCode,
                        current,
                        Math.min(count - current, AppConstants.TRACKEBLES_PER_REQUEST),
                        0
                    )

                    if (!isActive || logs.isEmpty())
                        return@produce

                    send(trackableConverter.createLocusGeocachingTrackables(logs))

                    current += logs.size
                }
            }
        }
}