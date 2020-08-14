package com.arcao.geocaching4locus.data.api.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Location(
    val country: String, // string
    val state: String // string
)
