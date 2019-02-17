package locus.api.mapper

import android.content.Context
import com.arcao.geocaching4locus.settings.manager.DefaultPreferenceManager
import locus.api.mapper.Util.GSAK_USERNAME
import locus.api.mapper.Util.applyUnavailabilityForGeocache
import locus.api.objects.extra.Point
import locus.api.objects.geocaching.GeocachingLog

class PointMerger(
    private val defaultPreferenceManager: DefaultPreferenceManager
) {

    @Deprecated("Use koin.")
    constructor(context: Context) : this(DefaultPreferenceManager(context))

    fun mergePoints(dest: Point, src: Point?) {
        dest.removeExtraOnDisplay()

        if (src?.gcData == null)
            return

        copyArchivedGeocacheLocation(dest, src)
        copyGsakGeocachingLogs(dest.gcData.logs, src.gcData.logs)
        copyComputedCoordinates(dest, src)
        copyPointId(dest, src)
        copyGcVote(dest, src)
        copyEditedGeocachingWaypointLocation(dest, src)

        // only when this feature is enabled
        if (defaultPreferenceManager.disableDnfNmNaGeocaches) {
            applyUnavailabilityForGeocache(dest, defaultPreferenceManager.disableDnfNmNaGeocachesThreshold)
        }
    }

    fun mergeGeocachingLogs(dest: Point?, src: Point?) {
        if (dest == null || src?.gcData == null)
            return

        // store original logs
        val originalLogs = ArrayList(dest.gcData.logs)

        // replace logs with new one
        dest.gcData.logs.apply {
            clear()
            addAll(src.gcData.logs)
        }

        // copy GSAK logs from original logs
        copyGsakGeocachingLogs(dest.gcData.logs, originalLogs)
    }

    // issue #14: Keep cache logs from GSAK when updating cache
    private fun copyGsakGeocachingLogs(dest: MutableList<GeocachingLog>, src: List<GeocachingLog>) {
        for (fromLog in src.reversed()) {
            if (GSAK_USERNAME.equals(fromLog.finder, ignoreCase = true)) {
                fromLog.date = System.currentTimeMillis()
                dest.add(0, fromLog)
            }
        }
    }

    // issue #13: Use old coordinates when cache is archived after update
    private fun copyArchivedGeocacheLocation(dest: Point, src: Point) {
        if (src.gcData == null || dest.gcData == null)
            return

        val latitude = src.location.getLatitude()
        val longitude = src.location.getLongitude()

        // are valid coordinates
        if (java.lang.Double.isNaN(latitude) || java.lang.Double.isNaN(longitude) || latitude == 0.0 && longitude == 0.0)
            return

        // is new point not archived or has computed coordinates
        if (!dest.gcData.isArchived || src.gcData.isComputed)
            return

        // store coordinates to new point
        dest.location.apply {
            setLatitude(latitude)
            setLongitude(longitude)
        }
    }

    // Copy computed coordinates to new point
    private fun copyComputedCoordinates(dest: Point, src: Point) {
        if (src.gcData == null || dest.gcData == null)
            return

        if (!src.gcData.isComputed || dest.gcData.isComputed)
            return

        val location = dest.location

        dest.gcData.apply {
            latOriginal = location.getLatitude()
            lonOriginal = location.getLongitude()
            isComputed = true
        }

        // update coordinates to new location
        location.set(src.location)
    }

    private fun copyPointId(dest: Point, src: Point) {
        if (src.getId() == 0L)
            return

        dest.setId(src.getId())
    }

    private fun copyGcVote(dest: Point, src: Point) {
        if (src.gcData == null || dest.gcData == null)
            return

        dest.gcData.apply {
            gcVoteAverage = src.gcData.gcVoteAverage
            gcVoteNumOfVotes = src.gcData.gcVoteNumOfVotes
            gcVoteUserVote = src.gcData.gcVoteUserVote
        }
    }

    private fun copyEditedGeocachingWaypointLocation(dest: Point, src: Point) {
        if (dest.gcData?.waypoints?.isEmpty() != false || src.gcData.waypoints.isEmpty())
            return

        // find Waypoint with zero coordinates
        for (waypoint in dest.gcData.waypoints) {
            if (waypoint.lat == 0.0 && waypoint.lon == 0.0) {

                // replace with coordinates from src Waypoint
                for (fromWaypoint in src.gcData.waypoints) {

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
