package com.arcao.geocaching4locus.data.api.model

data class UserWaypoint(
        val referenceCode: String, // string
        val description: String, // string
        val isCorrectedCoordinates: Boolean, // true
        val coordinates: Coordinates,
        val geocacheCode: String // string
)