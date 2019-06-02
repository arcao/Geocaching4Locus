package com.arcao.geocaching4locus.data.api.util

import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

fun LocalDateTime?.toSafeInstant(zone: String?): Instant? {
    return if (this != null) {
        val safeZone = zone?.takeIf(String::isNotEmpty) ?: "Etc/UTC"
        ZonedDateTime.of(this, ZoneId.of(safeZone)).toInstant()
    } else {
        null
    }
}