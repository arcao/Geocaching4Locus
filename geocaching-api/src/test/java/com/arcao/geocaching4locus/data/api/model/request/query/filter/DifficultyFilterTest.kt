package com.arcao.geocaching4locus.data.api.model.request.query.filter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal object DifficultyFilterTest {
    @Test
    fun verifyOneToFiveDifficulty() {
        val given = DifficultyFilter(1F, 5F)
        val expected = "diff:1.0-5.0"

        assertEquals(expected, given.toString())
    }

    @Test
    fun verifyOneAndHalfToTwoAndHalfDifficulty() {
        val given = DifficultyFilter(1.5F, 2.5F)
        val expected = "diff:1.5-2.5"

        assertEquals(expected, given.toString())
    }
}
