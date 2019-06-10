package com.arcao.geocaching4locus.data.api.util

import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.zone.ZoneRulesException
import timber.log.Timber

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
