package com.arcao.geocaching4locus.live_map.model

import android.content.Intent
import com.arcao.geocaching.api.data.coordinates.Coordinates
import locus.api.android.utils.LocusUtils

class LastLiveMapCoordinates private constructor(
    val center: Coordinates,
    val topLeft: Coordinates,
    val bottomRight: Coordinates
) {
    companion object {
        private val VAR_LOC_MAP_CENTER = "1302"
        private val VAR_LOC_MAP_BBOX_TOP_LEFT = "1303"
        private val VAR_LOC_MAP_BBOX_BOTTOM_RIGHT = "1304"

        var value : LastLiveMapCoordinates? = null
            private set

        @JvmStatic
        fun update(intent: Intent) {
            val mapCenterCoordinates = getCoordinatesFromIntent(intent, VAR_LOC_MAP_CENTER)
            val mapTopLeftCoordinates = getCoordinatesFromIntent(intent, VAR_LOC_MAP_BBOX_TOP_LEFT)
            val mapBottomRightCoordinates = getCoordinatesFromIntent(intent, VAR_LOC_MAP_BBOX_BOTTOM_RIGHT)

            if (mapCenterCoordinates != null && mapTopLeftCoordinates != null && mapBottomRightCoordinates != null) {
                value = LastLiveMapCoordinates(mapCenterCoordinates, mapTopLeftCoordinates, mapBottomRightCoordinates)
            } else {
                value = null
            }
        }

        @JvmStatic
        fun remove() {
            value = null
        }

        private fun getCoordinatesFromIntent(intent: Intent, extraName: String): Coordinates? {
            val location = LocusUtils.getLocationFromIntent(intent, extraName)

            return if (location == null || location.latitude.isNaN() || location.longitude.isNaN())
                null
            else
                Coordinates.create(location.latitude, location.longitude)
        }
    }
}
