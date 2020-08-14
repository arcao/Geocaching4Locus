package com.arcao.geocaching4locus.data.api.util

import timber.log.Timber
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.zone.ZoneRulesException

private const val ZONE_NAME_UTC = "Etc/UTC"

fun LocalDateTime?.toSafeInstant(zone: String?): Instant? {
    return if (this != null) {
        val zoneId = try {
            ZoneId.of(zone?.takeIf(String::isNotEmpty) ?: ZONE_NAME_UTC)
        } catch (e: ZoneRulesException) {
            Timber.e("Invalid zone '$zone'. Use '$ZONE_NAME_UTC' instead.")
            ZoneId.of(ZONE_NAME_UTC)
        }

        ZonedDateTime.of(this, zoneId).toInstant()
    } else {
        null
    }
}
