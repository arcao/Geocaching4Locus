package locus.api.mapper

import android.content.Context
import android.preference.PreferenceManager
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.arcao.geocaching4locus.base.constants.PrefConstants
import locus.api.mapper.Util.GSAK_USERNAME
import locus.api.mapper.Util.applyUnavailabilityForGeocache
import locus.api.objects.extra.Waypoint
import locus.api.objects.geocaching.GeocachingLog
import java.util.*

class WaypointMerger(@NonNull context: Context) {
    private val disableDnfNmNaGeocaches: Boolean
    private val disableDnfNmNaGeocachesThreshold: Int

    init {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
        disableDnfNmNaGeocaches = preferences.getBoolean(PrefConstants.DOWNLOADING_DISABLE_DNF_NM_NA_CACHES, false)
        disableDnfNmNaGeocachesThreshold = preferences.getInt(PrefConstants.DOWNLOADING_DISABLE_DNF_NM_NA_CACHES_LOGS_COUNT, 1)
    }

    fun mergeWaypoint(@NonNull dstWaypoint: Waypoint, @Nullable srcWaypoint: Waypoint?) {
        dstWaypoint.removeExtraOnDisplay()

        if (srcWaypoint?.gcData == null)
            return

        copyArchivedGeocacheLocation(dstWaypoint, srcWaypoint)
        copyGsakGeocachingLogs(dstWaypoint.gcData.logs, srcWaypoint.gcData.logs)
        copyComputedCoordinates(dstWaypoint, srcWaypoint)
        copyWaypointId(dstWaypoint, srcWaypoint)
        copyGcVote(dstWaypoint, srcWaypoint)
        copyEditedGeocachingWaypointLocation(dstWaypoint, srcWaypoint)

        // only when this feature is enabled
        if (disableDnfNmNaGeocaches) applyUnavailabilityForGeocache(dstWaypoint, disableDnfNmNaGeocachesThreshold)
    }

    fun mergeGeocachingLogs(@NonNull dstWaypoint: Waypoint, @Nullable srcWaypoint: Waypoint?) {
        if (srcWaypoint?.gcData == null)
            return

        // store original logs
        val originalLogs = ArrayList(dstWaypoint.gcData.logs)

        // replace logs with new one
        dstWaypoint.gcData.logs.apply {
            clear()
            addAll(srcWaypoint.gcData.logs)
        }

        // copy GSAK logs from original logs
        copyGsakGeocachingLogs(dstWaypoint.gcData.logs, originalLogs)
    }

    // issue #14: Keep cache logs from GSAK when updating cache
    private fun copyGsakGeocachingLogs(@NonNull dstLogs: MutableList<GeocachingLog>, @NonNull srcLogs: List<GeocachingLog>) {
        for (fromLog in srcLogs.reversed()) {
            if (GSAK_USERNAME.equals(fromLog.finder, ignoreCase = true)) {
                fromLog.date = System.currentTimeMillis()
                dstLogs.add(0, fromLog)
            }
        }
    }

    // issue #13: Use old coordinates when cache is archived after update
    private fun copyArchivedGeocacheLocation(@NonNull dstWaypoint: Waypoint, @NonNull srcWaypoint: Waypoint) {
        if (srcWaypoint.gcData == null || dstWaypoint.gcData == null)
            return

        val latitude = srcWaypoint.location.getLatitude()
        val longitude = srcWaypoint.location.getLongitude()

        // are valid coordinates
        if (java.lang.Double.isNaN(latitude) || java.lang.Double.isNaN(longitude) || latitude == 0.0 && longitude == 0.0)
            return

        // is new point not archived or has computed coordinates
        if (!dstWaypoint.gcData.isArchived || srcWaypoint.gcData.isComputed)
            return

        // store coordinates to new point
        dstWaypoint.location.apply {
            setLatitude(latitude)
            setLongitude(longitude)
        }
    }

    // Copy computed coordinates to new point
    private fun copyComputedCoordinates(@NonNull dstWaypoint: Waypoint, @NonNull srcWaypoint: Waypoint) {
        if (srcWaypoint.gcData == null || dstWaypoint.gcData == null)
            return

        if (!srcWaypoint.gcData.isComputed || dstWaypoint.gcData.isComputed)
            return

        val location = dstWaypoint.location

        dstWaypoint.gcData.apply {
            latOriginal = location.getLatitude()
            lonOriginal = location.getLongitude()
            isComputed = true
        }

        // update coordinates to new location
        location.set(srcWaypoint.location)
    }

    private fun copyWaypointId(@NonNull dstWaypoint: Waypoint, @NonNull srcWaypoint: Waypoint) {
        if (srcWaypoint.getId() == 0L)
            return

        dstWaypoint.setId(srcWaypoint.getId())
    }

    private fun copyGcVote(@NonNull dstWaypoint: Waypoint, @NonNull srcWaypoint: Waypoint) {
        if (srcWaypoint.gcData == null || dstWaypoint.gcData == null)
            return

        dstWaypoint.gcData.apply {
            gcVoteAverage = srcWaypoint.gcData.gcVoteAverage
            gcVoteNumOfVotes = srcWaypoint.gcData.gcVoteNumOfVotes
            gcVoteUserVote = srcWaypoint.gcData.gcVoteUserVote
        }
    }

    private fun copyEditedGeocachingWaypointLocation(@NonNull dstWaypoint: Waypoint, @NonNull srcWaypoint: Waypoint) {
        if (dstWaypoint.gcData?.waypoints?.isEmpty() != false || srcWaypoint.gcData.waypoints.isEmpty())
            return

        // find Waypoint with zero coordinates
        for (waypoint in dstWaypoint.gcData.waypoints) {
            if (waypoint.lat == 0.0 && waypoint.lon == 0.0) {

                // replace with coordinates from srcWaypoint Waypoint
                for (fromWaypoint in srcWaypoint.gcData.waypoints) {

                    if (waypoint.code.equals(fromWaypoint.code, ignoreCase = true)) {
                        waypoint.apply {
                            lat = fromWaypoint.lat
                            lon = fromWaypoint.lon
                        }
                    }
                }

            }
        }
    }
}
