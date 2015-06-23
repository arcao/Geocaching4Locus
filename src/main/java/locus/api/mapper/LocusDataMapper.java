package locus.api.mapper;

import android.content.Context;

import com.arcao.geocaching.api.data.Geocache;
import com.arcao.geocaching.api.data.GeocacheLog;
import com.arcao.geocaching.api.data.ImageData;
import com.arcao.geocaching.api.data.Trackable;
import com.arcao.geocaching.api.data.User;
import com.arcao.geocaching.api.data.UserWaypoint;
import com.arcao.geocaching.api.data.coordinates.Coordinates;
import com.arcao.geocaching.api.data.coordinates.CoordinatesParser;
import com.arcao.geocaching.api.data.type.AttributeType;
import com.arcao.geocaching.api.data.type.ContainerType;
import com.arcao.geocaching.api.data.type.GeocacheLogType;
import com.arcao.geocaching.api.data.type.GeocacheType;
import com.arcao.geocaching.api.data.type.WaypointType;
import com.arcao.geocaching.api.util.GeocachingUtils;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.util.ReverseListIterator;

import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import locus.api.objects.extra.ExtraData;
import locus.api.objects.extra.Location;
import locus.api.objects.extra.Waypoint;
import locus.api.objects.geocaching.GeocachingAttribute;
import locus.api.objects.geocaching.GeocachingData;
import locus.api.objects.geocaching.GeocachingImage;
import locus.api.objects.geocaching.GeocachingLog;
import locus.api.objects.geocaching.GeocachingTrackable;
import locus.api.objects.geocaching.GeocachingWaypoint;
import timber.log.Timber;

public class LocusDataMapper {
	private static final DateFormat GPX_TIME_FMT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
	private static final String GSAK_USERNAME = "gsak";
	private static final String ORIGINAL_COORDINATES_WAYPOINT_PREFIX = "RX";
	private static final Pattern FINAL_WAYPOINT_NAME_PATTERN = Pattern.compile("fin[a|á]l", Pattern.CASE_INSENSITIVE);
	private static final String GEOCACHE_GUID_LINK_PREFIX = "http://www.geocaching.com/seek/cache_details.aspx?guid=";

	static {
		GPX_TIME_FMT.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
	}

	public static List<Waypoint> toLocusPoints(Context context, Iterable<Geocache> caches) {
		List<Waypoint> points = new ArrayList<>();
		for (Geocache cache : caches) {
			points.add(toLocusPoint(context, cache));
		}

		return points;
	}

