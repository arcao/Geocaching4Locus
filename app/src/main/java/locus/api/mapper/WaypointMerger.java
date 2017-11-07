package locus.api.mapper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.arcao.geocaching4locus.base.util.ReverseListIterator;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

import locus.api.objects.extra.Location;
import locus.api.objects.extra.Waypoint;
import locus.api.objects.geocaching.GeocachingLog;
import locus.api.objects.geocaching.GeocachingWaypoint;

import static locus.api.mapper.Util.GSAK_USERNAME;
import static locus.api.mapper.Util.applyUnavailabilityForGeocache;

final public class WaypointMerger {
    private final SharedPreferences preferences;

    public WaypointMerger(@NonNull Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }

    public void mergeWaypoint(@NonNull Waypoint dstWaypoint, @Nullable Waypoint srcWaypoint) {
        dstWaypoint.removeExtraOnDisplay();

        if (srcWaypoint == null || srcWaypoint.gcData == null)
            return;

        copyArchivedGeocacheLocation(dstWaypoint, srcWaypoint);
        copyGsakGeocachingLogs(dstWaypoint.gcData.logs, srcWaypoint.gcData.logs);
        copyComputedCoordinates(dstWaypoint, srcWaypoint);
        copyWaypointId(dstWaypoint, srcWaypoint);
        copyGcVote(dstWaypoint, srcWaypoint);
        copyEditedGeocachingWaypointLocation(dstWaypoint, srcWaypoint);
        applyUnavailabilityForGeocache(preferences, dstWaypoint);
    }

    public void mergeGeocachingLogs(@NonNull Waypoint dstWaypoint, @Nullable Waypoint srcWaypoint) {
        if (srcWaypoint == null || srcWaypoint.gcData == null)
            return;

        // store original logs
        List<GeocachingLog> originalLogs = new ArrayList<>(dstWaypoint.gcData.logs);

        // replace logs with new one
        dstWaypoint.gcData.logs.clear();
        dstWaypoint.gcData.logs.addAll(srcWaypoint.gcData.logs);

        // copy GSAK logs from original logs
        copyGsakGeocachingLogs(dstWaypoint.gcData.logs, originalLogs);
    }

    // issue #14: Keep cache logs from GSAK when updating cache
    private void copyGsakGeocachingLogs(@NonNull List<GeocachingLog> dstLogs, @NonNull List<GeocachingLog> srcLogs) {
        for (GeocachingLog fromLog : new ReverseListIterator<>(srcLogs)) {
            if (GSAK_USERNAME.equalsIgnoreCase(fromLog.getFinder())) {
                fromLog.setDate(System.currentTimeMillis());
                dstLogs.add(0, fromLog);
            }
        }
    }

    // issue #13: Use old coordinates when cache is archived after update
    private void copyArchivedGeocacheLocation(@NonNull Waypoint dstWaypoint, @NonNull Waypoint srcWaypoint) {
        if (srcWaypoint.gcData == null || dstWaypoint.gcData == null)
            return;

        double latitude = srcWaypoint.getLocation().getLatitude();
        double longitude = srcWaypoint.getLocation().getLongitude();

        // are valid coordinates
        if (Double.isNaN(latitude) || Double.isNaN(longitude) || (latitude == 0 && longitude == 0))
            return;

        // is new point not archived or has computed coordinates
        if (!dstWaypoint.gcData.isArchived() || srcWaypoint.gcData.isComputed())
            return;

        // store coordinates to new point
        dstWaypoint.getLocation().setLatitude(latitude);
        dstWaypoint.getLocation().setLongitude(longitude);
    }

    // Copy computed coordinates to new point
    private void copyComputedCoordinates(@NonNull Waypoint dstWaypoint, @NonNull Waypoint srcWaypoint) {
        if (srcWaypoint.gcData == null || dstWaypoint.gcData == null)
            return;

        if (!srcWaypoint.gcData.isComputed() || dstWaypoint.gcData.isComputed())
            return;

        Location location = dstWaypoint.getLocation();

        dstWaypoint.gcData.setLatOriginal(location.getLatitude());
        dstWaypoint.gcData.setLonOriginal(location.getLongitude());
        dstWaypoint.gcData.setComputed(true);

        // update coordinates to new location
        location.set(srcWaypoint.getLocation());
    }

    private void copyWaypointId(@NonNull Waypoint dstWaypoint, @NonNull Waypoint srcWaypoint) {
        if (srcWaypoint.getId() == 0)
            return;

        dstWaypoint.setId(srcWaypoint.getId());
    }

    private void copyGcVote(@NonNull Waypoint dstWaypoint, @NonNull Waypoint srcWaypoint) {
        if (srcWaypoint.gcData == null || dstWaypoint.gcData == null)
            return;

        dstWaypoint.gcData.setGcVoteAverage(srcWaypoint.gcData.getGcVoteAverage());
        dstWaypoint.gcData.setGcVoteNumOfVotes(srcWaypoint.gcData.getGcVoteNumOfVotes());
        dstWaypoint.gcData.setGcVoteUserVote(srcWaypoint.gcData.getGcVoteUserVote());
    }

    private void copyEditedGeocachingWaypointLocation(@NonNull Waypoint dstWaypoint, @NonNull Waypoint srcWaypoint) {
        if (dstWaypoint.gcData == null || CollectionUtils.isEmpty(srcWaypoint.gcData.waypoints) ||
                CollectionUtils.isEmpty(dstWaypoint.gcData.waypoints))
            return;

        // find Waypoint with zero coordinates
        for (GeocachingWaypoint waypoint : dstWaypoint.gcData.waypoints) {
            if (waypoint.getLat() == 0 && waypoint.getLon() == 0) {

                // replace with coordinates from srcWaypoint Waypoint
                for (GeocachingWaypoint fromWaypoint : srcWaypoint.gcData.waypoints) {

                    if (waypoint.getCode().equalsIgnoreCase(fromWaypoint.getCode())) {
                        waypoint.setLat(fromWaypoint.getLat());
                        waypoint.setLon(fromWaypoint.getLon());
                    }
                }

            }
        }
    }
}
