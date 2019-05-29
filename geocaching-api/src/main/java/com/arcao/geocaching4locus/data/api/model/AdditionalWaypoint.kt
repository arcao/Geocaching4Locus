package com.arcao.geocaching4locus.data.api.model

import com.arcao.geocaching4locus.data.api.model.enums.AdditionalWaypointType
import com.squareup.moshi.Json

data class AdditionalWaypoint(
        val name: String, // string
        val coordinates: Coordinates?,
        val description: String?, // string
        @Json(name = "typeId") val type: AdditionalWaypointType, // 0
        val prefix: String, // string
        val url: String? // string
)