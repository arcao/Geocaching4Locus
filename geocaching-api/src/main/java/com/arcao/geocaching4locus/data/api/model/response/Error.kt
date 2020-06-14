package com.arcao.geocaching4locus.data.api.model.response

import com.arcao.geocaching4locus.data.api.model.enums.StatusCode
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Error(
    val statusCode: StatusCode = StatusCode.BAD_REQUEST,
    val statusMessage: String = "",
    val errorMessage: String = ""
)