	public static locus.api.objects.extra.Waypoint toLocusPoint(Context context, Geocache cache) {
		if (cache == null)
			return null;

		Location loc = new Location(cache.getCode());
		loc.setLatitude(cache.getCoordinates().getLatitude());
		loc.setLongitude(cache.getCoordinates().getLongitude());

		Waypoint p = new Waypoint(cache.getName(), loc);

		GeocachingData d = new GeocachingData();
		d.setCacheID(cache.getCode());
		d.setId(cache.getId());
		d.setName(cache.getName());
		d.setType(toLocusCacheType(cache.getGeocacheType()));
		d.setDifficulty(cache.getDifficulty());
		d.setTerrain(cache.getTerrain());
		if (cache.getOwner() != null) {
			d.setOwner(cache.getOwner().getUserName());
		}
		d.setPlacedBy(cache.getPlacedBy());
		d.setAvailable(cache.isAvailable());
		d.setArchived(cache.isArchived());
		d.setPremiumOnly(cache.isPremium());
		if (cache.getGuid() != null) {
			d.setCacheUrl(GEOCACHE_GUID_LINK_PREFIX + cache.getGuid());
		}

		if (cache.getPlaceDate() != null)
			d.setDateHidden(cache.getPlaceDate().getTime());

		if (cache.getPublishDate() != null)
			d.setDatePublished(cache.getPublishDate().getTime());

		if (cache.getLastUpdateDate() != null)
  	  d.setDateUpdated(cache.getLastUpdateDate().getTime());

		d.setContainer(toLocusContainerType(cache.getContainerType()));
		d.setFound(cache.isFoundByUser());

		d.setCountry(cache.getCountryName());
		d.setState(cache.getStateName());

		d.setDescriptions(BadBBCodeFixer.fix(cache.getShortDescription()), cache.isShortDescriptionHtml(),
			BadBBCodeFixer.fix(cache.getLongDescription()), cache.isLongDescriptionHtml());
		d.setEncodedHints(cache.getHint());
		d.setNotes(cache.getPersonalNote());
		d.setFavoritePoints(cache.getFavoritePoints());

		sortCacheLogsByCreated(cache.getGeocacheLogs());
		for (GeocacheLog log : cache.getGeocacheLogs()) {
			d.logs.add(toLocusCacheLog(log));
		}

		for (Trackable trackable : cache.getTrackables()) {
			d.trackables.add(toLocusTrackable(trackable));
		}

		for (com.arcao.geocaching.api.data.Waypoint waypoint : cache.getWaypoints()) {
			d.waypoints.add(toLocusWaypoint(waypoint));
		}

		for (ImageData image : cache.getImages()) {
			d.addImage(toLocusImage(image));
		}

		for (AttributeType attribute : cache.getAttributes()) {
			d.attributes.add(new GeocachingAttribute(attribute.getId(), attribute.isOn()));
		}

		for (com.arcao.geocaching.api.data.Waypoint waypoint : getWaypointsFromUserWaypoints(context, cache.getUserWaypoints(), cache.getCode())) {
			d.waypoints.add(toLocusWaypoint(waypoint));
		}

		for (com.arcao.geocaching.api.data.Waypoint waypoint : getWaypointsFromNote(context, cache.getPersonalNote(), cache.getCode())) {
			d.waypoints.add(toLocusWaypoint(waypoint));
		}

		p.gcData = d;

		updateCacheLocationByCorrectedCoordinates(context, p, cache.getUserWaypoints());

		return p;
	}

	private static void sortCacheLogsByCreated(List<GeocacheLog> cacheLogs) {
		Collections.sort(cacheLogs, new Comparator<GeocacheLog>() {
			@Override
			public int compare(GeocacheLog lhs, GeocacheLog rhs) {
				return lhs.getCreated().compareTo(rhs.getCreated());
			}
		});
	}


	private static GeocachingImage toLocusImage(ImageData image) {
		GeocachingImage i = new GeocachingImage();
		i.setName(image.getName());
		i.setDescription(image.getDescription());
		i.setThumbUrl(image.getThumbUrl());
		i.setUrl(image.getUrl());

		return i;
	}

	private static void updateCacheLocationByCorrectedCoordinates(Context mContext, Waypoint p, Iterable<UserWaypoint> userWaypoints) {
		UserWaypoint correctedCoordinateUserWaypoint = null;

		// find corrected coordinate user waypoint
		for (UserWaypoint w : userWaypoints) {
			if (w.isCorrectedCoordinate()) {
				correctedCoordinateUserWaypoint = w;
				break;
			}
		}

		// continue only if something was found
		if (correctedCoordinateUserWaypoint == null)
			return;

		Location location = p.getLocation();

		p.gcData.setComputed(true);
		p.gcData.setLatOriginal(location.getLatitude());
		p.gcData.setLonOriginal(location.getLongitude());

		// store original location to waypoint
		GeocachingWaypoint waypoint = getWaypointByNamePrefix(p, ORIGINAL_COORDINATES_WAYPOINT_PREFIX);
		if (waypoint == null) {
			waypoint = new GeocachingWaypoint();
			p.gcData.waypoints.add(waypoint);
		}

		waypoint.setCode(ORIGINAL_COORDINATES_WAYPOINT_PREFIX + p.gcData.getCacheID().substring(2));
		waypoint.setType(GeocachingWaypoint.CACHE_WAYPOINT_TYPE_REFERENCE);
		waypoint.setName(mContext.getString(R.string.original_coordinates_name));
		waypoint.setLat(location.getLatitude());
		waypoint.setLon(location.getLongitude());

		// update coordinates to new location
		Location newLocation = new Location(location.getProvider());
		newLocation.setLatitude(correctedCoordinateUserWaypoint.getLatitude());
		newLocation.setLongitude(correctedCoordinateUserWaypoint.getLongitude());
		location.set(newLocation);
	}

