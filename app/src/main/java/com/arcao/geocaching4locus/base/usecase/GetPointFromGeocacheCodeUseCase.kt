package com.arcao.geocaching4locus.base.usecase

import com.arcao.geocaching4locus.authentication.util.restrictions
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.data.account.AccountManager
import com.arcao.geocaching4locus.data.api.GeocachingApiRepository
import com.arcao.geocaching4locus.data.api.exception.GeocachingApiException
import com.arcao.geocaching4locus.data.api.model.enum.StatusCode
import com.arcao.geocaching4locus.error.exception.CacheNotFoundException
import kotlinx.coroutines.withContext
import locus.api.mapper.DataMapper

class GetPointFromGeocacheCodeUseCase(
    private val repository: GeocachingApiRepository,
    private val geocachingApiLogin: GeocachingApiLoginUseCase,
    private val accountManager: AccountManager,
    private val mapper: DataMapper,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) {
    suspend operator fun invoke(
        referenceCode: String,
        liteData: Boolean = true,
        geocacheLogsCount: Int = 0
    ) = withContext(dispatcherProvider.io) {
        geocachingApiLogin()

        try {
            val geocache = repository.geocache(
                referenceCode = referenceCode,
                lite = liteData,
                logsCount = geocacheLogsCount
            )

            accountManager.restrictions().updateLimits(repository.userLimits())

            mapper.createLocusPoint(geocache)
        } catch (e : GeocachingApiException) {
            if (e.statusCode == StatusCode.NOT_FOUND) {
                throw CacheNotFoundException(referenceCode)
            }

            throw  e
        }
    }
}
