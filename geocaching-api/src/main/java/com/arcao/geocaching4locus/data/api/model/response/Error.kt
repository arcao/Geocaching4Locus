package com.arcao.geocaching4locus.data.api.model.response

import com.arcao.geocaching4locus.data.api.model.enums.StatusCode

data class Error(
        val statusCode: StatusCode = StatusCode.BAD_REQUEST,
        val statusMessage: String = "",
        val errorMessage: String = ""
)