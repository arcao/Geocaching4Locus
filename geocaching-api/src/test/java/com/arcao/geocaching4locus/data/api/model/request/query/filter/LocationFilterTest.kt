package com.arcao.geocaching4locus.data.api.model.request.query.filter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal object LocationFilterTest {
    @Test
    fun verifyPositiveLocationFilter() {
        val given = LocationFilter(50.0, 14.0)
        val expected = "loc:[50.000000,14.000000]"

        assertEquals(expected, given.toString())
    }

    @Test
    fun verifyNegativeLocationFilter() {
        val given = LocationFilter(-50.0, -14.0)
        val expected = "loc:[-50.000000,-14.000000]"

        assertEquals(expected, given.toString())
    }
}
