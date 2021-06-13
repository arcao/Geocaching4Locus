package locus.api.mapper

import com.arcao.geocaching4locus.settings.manager.DefaultPreferenceManager
import locus.api.mapper.Util.GSAK_USERNAME
import locus.api.mapper.Util.applyUnavailabilityForGeocache
import locus.api.objects.geoData.Point
import locus.api.objects.geocaching.GeocachingLog

class PointMerger(
    private val defaultPreferenceManager: DefaultPreferenceManager
) {
    fun mergePoints(dest: Point, src: Point?) {
        dest.removeExtraOnDisplay()

        val srcGcData = src?.gcData ?: return
        val destGcData = dest.gcData ?: return

        copyArchivedGeocacheLocation(dest, src)
        copyGsakGeocachingLogs(destGcData.logs, srcGcData.logs)
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
        val originalLogs = ArrayList(dest.gcData?.logs.orEmpty())

        // replace logs with new one
        dest.gcData?.logs?.apply {
            clear()
            src.gcData?.logs?.let(this::addAll)
        }

        // copy GSAK logs from original logs
        copyGsakGeocachingLogs(dest.gcData?.logs, originalLogs)
    }

    // issue #14: Keep cache logs from GSAK when updating cache
    private fun copyGsakGeocachingLogs(
        dest: MutableList<GeocachingLog>?,
        src: List<GeocachingLog>?
    ) {
        src ?: return
        dest ?: return

        for (fromLog in src.reversed()) {
            if (GSAK_USERNAME.equals(fromLog.finder, ignoreCase = true)) {
                fromLog.date = System.currentTimeMillis()
                dest.add(0, fromLog)
            }
        }
    }

    // issue #13: Use old coordinates when cache is archived after update
    private fun copyArchivedGeocacheLocation(dest: Point, src: Point) {
        val srcGcData = src.gcData ?: return
        val destGcData = dest.gcData ?: return

        val latitude = src.location.latitude
        val longitude = src.location.longitude

        // are valid coordinates
        if (latitude.isNaN() || longitude.isNaN() || latitude == 0.0 && longitude == 0.0)
            return

        // is new point not archived or has computed coordinates
        if (!destGcData.isArchived || srcGcData.isComputed)
            return

        // store coordinates to new point
        dest.location.apply {
            this.latitude = latitude
            this.longitude = longitude
        }
    }

    // Copy computed coordinates to new point
    private fun copyComputedCoordinates(dest: Point, src: Point) {
        val srcGcData = src.gcData ?: return
        val destGcData = dest.gcData ?: return

        if (!srcGcData.isComputed || destGcData.isComputed)
            return

        val location = dest.location

        destGcData.apply {
            latOriginal = location.latitude
            lonOriginal = location.longitude
            isComputed = true
        }

        // update coordinates to new location
        location.set(src.location)
    }

    private fun copyPointId(dest: Point, src: Point) {
        if (src.id == 0L)
            return

        dest.id = src.id
    }

    private fun copyGcVote(dest: Point, src: Point) {
        val srcGcData = src.gcData ?: return
        val destGcData = dest.gcData ?: return

        destGcData.apply {
            gcVoteAverage = srcGcData.gcVoteAverage
            gcVoteNumOfVotes = srcGcData.gcVoteNumOfVotes
            gcVoteUserVote = srcGcData.gcVoteUserVote
        }
    }

    private fun copyEditedGeocachingWaypointLocation(dest: Point, src: Point) {
        val destWaypoints = dest.gcData?.waypoints ?: return
        val srcWaypoints = src.gcData?.waypoints ?: return

        if (destWaypoints.isEmpty() || srcWaypoints.isEmpty())
            return

        // find Waypoint with zero coordinates
        for (waypoint in destWaypoints) {
            if (waypoint.lat == 0.0 && waypoint.lon == 0.0) {

                // replace with coordinates from src Waypoint
                for (fromWaypoint in srcWaypoints) {

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
