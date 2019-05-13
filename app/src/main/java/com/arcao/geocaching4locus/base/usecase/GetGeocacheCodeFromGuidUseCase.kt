package com.arcao.geocaching4locus.base.usecase

import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.error.exception.CacheNotFoundException
import com.arcao.wherigoservice.api.WherigoService
import kotlinx.coroutines.withContext

class GetGeocacheCodeFromGuidUseCase(
    private val wherigoService: WherigoService,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) {
    suspend operator fun invoke(guid: String) = withContext(dispatcherProvider.io) {
        wherigoService.getCacheCodeFromGuid(guid) ?: throw CacheNotFoundException(guid)
    }
}