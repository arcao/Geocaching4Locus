package locus.api.mapper

import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.arcao.geocaching.api.data.Waypoint
import com.arcao.geocaching.api.data.type.WaypointType
import locus.api.objects.extra.Point
import locus.api.objects.geocaching.GeocachingWaypoint
import locus.api.utils.addIgnoreNull
import locus.api.utils.isNullOrEmpty

class WaypointConverter {
    fun addWaypoints(@NonNull point: Point, @Nullable waypoints: Collection<Waypoint?>?) {
        if (point.gcData == null || waypoints.isNullOrEmpty())
            return

        for (waypoint in waypoints!!) {
            point.gcData.waypoints.addIgnoreNull(createLocusGeocachingWaypoint(waypoint))
        }
    }

    @Nullable
    private fun createLocusGeocachingWaypoint(@Nullable waypoint: Waypoint?): GeocachingWaypoint? {
        if (waypoint == null)
            return null

        return GeocachingWaypoint().apply {
            code = waypoint.waypointCode()
            lat = waypoint.coordinates().latitude()
            lon = waypoint.coordinates().longitude()
            desc = waypoint.note()
            name = waypoint.name()
            typeImagePath = waypoint.iconName()
            type = createLocusWaypointType(waypoint.waypointType())
        }
    }

    @NonNull
    private fun createLocusWaypointType(@Nullable waypointType: WaypointType?): String {
        return when (waypointType ?: WaypointType.ReferencePoint) {
            WaypointType.FinalLocation -> GeocachingWaypoint.CACHE_WAYPOINT_TYPE_FINAL
            WaypointType.ParkingArea -> GeocachingWaypoint.CACHE_WAYPOINT_TYPE_PARKING
            WaypointType.VirtualStage -> GeocachingWaypoint.CACHE_WAYPOINT_TYPE_VIRTUAL_STAGE
            WaypointType.ReferencePoint -> GeocachingWaypoint.CACHE_WAYPOINT_TYPE_REFERENCE
            WaypointType.PhysicalStage -> GeocachingWaypoint.CACHE_WAYPOINT_TYPE_PHYSICAL_STAGE
            WaypointType.Trailhead -> GeocachingWaypoint.CACHE_WAYPOINT_TYPE_TRAILHEAD
            else -> GeocachingWaypoint.CACHE_WAYPOINT_TYPE_REFERENCE
        }
    }
}
