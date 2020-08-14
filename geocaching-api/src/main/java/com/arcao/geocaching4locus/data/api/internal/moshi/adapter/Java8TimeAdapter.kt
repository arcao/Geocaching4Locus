package com.arcao.geocaching4locus.data.api.internal.moshi.adapter

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class Java8TimeAdapter {
    @ToJson
    fun instantToJsonUTC(@LocalDateTimeUTC value: Instant): String {
        return LOCAL_DATE_TIME_FORMATTER.format(LocalDateTime.ofInstant(value, UTC_ZONE))
    }

    @FromJson
    @LocalDateTimeUTC
    fun instantFromJsonUTC(value: String): Instant {
        return LOCAL_DATE_TIME_FORMATTER.parse(value, LocalDateTime::from).toInstant(UTC_ZONE)
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
    fun localDateTimeToJson(value: LocalDateTime): String {
        return value.format(LOCAL_DATE_TIME_FORMATTER)
    }

    @FromJson
    fun localDateTimeFromJson(value: String): LocalDateTime {
        return LOCAL_DATE_TIME_FORMATTER.parse(value, LocalDateTime::from)
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
        private val UTC_ZONE = ZoneOffset.UTC
        private val INSTANT_FORMATTER = DateTimeFormatter.ISO_INSTANT
        private val LOCAL_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    }
}
