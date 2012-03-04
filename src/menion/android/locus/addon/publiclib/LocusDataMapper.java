package menion.android.locus.addon.publiclib;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import menion.android.locus.addon.publiclib.geoData.Point;
import menion.android.locus.addon.publiclib.geoData.PointGeocachingAttributes;
import menion.android.locus.addon.publiclib.geoData.PointGeocachingData;
import menion.android.locus.addon.publiclib.geoData.PointGeocachingDataLog;
import menion.android.locus.addon.publiclib.geoData.PointGeocachingDataTravelBug;
import menion.android.locus.addon.publiclib.geoData.PointGeocachingDataWaypoint;
import android.location.Location;

import com.arcao.geocaching.api.data.CacheLog;
import com.arcao.geocaching.api.data.Geocache;
import com.arcao.geocaching.api.data.SimpleGeocache;
import com.arcao.geocaching.api.data.Trackable;
import com.arcao.geocaching.api.data.User;
import com.arcao.geocaching.api.data.UserWaypoint;
import com.arcao.geocaching.api.data.Waypoint;
import com.arcao.geocaching.api.data.type.AttributeType;
import com.arcao.geocaching.api.data.type.CacheLogType;
import com.arcao.geocaching.api.data.type.CacheType;
import com.arcao.geocaching.api.data.type.ContainerType;
import com.arcao.geocaching.api.data.type.WaypointType;

public class LocusDataMapper {
	protected static final DateFormat GPX_TIME_FMT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	protected static final String TRACKABLE_URL = "http://www.geocaching.com/track/details.aspx?tracker=%s";

	static {
		GPX_TIME_FMT.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
	}
	
	public static Point toLocusPoint(SimpleGeocache cache) {
		Location loc = new Location(cache.getClass().getName());
		loc.setLatitude(cache.getLatitude());
		loc.setLongitude(cache.getLongitude());

		Point p = new Point(cache.getName(), loc);

		PointGeocachingData d = new PointGeocachingData();
		d.cacheID = cache.getCacheCode();
		d.name = cache.getName();
		d.type = toLocusCacheType(cache.getCacheType());
		d.difficulty = cache.getDifficultyRating();
		d.terrain = cache.getTerrainRating();
		if (cache.getAuthor() != null) {
			d.owner = cache.getAuthor().getUserName();
		}
		d.placedBy = cache.getContactName();
		d.available = cache.isAvailable();
		d.archived = cache.isArchived();
		d.premiumOnly = cache.isPremiumListing();
		d.hidden = GPX_TIME_FMT.format(cache.getCreated());
		d.exported = GPX_TIME_FMT.format(new Date());
		d.container = toLocusContainerType(cache.getContainerType());
		d.found = cache.isFound();

		if (cache instanceof Geocache) {
			Geocache gc = (Geocache) cache;

			d.country = gc.getCountryName();
			d.state = gc.getStateName();

			d.shortDescription = gc.getShortDescription();
			d.longDescription = gc.getLongDescription();
			d.encodedHints = gc.getHint();

			for (CacheLog log : gc.getCacheLogs()) {
				d.logs.add(toLocusCacheLog(log));
			}

			for (Trackable trackable : gc.getTrackables()) {
				d.travelBugs.add(toLocusTrackable(trackable));
			}

			for (Waypoint waypoint : gc.getWaypoints()) {
				d.waypoints.add(toLocusWaypoint(waypoint));
			}

			for (AttributeType attribute : gc.getAttributes()) {
				d.attributes.add(new PointGeocachingAttributes(attribute.getId(), attribute.isOn()));
			}

			int index = 0;
			for (UserWaypoint userWaypoint : gc.getUserWaypoints()) {
				PointGeocachingDataWaypoint w = new PointGeocachingDataWaypoint();

				w.type = toLocusWaypointType(WaypointType.FinalLocation);
				w.typeImagePath = WaypointType.FinalLocation.getIconName();
				w.lat = userWaypoint.getLatitude();
				w.lon = userWaypoint.getLongitude();
				w.name = String.format("%s %d", WaypointType.FinalLocation.getFriendlyName(), index + 1);
				w.description = userWaypoint.getDescription();
				w.code = userWaypoint.getUserWaypointCode(index);
				d.waypoints.add(w);

				index++;
			}
		}
		
		p.setGeocachingData(d);

		return p;
	}

