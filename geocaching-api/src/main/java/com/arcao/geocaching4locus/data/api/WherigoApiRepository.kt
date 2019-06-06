package com.arcao.geocaching4locus.data.api

import com.arcao.geocaching4locus.data.api.endpoint.WherigoApiEndpoint
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WherigoApiRepository(private val endpoint: Lazy<WherigoApiEndpoint>) {
    suspend fun guidToReferenceCode(guid : String) = apiCall {
        guidToReferenceCodeAsync(guid)
    }.referenceCode


    private suspend inline fun <T : Any> apiCall(crossinline body: WherigoApiEndpoint.() -> Deferred<T>): T =
        withContext(Dispatchers.IO) {
            endpoint.value.body().await()
        }
}