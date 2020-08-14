package com.arcao.geocaching4locus.data.api.model

import com.squareup.moshi.JsonClass
import java.time.LocalDateTime

@JsonClass(generateAdapter = true)
data class UserData(
    val foundDate: LocalDateTime?, // 2018-06-06T06:16:54.165Z
    val dnfDate: LocalDateTime?, // 2018-06-06T06:16:54.165Z
    val correctedCoordinates: Coordinates?,
    val isFavorited: Boolean, // true
    val note: String? // string
)
