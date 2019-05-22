package com.arcao.geocaching4locus.live_map.model

import com.arcao.geocaching4locus.data.api.model.Coordinates
import locus.api.objects.extra.Location

class LastLiveMapCoordinates private constructor(
    val center: Coordinates,
    val topLeft: Coordinates,
    val bottomRight: Coordinates
) {
    companion object {
        var value: LastLiveMapCoordinates? = null
            private set

        fun update(mapCenter: Location, mapTopLeft: Location, mapBottomRight: Location) {
            value = LastLiveMapCoordinates(
                mapCenter.toCoordinates(),
                mapTopLeft.toCoordinates(),
                mapBottomRight.toCoordinates()
            )
        }

        fun remove() {
            value = null
        }

        private fun Location.toCoordinates() = Coordinates(latitude, longitude)
    }
}
