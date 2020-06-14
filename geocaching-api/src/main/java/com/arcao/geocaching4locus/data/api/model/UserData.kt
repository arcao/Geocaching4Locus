package com.arcao.geocaching4locus.data.api.model

import java.time.LocalDateTime

data class UserData(
    val foundDate: LocalDateTime?, // 2018-06-06T06:16:54.165Z
    val dnfDate: LocalDateTime?, // 2018-06-06T06:16:54.165Z
    val correctedCoordinates: Coordinates?,
    val isFavorited: Boolean, // true
    val note: String? // string
)
