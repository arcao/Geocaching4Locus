package com.arcao.geocaching4locus.data.api.model.request.query.filter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal object RadiusFilterTest {
    @Test
    fun verifyKilometerRadiusFilter() {
        val given = RadiusFilter(10, DistanceUnit.KILOMETER)
        val expected = "radius:10km"

        assertEquals(expected, given.toString())
    }

    @Test
    fun verifyMeterRadiusFilter() {
        val given = RadiusFilter(600, DistanceUnit.METER)
        val expected = "radius:600m"

        assertEquals(expected, given.toString())
    }

    @Test
    fun verifyMileRadiusFilter() {
        val given = RadiusFilter(55, DistanceUnit.MILE)
        val expected = "radius:55mi"

        assertEquals(expected, given.toString())
    }
}