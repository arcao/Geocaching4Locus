package com.arcao.geocaching4locus.data.api.model.response

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class GuidToReferenceCodeResponse(
    val referenceCode: String?
)
