package com.arcao.geocaching4locus.base.usecase

import com.arcao.geocaching.api.GeocachingApi
import com.arcao.geocaching4locus.base.constants.AppConstants
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.yield
import locus.api.mapper.GeocacheLogConverter

class GetGeocachingLogsUseCase(
    private val geocachingApi: GeocachingApi,
    private val geocachingApiLogin: GeocachingApiLoginUseCase,
    private val geocacheLogConverter: GeocacheLogConverter,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) {
    @UseExperimental(ExperimentalCoroutinesApi::class)
    suspend operator fun invoke(
        scope: CoroutineScope,
        geocacheCode: String,
        start: Int = 0,
        count: Int = 0
    ) = scope.produce(dispatcherProvider.io) {
        geocachingApiLogin(geocachingApi)

        var current = start

        while (current < count) {
            val logs = geocachingApi.getGeocacheLogsByCacheCode(
                geocacheCode,
                current,
                Math.min(count - current, AppConstants.LOGS_PER_REQUEST)
            )

            yield()

            if (logs.isEmpty())
                return@produce

            send(geocacheLogConverter.createLocusGeocachingLogs(logs))

            current += logs.size
        }
    }
}