	protected static PointGeocachingDataWaypoint toLocusWaypoint(Waypoint waypoint) {
		PointGeocachingDataWaypoint w = new PointGeocachingDataWaypoint();
		
		w.code = waypoint.getWaypointCode();
		w.lat = waypoint.getLatitude();
		w.lon = waypoint.getLongitude();
		w.description = waypoint.getNote();
		w.name = waypoint.getName();
		w.typeImagePath = waypoint.getIconName();
		w.type = toLocusWaypointType(waypoint.getWaypointType());
		return w;
	}

	protected static PointGeocachingDataTravelBug toLocusTrackable(Trackable trackable) {
		PointGeocachingDataTravelBug t = new PointGeocachingDataTravelBug();

		t.details = trackable.getDescription();
		t.goal = trackable.getGoal();
		t.imgUrl = trackable.getTrackableTypeImage();
		t.name = trackable.getName();
		//p.origin = 
		if (trackable.getOwner() != null) {
			t.owner = trackable.getOwner().getUserName();
		}
		//p.released = 
		t.srcDetails = String.format(TRACKABLE_URL, trackable.getTrackingNumber());
		return t;
	}

	protected static PointGeocachingDataLog toLocusCacheLog(CacheLog log) {
		PointGeocachingDataLog l = new PointGeocachingDataLog();
		
		l.date = GPX_TIME_FMT.format(log.getDate());
		User author = log.getAuthor();
		if (author != null) {
			l.finder = author.getUserName();
			l.finderFound = author.getFindCount();
		}
		l.logText = log.getText();
		l.type = toLocusLogType(log.getLogType());
		return l;
	}

	protected static int toLocusCacheType(CacheType cacheType) {
		switch (cacheType) {
			case CacheInTrashOutEvent:
				return PointGeocachingData.CACHE_TYPE_CACHE_IN_TRASH_OUT;
			case Earth:
				return PointGeocachingData.CACHE_TYPE_EARTH;
			case Event:
				return PointGeocachingData.CACHE_TYPE_EVENT;
			case GpsAdventuresExhibit:
				return PointGeocachingData.CACHE_TYPE_GPS_ADVENTURE;
			case LetterboxHybrid:
				return PointGeocachingData.CACHE_TYPE_LETTERBOX;
			case Locationless:
				return PointGeocachingData.CACHE_TYPE_LOCATIONLESS;
			case MegaEvent:
				return PointGeocachingData.CACHE_TYPE_MEGA_EVENT;
			case Multi:
				return PointGeocachingData.CACHE_TYPE_MULTI;
			case ProjectApe:
				return PointGeocachingData.CACHE_TYPE_PROJECT_APE;
			case Traditional:
				return PointGeocachingData.CACHE_TYPE_TRADITIONAL;
			case Unknown:
				return PointGeocachingData.CACHE_TYPE_MYSTERY;
			case Virtual:
				return PointGeocachingData.CACHE_TYPE_VIRTUAL;
			case Webcam:
				return PointGeocachingData.CACHE_TYPE_WEBCAM;
			case Wherigo:
				return PointGeocachingData.CACHE_TYPE_WHERIGO;
			default:
				return PointGeocachingData.CACHE_TYPE_MYSTERY;
		}
	}
	
