package com.arcao.geocaching4locus.data.api.exception

import com.arcao.geocaching4locus.data.api.model.enum.StatusCode

class AuthenticationException(statusCode: StatusCode, statusMessage: String, errorMessage: String? = null) :
    GeocachingApiException(statusCode, statusMessage, errorMessage) {
}
