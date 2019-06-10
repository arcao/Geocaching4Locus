package com.arcao.geocaching4locus.data.api.model.request.query

import com.arcao.geocaching4locus.data.api.model.request.query.filter.Filter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal object GeocacheQueryTest {
    @Test
    fun verifyOneFilter() {
        val filter = createFilterStub("filter1")

        val expected = "filter1"
        assertEquals(expected, queryOf(filter).toString())
    }

    @Test
    fun verifyTwoFilters() {
        val filter1 = createFilterStub("filter1")
        val filter2 = createFilterStub("filter2")

        val expected = "filter1+filter2"

        assertEquals(expected, queryOf(filter1, filter2).toString())
    }

    @Test
    fun verifyValidFilters() {
        val filter1 = createFilterStub("filter1")
        val filter2 = createFilterStub("filter2", valid = false)
        val filter3 = createFilterStub("filter3")

        val expected = "filter1+filter3"

        assertEquals(expected, queryOf(filter1, filter2, filter3).toString())
    }

    private fun createFilterStub(toStringValue: String, valid: Boolean = true) = object : Filter {
        override fun isValid() = valid
        override fun toString() = toStringValue
    }
}
