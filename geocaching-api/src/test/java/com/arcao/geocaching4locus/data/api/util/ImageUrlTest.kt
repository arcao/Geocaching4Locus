package com.arcao.geocaching4locus.data.api.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal object ImageUrlTest {
    @Test
    fun verifyOriginalImageIsConvertedToLarge() {
        assertEquals(
            "https://img.geocaching.com/large/7663748f-ed3a-4206-a6c4-ea7fa8423f1a.jpg",
            ImageUrl.toLarge("https://img.geocaching.com/7663748f-ed3a-4206-a6c4-ea7fa8423f1a.jpg")
        )
    }

    @Test
    fun verifyHttpImageIsConvertedToLarge() {
        assertEquals(
            "http://img.geocaching.com/large/abc.png",
            ImageUrl.toLarge("http://img.geocaching.com/abc.png")
        )
    }

    @Test
    fun verifyWhitespaceIsTrimmed() {
        assertEquals(
            "https://img.geocaching.com/large/abc.jpg",
            ImageUrl.toLarge("  https://img.geocaching.com/abc.jpg \n")
        )
    }

    @Test
    fun verifyLargeImageIsNotChanged() {
        val url = "https://img.geocaching.com/large/abc.jpg"
        assertEquals(url, ImageUrl.toLarge(url))
    }

    @Test
    fun verifyOtherImageVariantsAreNotChanged() {
        val url = "https://img.geocaching.com/cache/large/abc.jpg"
        assertEquals(url, ImageUrl.toLarge(url))
    }

    @Test
    fun verifyForeignServerIsNotChanged() {
        val url = "https://example.com/photo.jpg"
        assertEquals(url, ImageUrl.toLarge(url))
    }
}
