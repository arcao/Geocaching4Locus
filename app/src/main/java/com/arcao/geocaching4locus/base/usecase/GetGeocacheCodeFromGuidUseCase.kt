package com.arcao.geocaching4locus.base.usecase

import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.data.api.WherigoApiRepository
import com.arcao.geocaching4locus.error.exception.CacheNotFoundException
import kotlinx.coroutines.withContext

class GetGeocacheCodeFromGuidUseCase(
    private val repository: WherigoApiRepository,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) {
    suspend operator fun invoke(guid: String) = withContext(dispatcherProvider.io) {
        repository.guidToReferenceCode(guid) ?: throw CacheNotFoundException(guid)
    }
}