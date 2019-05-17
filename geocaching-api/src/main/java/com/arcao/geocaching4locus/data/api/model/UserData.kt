package com.arcao.geocaching4locus.data.api.model

import org.threeten.bp.Instant

data class UserData(
        val foundDate: Instant?, // 2018-06-06T06:16:54.165Z
        val dnfDate: Instant?, // 2018-06-06T06:16:54.165Z
        val correctedCoordinates: Coordinates?,
        val isFavorited: Boolean, // true
        val note: String? // string
)