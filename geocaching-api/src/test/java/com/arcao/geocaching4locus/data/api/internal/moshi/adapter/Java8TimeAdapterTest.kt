package com.arcao.geocaching4locus.data.api.internal.moshi.adapter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.threeten.bp.*
import org.threeten.bp.zone.TzdbZoneRulesProvider
import org.threeten.bp.zone.ZoneRulesInitializer
import org.threeten.bp.zone.ZoneRulesProvider

internal object Java8TimeAdapterTest {
    private val PT_ZONE: ZoneId by lazy { ZoneId.of("America/Los_Angeles") }
    private lateinit var adapter: Java8TimeAdapter

    @JvmStatic
    @BeforeAll
    fun setupThreeTenABP() {
        // load TZDB for ThreeTenABP
        ZoneRulesInitializer.setInitializer(object : ZoneRulesInitializer() {
            override fun initializeProviders() {
                val stream = this::class.java.getResourceAsStream("/TZDB.dat")
                stream.use {
                    ZoneRulesProvider.registerProvider(TzdbZoneRulesProvider(it))
                }
            }
        })
    }

    @BeforeEach
    fun setup() {
        adapter = Java8TimeAdapter()
    }

    @Test
    fun verifyOffsetDateTimeToJson() {
        val given = LocalDateTime.of(2018, Month.JANUARY, 20, 12, 13, 14)
                .atOffset(ZoneOffset.UTC)
        val expected = "2018-01-20T12:13:14Z"

        assertEquals(expected, adapter.offsetDateTimeToJson(given))
    }

    @Test
    fun verifyOffsetDateTimeFromJson() {
        val given = "2018-01-20T12:13:14Z"
        val expected = LocalDateTime.of(2018, Month.JANUARY, 20, 12, 13, 14)
                .atOffset(ZoneOffset.UTC)

        assertEquals(expected, adapter.offsetDateTimeFromJson(given))
    }

    @Test
    fun verifyInstantToJson() {
        val given = LocalDateTime.of(2018, Month.JANUARY, 20, 12, 13, 14)
                .atOffset(ZoneOffset.UTC)
                .toInstant()
        val expected = "2018-01-20T12:13:14Z"

        assertEquals(expected, adapter.instantToJson(given))
    }

    @Test
    fun verifyInstantFromJson() {
        val given = "2018-01-20T12:13:14Z"
        val expected = LocalDateTime.of(2018, Month.JANUARY, 20, 12, 13, 14)
                .atOffset(ZoneOffset.UTC)
                .toInstant()

        assertEquals(expected, adapter.instantFromJson(given))
    }

    @Test
    fun verifyGroundspeakInstantToJsonPTZone() {
        val given = LocalDateTime.of(2018, Month.JANUARY, 20, 12, 13, 14)
                .atZone(PT_ZONE)
                .toInstant()
        val expected = "2018-01-20T12:13:14"

        assertEquals(expected, adapter.groundspeakInstantToJson(given))
    }

    @Test
    fun verifyGroundspeakInstantToJsonUTCOffset() {
        val given = LocalDateTime.of(2018, Month.JANUARY, 20, 12, 13, 14)
                .atOffset(ZoneOffset.UTC)
                .toInstant()
        // LocalDateTime in PST zone
        val expected = "2018-01-20T04:13:14"

        assertEquals(expected, adapter.groundspeakInstantToJson(given))
    }

    @Test
    fun verifyGroundspeakInstantFromJsonPT() {
        val given = "2018-01-20T12:13:14"
        val expected = LocalDateTime.of(2018, Month.JANUARY, 20, 12, 13, 14)
                .atZone(PT_ZONE)
                .toInstant()

        assertEquals(expected, adapter.groundspeakInstantFromJson(given))
    }

    @Test
    fun verifyDurationToJson() {
        val given = Duration.ofSeconds(300)
        val expected = "300"

        assertEquals(expected, adapter.durationToJson(given))
    }

    @Test
    fun verifyDurationFromJson() {
        val given = "300"
        val expected = Duration.ofSeconds(300)

        assertEquals(expected, adapter.durationFromJson(given))
    }
}