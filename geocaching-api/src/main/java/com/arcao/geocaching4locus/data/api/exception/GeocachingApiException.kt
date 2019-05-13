package com.arcao.geocaching4locus.data.api.exception

import com.arcao.geocaching4locus.data.api.model.enum.StatusCode

class GeocachingApiException(val statusCode : StatusCode, val statusMessage: String, val errorMessage : String) : Exception("$statusMessage: $errorMessage")