package locus.api.mapper

import androidx.annotation.NonNull
import locus.api.objects.extra.Waypoint
import locus.api.objects.geocaching.GeocachingLog

object Util {
    const val GSAK_USERNAME = "gsak"

    @JvmStatic
    fun applyUnavailabilityForGeocache(@NonNull toPoint: Waypoint, threshold: Int) {
        var counter = 0

        // only when there is any log
        if (toPoint.gcData?.logs?.isEmpty() != false)
            return

        // skip analyzing already archived geocache
        if (toPoint.gcData.isArchived)
            return

        // go through all logs (must be sorted by visited date, newest first)
        val geocachingLogs = toPoint.gcData.logs

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
            toPoint.gcData.isAvailable = false
        }
    }
}
