package com.arcao.geocaching4locus.data.api

import com.arcao.geocaching4locus.data.api.endpoint.WherigoApiEndpoint
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WherigoApiRepository(private val endpoint: WherigoApiEndpoint) {
    suspend fun guidToReferenceCode(guid: String) = apiCall {
        endpoint.guidToReferenceCodeAsync(guid)
    }.referenceCode

    private suspend inline fun <T : Any> apiCall(crossinline body: WherigoApiEndpoint.() -> Deferred<T>): T =
        withContext(Dispatchers.IO) {
            endpoint.body().await()
        }
}
