package com.arcao.geocaching4locus.data.api.model.request.query.filter

import com.arcao.geocaching4locus.data.api.model.GeocacheType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal object GeocacheTypeFilterTest {
    @Test
    fun verifyPositiveGeocacheType() {
        val given = GeocacheTypeFilter(GeocacheType.TRADITIONAL, GeocacheType.MULTI_CACHE)
        val expected = "type:2,3"

        assertEquals(expected, given.toString())
    }

    @Test
    fun verifyNegativeConstructorGeocacheType() {
        val given =
            GeocacheTypeFilter(GeocacheType.TRADITIONAL, GeocacheType.GIGA_EVENT, not = true)
        val expected = "type:not(2,7005)"

        assertEquals(expected, given.toString())
    }

    @Test
    fun verifyNegativeMethodGeocacheType() {
        val given = GeocacheTypeFilter(GeocacheType.GIGA_EVENT, GeocacheType.WHERIGO).not()
        val expected = "type:not(7005,1858)"

        assertEquals(expected, given.toString())
    }
}
