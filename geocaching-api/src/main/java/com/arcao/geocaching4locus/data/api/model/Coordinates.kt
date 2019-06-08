package com.arcao.geocaching4locus.data.api.model

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Coordinates(
    val latitude: Double, // 0
    val longitude: Double // 0
) {
    fun distanceTo(another: Coordinates): Long {
        val result = DoubleArray(1)
        computeDistanceAndBearing(this, another, result)
        return result[0].toLong()
    }

    companion object {
        private const val AVERAGE_RADIUS_OF_EARTH = 6372797.560856

        /**
         * Computes the approximate distance in meters between two coordinates, and
         * optionally the initial and final bearings bearings of the shortest path between
         * them.
         *
         * The computed distance is stored in `results[0]`. If results has length 2,
         * the initial bearing is stored in `results[1]`. If results has
         * length 3, the final bearing is stored in `results[2]`.
         *
         * Compute is based on [Haversine formula](http://en.wikipedia.org/wiki/Haversine_formula).
         * Precision is around 99.9%.
         *
         * @param source source coordinates
         * @param destination destination coordinates
         * @param results array where first index is distance in meters, second index is initial bearing in degree
         * and third index is final bearing in degree
         * @since 1.20
         */
        private fun computeDistanceAndBearing(source: Coordinates, destination: Coordinates, results: DoubleArray) {
            if (results.isEmpty() || results.size > 3) {
                throw IllegalArgumentException("Results has to be initialized array of size 1, 2 or 3.")
            }

            val lat1 = Math.toRadians(source.latitude)
            val lon1 = Math.toRadians(source.longitude)
            val lat2 = Math.toRadians(destination.latitude)
            val lon2 = Math.toRadians(destination.longitude)

            // prepare variables
            val cosLat1 = cos(lat1)
            val cosLat2 = cos(lat2)
            val sinDLat2 = sin((lat2 - lat1) / 2.0)
            val sinDLon2 = sin((lon2 - lon1) / 2.0)

            // compute values
            val a = sinDLat2 * sinDLat2 + cosLat1 * cosLat2 * sinDLon2 * sinDLon2
            val d = 2.0 * atan2(sqrt(a), sqrt(1.0 - a))

            // convert to metres
            results[0] = d * AVERAGE_RADIUS_OF_EARTH

            // compute initial bearing
            if (results.size > 1) {
                val sinLambda = sin(lon2 - lon1)
                val cosLambda = cos(lon2 - lon1)

                var y = sinLambda * cosLat2
                var x = cosLat1 * sin(lat2) - sin(lat1) * cosLat2 * cosLambda
                results[1] = Math.toDegrees(atan2(y, x))

                // compute final bearing
                if (results.size > 2) {
                    y = sinLambda * cosLat1
                    x = -sin(lat1) * cosLat2 + cosLat1 * sin(lat2) * cosLambda
                    results[2] = Math.toDegrees(atan2(y, x))
                }
            }
        }
    }
}
