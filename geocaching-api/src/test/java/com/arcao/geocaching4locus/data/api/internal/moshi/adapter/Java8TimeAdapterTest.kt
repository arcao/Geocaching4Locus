package com.arcao.geocaching4locus.data.api.internal.moshi.adapter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.threeten.bp.Duration
import org.threeten.bp.LocalDateTime
import org.threeten.bp.Month
import org.threeten.bp.ZoneOffset
import org.threeten.bp.zone.TzdbZoneRulesProvider
import org.threeten.bp.zone.ZoneRulesInitializer
import org.threeten.bp.zone.ZoneRulesProvider

internal object Java8TimeAdapterTest {
    private lateinit var adapter: Java8TimeAdapter

    @JvmStatic
    @BeforeAll
    fun setupThreeTenABP() {
        try {
            // load TZDB for ThreeTenABP
            ZoneRulesInitializer.setInitializer(object : ZoneRulesInitializer() {
                override fun initializeProviders() {
                    val stream = this::class.java.getResourceAsStream("/TZDB.dat")
                    stream.use {
                        ZoneRulesProvider.registerProvider(TzdbZoneRulesProvider(it))
                    }
                }
            })
        } catch (ignored: IllegalStateException) {
            // ignored
        }
    }

    @BeforeEach
    fun setup() {
        adapter = Java8TimeAdapter()
    }

    @Test
    fun verifyInstantToJsonUTC() {
        val given = LocalDateTime.of(2018, Month.JANUARY, 20, 12, 13, 14)
                .toInstant(ZoneOffset.UTC)
        val expected = "2018-01-20T12:13:14"

        assertEquals(expected, adapter.instantToJsonUTC(given))
    }

    @Test
    fun verifyInstantFromJsonUTC() {
        val given = "2018-01-20T12:13:14.000"
        val expected = LocalDateTime.of(2018, Month.JANUARY, 20, 12, 13, 14)
                .toInstant(ZoneOffset.UTC)

        assertEquals(expected, adapter.instantFromJsonUTC(given))
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
    fun verifyLocalDateTimeToJson() {
        val given = LocalDateTime.of(2018, Month.JANUARY, 20, 12, 13, 14)
        val expected = "2018-01-20T12:13:14"

        assertEquals(expected, adapter.localDateTimeToJson(given))
    }

    @Test
    fun verifyLocalDateTimeFromJson() {
        val given = "2018-01-20T12:13:14.000"
        val expected = LocalDateTime.of(2018, Month.JANUARY, 20, 12, 13, 14)

        assertEquals(expected, adapter.localDateTimeFromJson(given))
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
