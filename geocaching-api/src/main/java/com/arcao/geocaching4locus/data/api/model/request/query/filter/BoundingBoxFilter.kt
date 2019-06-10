package com.arcao.geocaching4locus.data.api.model.request.query.filter

import com.arcao.geocaching4locus.data.api.model.Coordinates
import java.util.Locale

class BoundingBoxFilter(
    private val topLatitude: Double,
    private val leftLongitude: Double,
    private val bottomLatitude: Double,
    private val rightLongitude: Double
) : Filter {

    constructor(topLeftCoordinates: Coordinates, bottomRightCoordinates: Coordinates) : this(
        topLeftCoordinates.latitude,
        topLeftCoordinates.longitude,
        bottomRightCoordinates.latitude,
        bottomRightCoordinates.longitude
    )

    override fun isValid() = true

    override fun toString(): String {
        return "box:[[%.6f,%.6f],[%.6f,%.6f]]".format(
            Locale.US,
            topLatitude,
            leftLongitude,
            bottomLatitude,
            rightLongitude
        )
    }
}
