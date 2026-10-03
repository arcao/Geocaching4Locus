package com.arcao.geocaching4locus.data.api.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class UserTest {
    private fun user(referenceCode: String?) = User(
        referenceCode = referenceCode,
        username = "user",
        avatarUrl = null,
        bannerUrl = null,
        profileText = null,
        url = null,
        homeCoordinates = null,
        geocacheLimits = null
    )

    @Test
    fun `hidden user has no id`() {
        val user = user("PRHIDDEN")

        assertTrue(user.isHidden)
        assertNull(user.idOrNull)
    }

    @Test
    fun `valid reference code is converted to id`() {
        val user = user("PR1")

        assertFalse(user.isHidden)
        assertEquals(1L, user.idOrNull)
    }

    @Test
    fun `missing or invalid reference code has no id`() {
        assertNull(user(null).idOrNull)
        assertNull(user("PRHIDDEN2").idOrNull)
    }
}
