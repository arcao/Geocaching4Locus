package locus.api.mapper;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.arcao.geocaching4locus.base.constants.PrefConstants;

import java.util.Date;
import java.util.List;

import locus.api.objects.extra.Waypoint;
import locus.api.objects.geocaching.GeocachingLog;
import timber.log.Timber;

final public class Util {
    static final String GSAK_USERNAME = "gsak";

    private Util() {
    }

    static long safeDateLong(@Nullable Date date) {
        return date != null ? date.getTime() : 0;
    }

    public static void applyUnavailabilityForGeocache(SharedPreferences preferences, @NonNull Waypoint toPoint) {
        int counter = 0;

        // only when this feature is enabled
        if (!preferences.getBoolean(PrefConstants.DOWNLOADING_DISABLE_DNF_NM_NA_CACHES, false))
            return;

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
            Timber.d("Analyzing log with date: %d", log.getDate());

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

        // if counter contains required count
        if (counter >= preferences.getInt(PrefConstants.DOWNLOADING_DISABLE_DNF_NM_NA_CACHES_LOGS_COUNT, 1)) {
            // set geocache as not available
            toPoint.gcData.setAvailable(false);
        }
    }
}
