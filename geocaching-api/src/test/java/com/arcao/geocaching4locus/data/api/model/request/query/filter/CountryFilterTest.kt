package com.arcao.geocaching4locus.data.api.model.request.query.filter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

object CountryFilterTest {
    @Test
    fun verifyCountry() {
        val given = CountryFilter("abcd")
        val expected = "co:abcd"

        assertTrue(given.isValid())
        assertEquals(expected, given.toString())
    }
}
