package com.arcao.geocaching4locus.data.api.model

import com.arcao.geocaching4locus.data.api.util.ReferenceCode
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserWaypoint(
    val referenceCode: String, // string
    val description: String?, // string
    val isCorrectedCoordinates: Boolean, // true
    val coordinates: Coordinates,
    val geocacheCode: String // string
) {
    val id by lazy {
        ReferenceCode.toId(referenceCode)
    }
}