	private static GeocachingWaypoint toLocusWaypoint(com.arcao.geocaching.api.data.Waypoint waypoint) {
		GeocachingWaypoint w = new GeocachingWaypoint();

		w.setCode(waypoint.getWaypointCode());
		w.setLat(waypoint.getLatitude());
		w.setLon(waypoint.getLongitude());
		w.setDesc(waypoint.getNote());
		w.setName(waypoint.getName());
		w.setTypeImagePath(waypoint.getIconName());
		w.setType(toLocusWaypointType(waypoint.getWaypointType()));
		return w;
	}

	private static GeocachingTrackable toLocusTrackable(Trackable trackable) {
		GeocachingTrackable t = new GeocachingTrackable();

		t.setId(trackable.getId());
		t.setDetails(trackable.getDescription());
		//t.setGoal(trackable.getGoal());
		t.setImgUrl(trackable.getTrackableTypeImage());
		t.setName(trackable.getName());
		/*if (trackable.getCurrentOwner() != null) {
			t.setCurrentOwner(trackable.getCurrentOwner().getUserName());
		}
		if (trackable.getOwner() != null) {
			t.setOriginalOwner(trackable.getOwner().getUserName());
		}*/
		//t.setReleased(trackable.getCreated().getTime());
		//t.setSrcDetails(trackable.getTrackablePage());
		return t;
	}

	private static GeocachingLog toLocusCacheLog(GeocacheLog log) {
		GeocachingLog l = new GeocachingLog();

		l.setId(log.getId());
		l.setDate(log.getVisited().getTime());
		User author = log.getAuthor();
		if (author != null) {
			l.setFinder(author.getUserName());
			l.setFindersFound(author.getFindCount());
			l.setFindersId(author.getId());
		}
		l.setLogText(log.getText());
		l.setType(toLocusLogType(log.getLogType()));

		for (ImageData image: log.getImages()) {
			l.addImage(toLocusImage(image));
		}

		return l;
	}

	private static int toLocusCacheType(GeocacheType cacheType) {
		switch (cacheType) {
			case CacheInTrashOutEvent:
				return GeocachingData.CACHE_TYPE_CACHE_IN_TRASH_OUT;
			case Earth:
				return GeocachingData.CACHE_TYPE_EARTH;
			case Event:
				return GeocachingData.CACHE_TYPE_EVENT;
			case GpsAdventuresExhibit:
				return GeocachingData.CACHE_TYPE_GPS_ADVENTURE;
			case GroundspeakBlockParty:
				return GeocachingData.CACHE_TYPE_GROUNDSPEAK;
			case GroudspeakHQ:
				return GeocachingData.CACHE_TYPE_GROUNDSPEAK;
			case GroudspeakLostAndFoundCelebration:
				return GeocachingData.CACHE_TYPE_LF_CELEBRATION;
			case LetterboxHybrid:
				return GeocachingData.CACHE_TYPE_LETTERBOX;
			case Locationless:
				return GeocachingData.CACHE_TYPE_LOCATIONLESS;
			case LostAndFoundEvent:
				return GeocachingData.CACHE_TYPE_LF_EVENT;
			case MegaEvent:
				return GeocachingData.CACHE_TYPE_MEGA_EVENT;
			case Multi:
				return GeocachingData.CACHE_TYPE_MULTI;
			case ProjectApe:
				return GeocachingData.CACHE_TYPE_PROJECT_APE;
			case Traditional:
				return GeocachingData.CACHE_TYPE_TRADITIONAL;
			case Mystery:
				return GeocachingData.CACHE_TYPE_MYSTERY;
			case Virtual:
				return GeocachingData.CACHE_TYPE_VIRTUAL;
			case Webcam:
				return GeocachingData.CACHE_TYPE_WEBCAM;
			case Wherigo:
				return GeocachingData.CACHE_TYPE_WHERIGO;
			case GigaEvent:
				return GeocachingData.CACHE_TYPE_GIGA_EVENT;
			default:
				return GeocachingData.CACHE_TYPE_MYSTERY;
		}
	}

