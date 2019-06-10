package com.arcao.geocaching4locus.data.api.model.request.query.filter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal object FavoritePointsFilterTest {
    @Test
    fun verifyZeroFavoritePoints() {
        val given = FavoritePointsFilter(0)

        assertFalse(given.isValid())
    }

    @Test
    fun verifyOneFavoritePoints() {
        val given = FavoritePointsFilter(1)
        val expected = "fav:1"

        assertTrue(given.isValid())
        assertEquals(expected, given.toString())
    }

    @Test
    fun verifyFiveFavoritePoints() {
        val given = FavoritePointsFilter(5)
        val expected = "fav:5"

        assertTrue(given.isValid())
        assertEquals(expected, given.toString())
    }
}
