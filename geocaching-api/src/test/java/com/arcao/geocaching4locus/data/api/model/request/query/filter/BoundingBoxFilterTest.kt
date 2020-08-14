package com.arcao.geocaching4locus.data.api.model.request.query.filter

import com.arcao.geocaching4locus.data.api.model.Coordinates
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal object BoundingBoxFilterTest {
    @Test
    fun verifyPositiveBoundingBox() {
        val given = BoundingBoxFilter(50.0, 14.0, 25.0, 7.0)
        val expected = "box:[[50.000000,14.000000],[25.000000,7.000000]]"

        assertEquals(expected, given.toString())
    }

    @Test
    fun verifyPositiveCoordinatesBoundingBox() {
        val topLeftCoordinates = Coordinates(50.0, 7.0)
        val bottomRightCoordinates = Coordinates(25.0, 14.0)

        val given = BoundingBoxFilter(topLeftCoordinates, bottomRightCoordinates)
        val expected = "box:[[50.000000,7.000000],[25.000000,14.000000]]"

        assertEquals(expected, given.toString())
    }

    @Test
    fun verifyNegativeBoundingBox() {
        val given = BoundingBoxFilter(-25.0, -7.0, -50.0, -14.0)
        val expected = "box:[[-25.000000,-7.000000],[-50.000000,-14.000000]]"

        assertEquals(expected, given.toString())
    }

    @Test
    fun verifyNegativeCoordinatesBoundingBox() {
        val topLeftCoordinates = Coordinates(-25.0, -14.0)
        val bottomRightCoordinates = Coordinates(-50.0, -7.0)

        val given = BoundingBoxFilter(topLeftCoordinates, bottomRightCoordinates)
        val expected = "box:[[-25.000000,-14.000000],[-50.000000,-7.000000]]"

        assertEquals(expected, given.toString())
    }
}
