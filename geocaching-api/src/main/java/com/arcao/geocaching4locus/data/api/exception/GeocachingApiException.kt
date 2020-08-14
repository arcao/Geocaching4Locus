package com.arcao.geocaching4locus.data.api.exception

import com.arcao.geocaching4locus.data.api.model.enums.StatusCode

open class GeocachingApiException(
    val statusCode: StatusCode,
    val statusMessage: String,
    val errorMessage: String? = null
) :
    Exception("$statusCode ($statusMessage): $errorMessage")
