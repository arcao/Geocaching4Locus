package locus.api.mapper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Date;
import java.util.List;

import locus.api.objects.extra.Waypoint;
import locus.api.objects.geocaching.GeocachingLog;

final public class Util {
    static final String GSAK_USERNAME = "gsak";

    private Util() {
    }

    static long safeDateLong(@Nullable Date date) {
        return date != null ? date.getTime() : 0;
    }

    public static void applyUnavailabilityForGeocache(@NonNull Waypoint toPoint, int threshold) {
        int counter = 0;

        // only when there is any log
        if (toPoint.gcData == null || toPoint.gcData.logs == null || toPoint.gcData.logs.isEmpty())
            return;

        // skip analyzing already archived geocache
        if (toPoint.gcData.isArchived())
            return;

        // go through all logs (must be sorted by visited date, newest first)
        List<GeocachingLog> geocachingLogs = toPoint.gcData.logs;

        loop:
        for (GeocachingLog log : geocachingLogs) {
            // skip GSAK log
            if (GSAK_USERNAME.equalsIgnoreCase(log.getFinder()))
                continue;

            // increase counter for DNF, NM and NA log
            switch (log.getType()) {
                case GeocachingLog.CACHE_LOG_TYPE_NOT_FOUND:
                case GeocachingLog.CACHE_LOG_TYPE_NEEDS_MAINTENANCE:
                case GeocachingLog.CACHE_LOG_TYPE_NEEDS_ARCHIVED:
                    counter++;
                    break;

                default:
                    // for other log types break the loop
                    break loop;
            }
        }

        // if counter contains required threshold
        if (counter >= threshold) {
            // set geocache as not available
            toPoint.gcData.setAvailable(false);
        }
    }
}
