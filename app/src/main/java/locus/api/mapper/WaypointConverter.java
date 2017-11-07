package locus.api.mapper;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.arcao.geocaching.api.data.Waypoint;
import com.arcao.geocaching.api.data.type.WaypointType;

import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;

import locus.api.objects.geocaching.GeocachingWaypoint;

final class WaypointConverter {
    void addWaypoints(@NonNull locus.api.objects.extra.Waypoint toPoint, @Nullable Collection<Waypoint> waypoints) {
        if (toPoint.gcData == null || CollectionUtils.isEmpty(waypoints))
            return;

        for (Waypoint waypoint : waypoints) {
            CollectionUtils.addIgnoreNull(toPoint.gcData.waypoints, createLocusGeocachingWaypoint(waypoint));
        }
    }

    @Nullable
    private GeocachingWaypoint createLocusGeocachingWaypoint(@Nullable Waypoint waypoint) {
        if (waypoint == null)
            return null;

        GeocachingWaypoint w = new GeocachingWaypoint();
        w.setCode(waypoint.waypointCode());
        w.setLat(waypoint.coordinates().latitude());
        w.setLon(waypoint.coordinates().longitude());
        w.setDesc(waypoint.note());
        w.setName(waypoint.name());
        w.setTypeImagePath(waypoint.iconName());
        w.setType(createLocusWaypointType(waypoint.waypointType()));
        return w;
    }

    @NonNull
    private String createLocusWaypointType(@Nullable WaypointType waypointType) {
        if (waypointType == null)
            return GeocachingWaypoint.CACHE_WAYPOINT_TYPE_REFERENCE;

        switch (waypointType) {
            case FinalLocation:
                return GeocachingWaypoint.CACHE_WAYPOINT_TYPE_FINAL;
            case ParkingArea:
                return GeocachingWaypoint.CACHE_WAYPOINT_TYPE_PARKING;
            case VirtualStage:
                return GeocachingWaypoint.CACHE_WAYPOINT_TYPE_VIRTUAL_STAGE;
            case ReferencePoint:
                return GeocachingWaypoint.CACHE_WAYPOINT_TYPE_REFERENCE;
            case PhysicalStage:
                return GeocachingWaypoint.CACHE_WAYPOINT_TYPE_PHYSICAL_STAGE;
            case Trailhead:
                return GeocachingWaypoint.CACHE_WAYPOINT_TYPE_TRAILHEAD;
            default:
                return GeocachingWaypoint.CACHE_WAYPOINT_TYPE_REFERENCE;
        }
    }
}
