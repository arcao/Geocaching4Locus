package com.arcao.geocaching4locus.data.api.model.request.query.filter

import com.arcao.geocaching4locus.data.api.model.Coordinates
import java.util.Locale
import kotlin.math.abs

class LocationFilter(private val latitude: Double, private val longitude: Double) : Filter {
    override fun isValid() = abs(latitude) <= 90 && abs(longitude) <= 180

    constructor(coordinates: Coordinates) : this(coordinates.latitude, coordinates.longitude)

    override fun toString(): String = "loc:[%.6f,%.6f]".format(Locale.US, latitude, longitude)
}
