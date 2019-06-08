package com.arcao.geocaching4locus.data.api.endpoint

import com.arcao.geocaching4locus.data.api.model.response.GuidToReferenceCodeResponse
import kotlinx.coroutines.Deferred
import retrofit2.http.GET
import retrofit2.http.Query

interface WherigoApiEndpoint {
    @GET("api/v2/guidToReferenceCode")
    fun guidToReferenceCodeAsync(@Query("guid") guid: String): Deferred<GuidToReferenceCodeResponse>
}
