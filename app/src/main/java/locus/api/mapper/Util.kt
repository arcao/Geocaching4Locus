package locus.api.mapper

import androidx.annotation.NonNull
import locus.api.objects.geoData.Point
import locus.api.objects.geocaching.GeocachingLog

object Util {
    const val GSAK_USERNAME = "gsak"

    fun applyUnavailabilityForGeocache(@NonNull point: Point, threshold: Int) {
        var counter = 0

        val gcData = point.gcData ?: return

        // only when there is any log
        if (gcData.logs.isEmpty())
            return

        // skip analyzing already archived geocache
        if (gcData.isArchived)
            return

        // go through all logs (must be sorted by visited date, newest first)
        val geocachingLogs = gcData.logs

        loop@ for (log in geocachingLogs) {
            // skip GSAK log
            if (GSAK_USERNAME.equals(log.finder, ignoreCase = true))
                continue

            when (log.type) {
                // increase counter for DNF, NM and NA log
                GeocachingLog.CACHE_LOG_TYPE_NOT_FOUND,
                GeocachingLog.CACHE_LOG_TYPE_NEEDS_MAINTENANCE,
                GeocachingLog.CACHE_LOG_TYPE_NEEDS_ARCHIVED -> counter++

                // for other log types break the loop
                else -> break@loop
            }
        }

        // if counter contains required threshold
        if (counter >= threshold) {
            // set geocache as not available
            gcData.isAvailable = false
        }
    }
}
