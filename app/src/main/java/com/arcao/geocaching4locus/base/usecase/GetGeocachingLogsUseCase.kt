package com.arcao.geocaching4locus.base.usecase

import com.arcao.geocaching4locus.base.constants.AppConstants
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.data.api.GeocachingApiRepository
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.yield
import locus.api.mapper.GeocacheLogConverter
import kotlin.math.min

class GetGeocachingLogsUseCase(
    private val repository: GeocachingApiRepository,
    private val geocachingApiLogin: GeocachingApiLoginUseCase,
    private val geocacheLogConverter: GeocacheLogConverter,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) {
    @Suppress("BlockingMethodInNonBlockingContext")
    suspend operator fun invoke(
        referenceCode: String,
        start: Int = 0,
        count: Int = 0
    ) = flow {
        geocachingApiLogin()

        var current = start

        while (current < count) {
            val logs = repository.geocacheLogs(
                referenceCode = referenceCode,
                skip = current,
                take = min(count - current, AppConstants.LOGS_PER_REQUEST)
            )

            yield()

            if (logs.isEmpty())
                return@flow

            emit(geocacheLogConverter.createLocusGeocachingLogs(logs))

            current += logs.size
        }
    }.flowOn(dispatcherProvider.io)
}
