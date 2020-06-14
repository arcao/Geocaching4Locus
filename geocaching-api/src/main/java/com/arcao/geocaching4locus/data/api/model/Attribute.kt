package com.arcao.geocaching4locus.data.api.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Attribute(
    val id: Int, // 0
    val name: String, // string
    val isOn: Boolean, // true
    val imageUrl: String // string
)
