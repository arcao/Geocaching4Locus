package com.arcao.geocaching4locus.data.api.model.request.query.filter

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal object RadiusFilterTest {
    @Test
    fun verifyKilometerRadiusFilter() {
        val given = RadiusFilter(10F, DistanceUnit.KILOMETER)
        val expected = "radius:10.0km"

        assertEquals(expected, given.toString())
    }

    @Test
    fun verifyMeterRadiusFilter() {
        val given = RadiusFilter(600F, DistanceUnit.METER)
        val expected = "radius:600.0m"

        assertEquals(expected, given.toString())
    }

    @Test
    fun verifyMileRadiusFilter() {
        val given = RadiusFilter(55.5F, DistanceUnit.MILE)
        val expected = "radius:55.5mi"

        assertEquals(expected, given.toString())
    }
}