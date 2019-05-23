package locus.api.mapper

import com.arcao.geocaching4locus.data.api.model.AdditionalWaypoint
import com.arcao.geocaching4locus.data.api.model.enum.AdditionalWaypointType
import com.arcao.geocaching4locus.data.api.util.ReferenceCode
import locus.api.objects.extra.Point
import locus.api.objects.geocaching.GeocachingWaypoint
import locus.api.utils.addIgnoreNull
import locus.api.utils.isNullOrEmpty

class WaypointConverter {
    fun addWaypoints(point: Point, waypoints: Collection<AdditionalWaypoint?>?, geocacheId: Long) {
        if (point.gcData?.waypoints == null || waypoints.isNullOrEmpty())
            return

        for (waypoint in waypoints) {
            point.gcData.waypoints.addIgnoreNull(createLocusGeocachingWaypoint(waypoint, geocacheId))
        }
    }

    private fun createLocusGeocachingWaypoint(waypoint: AdditionalWaypoint?, geocacheId: Long): GeocachingWaypoint? {
        if (waypoint == null)
            return null

        return GeocachingWaypoint().apply {
            code = ReferenceCode.toReferenceCode(waypoint.prefix, geocacheId)
            lat = waypoint.coordinates?.latitude ?: 0.0
            lon = waypoint.coordinates?.longitude ?: 0.0
            desc = waypoint.description
            name = waypoint.name
            type = waypoint.type.toLocusMapWaypointType()
        }
    }

    private fun AdditionalWaypointType?.toLocusMapWaypointType() : String {
        return when (this ?: AdditionalWaypointType.REFERENCE_POINT) {
            AdditionalWaypointType.FINAL_LOCATION -> GeocachingWaypoint.CACHE_WAYPOINT_TYPE_FINAL
            AdditionalWaypointType.PARKING_AREA -> GeocachingWaypoint.CACHE_WAYPOINT_TYPE_PARKING
            AdditionalWaypointType.VIRTUAL_STAGE -> GeocachingWaypoint.CACHE_WAYPOINT_TYPE_VIRTUAL_STAGE
            AdditionalWaypointType.REFERENCE_POINT -> GeocachingWaypoint.CACHE_WAYPOINT_TYPE_REFERENCE
            AdditionalWaypointType.PHYSICAL_STAGE -> GeocachingWaypoint.CACHE_WAYPOINT_TYPE_PHYSICAL_STAGE
            AdditionalWaypointType.TRAILHEAD -> GeocachingWaypoint.CACHE_WAYPOINT_TYPE_TRAILHEAD
        }
    }
}
