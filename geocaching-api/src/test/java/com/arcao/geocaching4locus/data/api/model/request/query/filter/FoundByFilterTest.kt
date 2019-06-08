package com.arcao.geocaching4locus.data.api.model.request.query.filter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal object FoundByFilterTest {
    @Test
    fun verifyPositiveFoundBy() {
        val given = FoundByFilter("john")
        val expected = "fby:john"

        assertEquals(expected, given.toString())
    }

    @Test
    fun verifyNegativeConstructorFoundBy() {
        val given = FoundByFilter("jane", not = true)
        val expected = "fby:not(jane)"

        assertEquals(expected, given.toString())
    }

    @Test
    fun verifyNegativeMethodFoundBy() {
        val given = FoundByFilter("lisa").not()
        val expected = "fby:not(lisa)"

        assertEquals(expected, given.toString())
    }
}