	protected static int toLocusContainerType(ContainerType containerType) {
		switch(containerType) {
			case Huge:
				return PointGeocachingData.CACHE_SIZE_HUGE;
			case Large:
				return PointGeocachingData.CACHE_SIZE_LARGE;
			case Micro:
				return PointGeocachingData.CACHE_SIZE_MICRO;
			case NotChosen:
				return PointGeocachingData.CACHE_SIZE_NOT_CHOSEN;
			case Other:
				return PointGeocachingData.CACHE_SIZE_OTHER;
			case Regular:
				return PointGeocachingData.CACHE_SIZE_REGULAR;
			case Small:
				return PointGeocachingData.CACHE_SIZE_SMALL;
			default:
				return PointGeocachingData.CACHE_SIZE_NOT_CHOSEN;
		}
	}
	
	protected static String toLocusWaypointType(WaypointType waypointType) {
		switch (waypointType) {
			case FinalLocation:
				return PointGeocachingData.CACHE_WAYPOINT_TYPE_FINAL;
			case ParkingArea:
				return PointGeocachingData.CACHE_WAYPOINT_TYPE_PARKING;
			case QuestionToAnswer:
				return PointGeocachingData.CACHE_WAYPOINT_TYPE_QUESTION;
			case ReferencePoint:
				return PointGeocachingData.CACHE_WAYPOINT_TYPE_REFERENCE;
			case StagesOfAMulticache:
				return PointGeocachingData.CACHE_WAYPOINT_TYPE_STAGES;
			case Trailhead:
				return PointGeocachingData.CACHE_WAYPOINT_TYPE_TRAILHEAD;
			default:
				return PointGeocachingData.CACHE_WAYPOINT_TYPE_REFERENCE;
		} 
	}
	
	protected static int toLocusLogType(CacheLogType logType) {
		switch (logType) {
			case Announcement:
				return PointGeocachingData.CACHE_LOG_TYPE_ANNOUNCEMENT;
			case Attended:
				return PointGeocachingData.CACHE_LOG_TYPE_ATTENDED;
			case DidntFindIt:
				return PointGeocachingData.CACHE_LOG_TYPE_NOT_FOUNDED;
			case EnableListing:
				return PointGeocachingData.CACHE_LOG_TYPE_ENABLE_LISTING;
			case FoundIt:
				return PointGeocachingData.CACHE_LOG_TYPE_FOUNDED;
			case NeedsArchived:
				return PointGeocachingData.CACHE_LOG_TYPE_NEEDS_ARCHIVED;
			case NeedsMaintenance:
				return PointGeocachingData.CACHE_LOG_TYPE_NEEDS_MAINTENANCE;
			case OwnerMaintenance:
				return PointGeocachingData.CACHE_LOG_TYPE_OWNER_MAINTENANCE;
			case PostReviewerNote:
				return PointGeocachingData.CACHE_LOG_TYPE_POST_REVIEWER_NOTE;
			case PublishListing:
				return PointGeocachingData.CACHE_LOG_TYPE_PUBLISH_LISTING;
			case RetractListing:
				return PointGeocachingData.CACHE_LOG_TYPE_RETRACT_LISTING;
			case TemporarilyDisableListing:
				return PointGeocachingData.CACHE_LOG_TYPE_TEMPORARILY_DISABLE_LISTING;
			case Unknown:
				return PointGeocachingData.CACHE_LOG_TYPE_UNKNOWN;
			case UpdateCoordinates:
				return PointGeocachingData.CACHE_LOG_TYPE_UPDATE_COORDINATES;
			case WebcamPhotoTaken:
				return PointGeocachingData.CACHE_LOG_TYPE_WEBCAM_PHOTO_TAKEN;
			case WillAttend:
				return PointGeocachingData.CACHE_LOG_TYPE_WILL_ATTEND;
			case WriteNote:
				return PointGeocachingData.CACHE_LOG_TYPE_WRITE_NOTE;
			default:
				return PointGeocachingData.CACHE_LOG_TYPE_UNKNOWN;
		}
	}
}
