package locus.api.mapper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.base.util.ReverseListIterator;
import locus.api.objects.extra.Location;
import locus.api.objects.extra.Waypoint;
import locus.api.objects.geocaching.GeocachingLog;
import locus.api.objects.geocaching.GeocachingWaypoint;
import org.apache.commons.collections4.CollectionUtils;

import static locus.api.mapper.Util.GSAK_USERNAME;
import static locus.api.mapper.Util.applyUnavailabilityForGeocache;
import static locus.api.mapper.Util.getWaypointByNamePrefix;

final public class WaypointMerger {
	private static final String ORIGINAL_COORDINATES_WAYPOINT_PREFIX = "RX";

	private final Context mContext;
	private final SharedPreferences mPrefs;

	public WaypointMerger(@NonNull Context context) {
		mContext = context.getApplicationContext();
		mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
	}

	public void mergeWaypoint(@NonNull Waypoint dstWaypoint, @Nullable Waypoint srcWaypoint) {
		dstWaypoint.removeExtraOnDisplay();

		if (srcWaypoint == null || srcWaypoint.gcData == null)
			return;

		copyArchivedGeocacheLocation(dstWaypoint, srcWaypoint);
		copyGsakGeocachingLogs(dstWaypoint, srcWaypoint);
		copyComputedCoordinates(dstWaypoint, srcWaypoint);
		copyWaypointId(dstWaypoint, srcWaypoint);
		copyGcVote(dstWaypoint, srcWaypoint);
		copyEditedGeocachingWaypointLocation(dstWaypoint, srcWaypoint);
		applyUnavailabilityForGeocache(mPrefs, dstWaypoint);
	}

	public void mergeGeocachingLogs(@NonNull Waypoint dstWaypoint, @Nullable Waypoint srcWaypoint) {
		if (srcWaypoint == null || srcWaypoint.gcData == null)
			return;

		copyGsakGeocachingLogs(dstWaypoint, srcWaypoint);
	}

	// issue #14: Keep cache logs from GSAK when updating cache
	private void copyGsakGeocachingLogs(@NonNull Waypoint dstWaypoint, @NonNull Waypoint srcWaypoint) {
		if (srcWaypoint.gcData == null || CollectionUtils.isEmpty(srcWaypoint.gcData.logs))
			return;

		for(GeocachingLog fromLog : new ReverseListIterator<>(srcWaypoint.gcData.logs)) {
			if (GSAK_USERNAME.equalsIgnoreCase(fromLog.getFinder())) {
				fromLog.setDate(System.currentTimeMillis());
				dstWaypoint.gcData.logs.add(0, fromLog);
			}
		}
	}

	// issue #13: Use old coordinates when cache is archived after update
	private void copyArchivedGeocacheLocation(@NonNull Waypoint dstWaypoint, @NonNull Waypoint srcWaypoint) {
		if (srcWaypoint.gcData == null || dstWaypoint.gcData == null)
			return;

		double latitude = srcWaypoint.getLocation().getLatitude();
		double longitude  = srcWaypoint.getLocation().getLongitude();

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

		// store original location to waypoint
		GeocachingWaypoint
				waypoint = getWaypointByNamePrefix(dstWaypoint, ORIGINAL_COORDINATES_WAYPOINT_PREFIX);
		if (waypoint == null) {
			waypoint = new GeocachingWaypoint();
			dstWaypoint.gcData.waypoints.add(waypoint);
		}

		waypoint.setCode(ORIGINAL_COORDINATES_WAYPOINT_PREFIX + dstWaypoint.gcData.getCacheID().substring(2));
		waypoint.setType(GeocachingWaypoint.CACHE_WAYPOINT_TYPE_REFERENCE);
		waypoint.setName(mContext.getString(R.string.var_original_coordinates_name));
		waypoint.setLat(location.getLatitude());
		waypoint.setLon(location.getLongitude());

		// update coordinates to new location
		location.set(srcWaypoint.getLocation());
	}

	private void copyWaypointId(@NonNull Waypoint dstWaypoint, @NonNull Waypoint srcWaypoint) {
		if (srcWaypoint.id == 0)
			return;

		dstWaypoint.id = srcWaypoint.id;
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
