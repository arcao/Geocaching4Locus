package com.arcao.geocaching4locus.data.api.model.request.query.filter

import com.arcao.geocaching4locus.data.api.model.GeocacheSize
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal object GeocacheSizeFilterTest {
    @Test
    fun verifyPositiveGeocacheSize() {
        val given = GeocacheSizeFilter(GeocacheSize.MICRO, GeocacheSize.LARGE)
        val expected = "type:2,4"

        assertEquals(expected, given.toString())
    }

    @Test
    fun verifyNegativeConstructorGeocacheSize() {
        val given = GeocacheSizeFilter(GeocacheSize.MICRO, GeocacheSize.LARGE, not = true)
        val expected = "type:not(2,4)"

        assertEquals(expected, given.toString())
    }

    @Test
    fun verifyNegativeMethodGeocacheSize() {
        val given = GeocacheSizeFilter(GeocacheSize.MICRO, GeocacheSize.LARGE).not()
        val expected = "type:not(2,4)"

        assertEquals(expected, given.toString())
    }
}