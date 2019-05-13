package com.arcao.geocaching4locus.data.api.model.request.query.filter

import com.arcao.geocaching4locus.data.api.model.Coordinates

class BoundingBoxFilter(
        private val topLatitude : Double,
        private val rightLongitude : Double,
        private val bottomLatitude : Double,
        private val leftLongitude : Double
) : Filter {

    constructor(topLeftCoordinates: Coordinates, bottomRightCoordinates: Coordinates): this(
            topLeftCoordinates.latitude,
            bottomRightCoordinates.longitude,
            bottomRightCoordinates.latitude,
            topLeftCoordinates.longitude
    )

    override fun isValid() = true

    override fun toString(): String {
        return "box:[[$topLatitude,$rightLongitude],[$bottomLatitude,$leftLongitude]]"
    }
}