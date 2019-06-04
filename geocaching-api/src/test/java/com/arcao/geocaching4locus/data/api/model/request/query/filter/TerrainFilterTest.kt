package com.arcao.geocaching4locus.data.api.model.request.query.filter

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal object TerrainFilterTest {
    @Test
    fun verifyOneToFiveTerrain() {
        val given = TerrainFilter(1F, 5F)
        val expected = "terr:1.0-5.0"

        assertEquals(expected, given.toString())
    }

    @Test
    fun verifyOneAndHalfToTwoAndHalfTerrain() {
        val given = TerrainFilter(1.5F, 2.5F)
        val expected = "terr:1.5-2.5"

        assertEquals(expected, given.toString())
    }
}