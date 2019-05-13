package com.arcao.geocaching4locus.data.api.internal.moshi.adapter

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.ToJson
import org.threeten.bp.*
import org.threeten.bp.format.DateTimeFormatter

class Java8TimeAdapter {
    @ToJson
    fun offsetDateTimeToJson(value: OffsetDateTime): String {
        return OFFSET_FORMATTER.format(value)
    }
    
    @FromJson
    fun offsetDateTimeFromJson(value: String): OffsetDateTime {
        return OFFSET_FORMATTER.parse(value, OffsetDateTime::from)
    }

    @ToJson
    fun instantToJson(value: Instant): String {
        return INSTANT_FORMATTER.format(value)
    }

    @FromJson
    fun instantFromJson(value: String): Instant {
        return INSTANT_FORMATTER.parse(value, Instant::from)
    }

    @ToJson
    fun groundspeakOffsetDateTimeToJson(@LocalDateTimePTZone value: OffsetDateTime): String {
        return value.atZoneSameInstant(PT_ZONE).toLocalDateTime().format(LOCAL_DATE_TIME_FORMATTER)
    }

    @FromJson
    @LocalDateTimePTZone
    fun groundspeakOffsetDateTimeFromJson(value: String): OffsetDateTime {
        return LOCAL_DATE_TIME_FORMATTER.parse(value, LocalDateTime::from).atZone(PT_ZONE).toOffsetDateTime()
    }

    @ToJson
    fun groundspeakInstantToJson(@LocalDateTimePTZone value: Instant): String {
        return value.atZone(PT_ZONE).toLocalDateTime().format(LOCAL_DATE_TIME_FORMATTER)
    }

    @FromJson
    @LocalDateTimePTZone
    fun groundspeakInstantFromJson(value: String): Instant {
        return LOCAL_DATE_TIME_FORMATTER.parse(value, LocalDateTime::from).atZone(PT_ZONE).toInstant()
    }


    @ToJson
    fun durationToJson(value: Duration): String {
        return value.seconds.toString()
    }

    @FromJson
    fun durationFromJson(value: String): Duration {
        return Duration.ofSeconds(value.toLong())
    }

    companion object {
        private val PT_ZONE = ZoneId.of("America/Los_Angeles")
        private val OFFSET_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME
        private val INSTANT_FORMATTER = DateTimeFormatter.ISO_INSTANT
        private val LOCAL_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    }
}

@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class LocalDateTimePTZone