	private static int toLocusContainerType(ContainerType containerType) {
		switch(containerType) {
			case Huge:
				return GeocachingData.CACHE_SIZE_HUGE;
			case Large:
				return GeocachingData.CACHE_SIZE_LARGE;
			case Micro:
				return GeocachingData.CACHE_SIZE_MICRO;
			case NotChosen:
				return GeocachingData.CACHE_SIZE_NOT_CHOSEN;
			case Other:
				return GeocachingData.CACHE_SIZE_OTHER;
			case Regular:
				return GeocachingData.CACHE_SIZE_REGULAR;
			case Small:
				return GeocachingData.CACHE_SIZE_SMALL;
			default:
				return GeocachingData.CACHE_SIZE_OTHER;
		}
	}

	private static String toLocusWaypointType(WaypointType waypointType) {
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

	private static int toLocusLogType(GeocacheLogType logType) {
		switch (logType) {
			case Announcement:
				return GeocachingLog.CACHE_LOG_TYPE_ANNOUNCEMENT;
			case Attended:
				return GeocachingLog.CACHE_LOG_TYPE_ATTENDED;
			case DidntFindIt:
				return GeocachingLog.CACHE_LOG_TYPE_NOT_FOUND;
			case EnableListing:
				return GeocachingLog.CACHE_LOG_TYPE_ENABLE_LISTING;
			case FoundIt:
				return GeocachingLog.CACHE_LOG_TYPE_FOUND;
			case NeedsArchived:
				return GeocachingLog.CACHE_LOG_TYPE_NEEDS_ARCHIVED;
			case NeedsMaintenance:
				return GeocachingLog.CACHE_LOG_TYPE_NEEDS_MAINTENANCE;
			case OwnerMaintenance:
				return GeocachingLog.CACHE_LOG_TYPE_OWNER_MAINTENANCE;
			case PostReviewerNote:
				return GeocachingLog.CACHE_LOG_TYPE_POST_REVIEWER_NOTE;
			case PublishListing:
				return GeocachingLog.CACHE_LOG_TYPE_PUBLISH_LISTING;
			case RetractListing:
				return GeocachingLog.CACHE_LOG_TYPE_RETRACT_LISTING;
			case TemporarilyDisableListing:
				return GeocachingLog.CACHE_LOG_TYPE_TEMPORARILY_DISABLE_LISTING;
			case Unknown:
				return GeocachingLog.CACHE_LOG_TYPE_UNKNOWN;
			case UpdateCoordinates:
				return GeocachingLog.CACHE_LOG_TYPE_UPDATE_COORDINATES;
			case WebcamPhotoTaken:
				return GeocachingLog.CACHE_LOG_TYPE_WEBCAM_PHOTO_TAKEN;
			case WillAttend:
				return GeocachingLog.CACHE_LOG_TYPE_WILL_ATTEND;
			case WriteNote:
				return GeocachingLog.CACHE_LOG_TYPE_WRITE_NOTE;
			case Archive:
				return GeocachingLog.CACHE_LOG_TYPE_ARCHIVE;
			case Unarchive:
				return GeocachingLog.CACHE_LOG_TYPE_UNARCHIVE;
			default:
				return GeocachingLog.CACHE_LOG_TYPE_UNKNOWN;
		}
	}

	private static Iterable<com.arcao.geocaching.api.data.Waypoint> getWaypointsFromNote(Context context, String note, String cacheCode) {
		List<com.arcao.geocaching.api.data.Waypoint> res = new ArrayList<>();

		if (StringUtils.isBlank(note)) {
			return res;
		}

		final Pattern coordPattern = Pattern.compile("\\b[nNsS]\\s*\\d"); // begin of coordinates
		final Pattern namePattern = Pattern.compile("^(.+):\\s*\\z");
		final long waypointBaseId = GeocachingUtils.base31Decode("N0");

		int count = 0;
		int nameCount = 0;

		String namePrefix = "";

		Matcher matcher = coordPattern.matcher(note);
		while (matcher.find()) {
			try {
				final Coordinates point = CoordinatesParser.parse(note.substring(matcher.start()));
				count++;

				String name = namePrefix + note.substring(0, matcher.start());

				// TODO fix it better
				int lastLineEnd = name.lastIndexOf('\n');
				if (lastLineEnd != -1)
					name = name.substring(lastLineEnd +1);

				Matcher nameMatcher = namePattern.matcher(name);

				WaypointType waypointType = WaypointType.ReferencePoint;

				if (nameMatcher.find() && nameMatcher.group(1).trim().length() > 0) {
					name = nameMatcher.group(1).trim();

					if (FINAL_WAYPOINT_NAME_PATTERN.matcher(name).matches()) {
						waypointType = WaypointType.FinalLocation;
					}
				} else {
					nameCount++;
					name = context.getString(R.string.user_waypoint_name, nameCount);
				}

				final String code = GeocachingUtils.base31Encode(waypointBaseId + count) + cacheCode.substring(2);

				res.add(new com.arcao.geocaching.api.data.Waypoint(point, new Date(), code, name, "", waypointType));

				namePrefix = "";
			} catch (ParseException e) {
				Timber.w(e, e.getMessage());

				// fix for "S1: N 49 ..."
				namePrefix += note.substring(0, matcher.start() + 1);
			}

			note = note.substring(matcher.start() + 1);
			matcher = coordPattern.matcher(note);
		}
		return res;
	}

	private static Iterable<com.arcao.geocaching.api.data.Waypoint> getWaypointsFromUserWaypoints(Context context, Collection<UserWaypoint> userWaypoints, String cacheCode) {
		List<com.arcao.geocaching.api.data.Waypoint> res = new ArrayList<>();

		if (userWaypoints == null || userWaypoints.size() == 0)
			return res;

		final int waypointBaseId = (int) GeocachingUtils.base31Decode("N0");

		int count = 1;
		for (UserWaypoint uw : userWaypoints) {
			if (!uw.isCorrectedCoordinate())
				continue;

			final String name = context.getString(R.string.final_location_name, count);
			final String waypointCode = GeocachingUtils.base31Encode(waypointBaseId + count) + cacheCode.substring(2);

			res.add(new com.arcao.geocaching.api.data.Waypoint(uw.getCoordinates(), new Date(), waypointCode, name, uw.getDescription(), WaypointType.FinalLocation));
			count++;
		}

		return res;
	}

	public static Waypoint mergePoints(Context mContext, Waypoint toPoint, Waypoint fromPoint) {
		clearExtraOnDisplayCallback(toPoint);

		if (fromPoint == null || fromPoint.gcData == null)
			return toPoint;

		fixArchivedCacheLocation(toPoint, fromPoint);
		mergeCacheLogs(toPoint, fromPoint);
		fixComputedCoordinates(mContext, toPoint, fromPoint);
		copyWaypointId(toPoint, fromPoint);
		copyGcVote(toPoint, fromPoint);
		fixEditedWaypoints(toPoint, fromPoint);

		return toPoint;
	}

	private static void clearExtraOnDisplayCallback(Waypoint p) {
		p.addParameter(ExtraData.PAR_INTENT_EXTRA_ON_DISPLAY, "clear");
	}

	// issue #14: Keep cache logs from GSAK when updating cache
	public static Waypoint mergeCacheLogs(Waypoint toPoint, Waypoint fromPoint) {
		if (fromPoint.gcData.logs.size() == 0)
			return toPoint;

		for(GeocachingLog fromLog : new ReverseListIterator<>(fromPoint.gcData.logs)) {
			if (GSAK_USERNAME.equalsIgnoreCase(fromLog.getFinder())) {
				fromLog.setDate(new Date().getTime());
				toPoint.gcData.logs.add(0, fromLog);
			}
		}

		return toPoint;
	}

	// issue #13: Use old coordinates when cache is archived after update
	private static Waypoint fixArchivedCacheLocation(Waypoint toPoint, Waypoint fromPoint) {
		if (!toPoint.gcData.isArchived() || (fromPoint.getLocation().getLatitude() == 0 && fromPoint.getLocation().getLongitude() == 0)
			|| Double.isNaN(fromPoint.getLocation().getLatitude()) || Double.isNaN(fromPoint.getLocation().getLongitude())
			|| fromPoint.gcData.isComputed())
			return toPoint;

		toPoint.getLocation().setLatitude(fromPoint.getLocation().getLatitude());
		toPoint.getLocation().setLongitude(fromPoint.getLocation().getLongitude());

		return toPoint;
	}

	private static Waypoint fixComputedCoordinates(Context mContext, Waypoint toPoint, Waypoint fromPoint) {
		if (!fromPoint.gcData.isComputed() || toPoint.gcData.isComputed())
			return toPoint;

		Location location = toPoint.getLocation();

		toPoint.gcData.setLatOriginal(location.getLatitude());
		toPoint.gcData.setLonOriginal(location.getLongitude());
		toPoint.gcData.setComputed(true);

		// store original location to waypoint
		GeocachingWaypoint waypoint = getWaypointByNamePrefix(toPoint, ORIGINAL_COORDINATES_WAYPOINT_PREFIX);
		if (waypoint == null) {
			waypoint = new GeocachingWaypoint();
			toPoint.gcData.waypoints.add(waypoint);
		}

		waypoint.setCode(ORIGINAL_COORDINATES_WAYPOINT_PREFIX + toPoint.gcData.getCacheID().substring(2));
		waypoint.setType(GeocachingWaypoint.CACHE_WAYPOINT_TYPE_REFERENCE);
		waypoint.setName(mContext.getString(R.string.original_coordinates_name));
		waypoint.setLat(location.getLatitude());
		waypoint.setLon(location.getLongitude());

		// update coordinates to new location
		location.set(fromPoint.getLocation());
		return toPoint;
	}

	private static Waypoint copyWaypointId(Waypoint toPoint, Waypoint fromPoint) {
		if (fromPoint.id == 0)
			return toPoint;

		toPoint.id = fromPoint.id;

		return toPoint;
	}


	private static GeocachingWaypoint getWaypointByNamePrefix(Waypoint fromPoint, String prefix) {
		if (fromPoint.gcData == null)
			return null;

		for (GeocachingWaypoint waypoint : fromPoint.gcData.waypoints) {
			if (waypoint.getCode() != null && waypoint.getCode().startsWith(prefix)) {
				return waypoint;
			}
		}

		return null;
	}

	private static Waypoint copyGcVote(Waypoint toPoint, Waypoint fromPoint) {
		if (fromPoint.gcData == null)
			return toPoint;

		toPoint.gcData.setGcVoteAverage(fromPoint.gcData.getGcVoteAverage());
		toPoint.gcData.setGcVoteNumOfVotes(fromPoint.gcData.getGcVoteNumOfVotes());
		toPoint.gcData.setGcVoteUserVote(fromPoint.gcData.getGcVoteUserVote());

		return toPoint;
	}

	private static Waypoint fixEditedWaypoints(Waypoint toPoint, Waypoint fromPoint) {
		if (fromPoint.gcData.waypoints == null || fromPoint.gcData.waypoints.size() == 0
			|| toPoint.gcData == null || toPoint.gcData.waypoints.size() == 0)
			return toPoint;

		// find Waypoint with zero coordinates
		for (GeocachingWaypoint waypoint : toPoint.gcData.waypoints) {
			if (waypoint.getLat() == 0 && waypoint.getLon() == 0) {

				// replace with coordinates from fromPoint Waypoint
				for (GeocachingWaypoint fromWaypoint : fromPoint.gcData.waypoints) {

					if (waypoint.getCode().equalsIgnoreCase(fromWaypoint.getCode())) {
						waypoint.setLat(fromWaypoint.getLat());
						waypoint.setLon(fromWaypoint.getLon());
					}
				}

			}
		}

		return toPoint;
	}
}
