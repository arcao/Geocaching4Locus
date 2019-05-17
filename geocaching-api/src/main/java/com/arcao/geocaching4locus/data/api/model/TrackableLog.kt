package com.arcao.geocaching4locus.data.api.model

import com.arcao.geocaching4locus.data.api.internal.moshi.adapter.LocalDateTimePTZone
import org.threeten.bp.Instant

data class TrackableLog(
    val referenceCode: String, // string
    val ownerCode: String, // string
    val imageCount: Int, // 0
    val trackableCode: String, // string
    val geocacheCode: String, // string
    val geocacheName: String, // string
    @LocalDateTimePTZone val loggedDate: Instant, // 2018-06-06T06:16:54.589Z
    val text: String, // string
    val isRot13Encoded: Boolean, // true
    val trackableLogType: TrackableLogType, // type
    val coordinates: Coordinates, // Coordinates
    val url: String, // string
    val owner: User // user
)