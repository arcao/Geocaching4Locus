package com.arcao.geocaching4locus.data.api.model

import com.arcao.geocaching4locus.data.api.internal.moshi.adapter.LocalDateTimePTZone
import org.threeten.bp.Instant

data class UserData(
        @LocalDateTimePTZone val foundDate: Instant?, // 2018-06-06T06:16:54.165Z
        @LocalDateTimePTZone val dnfDate: Instant?, // 2018-06-06T06:16:54.165Z
        val correctedCoordinates: Coordinates?,
        val isFavorited: Boolean, // true
        val note: String? // string
)