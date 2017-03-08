package locus.api.mapper;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.util.ReverseListIterator;
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
import locus.api.objects.extra.Location;
import locus.api.objects.extra.Waypoint;
import locus.api.objects.geocaching.GeocachingAttribute;
import locus.api.objects.geocaching.GeocachingData;
import locus.api.objects.geocaching.GeocachingImage;
import locus.api.objects.geocaching.GeocachingLog;
import locus.api.objects.geocaching.GeocachingTrackable;
import locus.api.objects.geocaching.GeocachingWaypoint;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import timber.log.Timber;

public class LocusDataMapper {
	private static final DateFormat GPX_TIME_FMT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
	private static final String GSAK_USERNAME = "gsak";
	private static final String ORIGINAL_COORDINATES_WAYPOINT_PREFIX = "RX";
	private static final Pattern FINAL_WAYPOINT_NAME_PATTERN = Pattern.compile("fin[a|á]+[l|ł]", Pattern.CASE_INSENSITIVE);
	private static final String GEOCACHE_GUID_LINK_PREFIX = "http://www.geocaching.com/seek/cache_details.aspx?guid=";

	private static final Pattern NOTE__COORDINATE_PATTERN = Pattern.compile("\\b[nNsS]\\s*\\d"); // begin of coordinates
	private static final Pattern NOTE__NAME_PATTERN = Pattern.compile("^(.+):\\s*\\z");
	private static final long WAYPOINT_BASE_ID = GeocachingUtils.base31Decode("N0");
	public static final String LITE_GEOCACHE_LISTING_HTML = "<meta http-equiv=\"refresh\" content=\"0;url=%1$s#ctl00_ContentBody_ShortDescription\" />"
			+ "<p><a href=\"%1$s#ctl00_ContentBody_ShortDescription\">%2$s</a></p>";

	static {
		GPX_TIME_FMT.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
	}

	private final Context mContext;
	private final boolean mPremiumMember;

	public LocusDataMapper(@NonNull Context context) {
		mContext = context.getApplicationContext();
		mPremiumMember = App.get(mContext).getAuthenticatorHelper().getRestrictions().isPremiumMember();
	}

	@NonNull
	public List<Waypoint> toLocusPoints(@Nullable List<Geocache> caches) {
		if (CollectionUtils.isEmpty(caches))
			return Collections.emptyList();

		List<Waypoint> points = new ArrayList<>(caches.size());
		for (Geocache cache : caches) {
			CollectionUtils.addIgnoreNull(points, toLocusPoint(cache));
		}

		return points;
	}

	@Nullable
	public locus.api.objects.extra.Waypoint toLocusPoint(@Nullable Geocache cache) {
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

		d.setDateHidden(toSafeDateLong(cache.getPlaceDate()));
		d.setDatePublished(toSafeDateLong(cache.getPublishDate()));
		d.setDateUpdated(toSafeDateLong(cache.getLastUpdateDate()));

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

		for (com.arcao.geocaching.api.data.Waypoint waypoint : CollectionUtils.emptyIfNull(
				cache.getWaypoints())) {
			CollectionUtils.addIgnoreNull(d.waypoints, toLocusWaypoint(waypoint));
		}

		for (ImageData image : CollectionUtils.emptyIfNull(cache.getImages())) {
			d.addImage(toLocusImage(image));
		}

		for (AttributeType attribute : CollectionUtils.emptyIfNull(cache.getAttributes())) {
			if (attribute == null)
				continue;

			d.attributes.add(new GeocachingAttribute(attribute.getId(), attribute.isOn()));
		}

		CollectionUtils.addIgnoreNull(d.waypoints,
				toLocusWaypoint(getCorrectedCoordinateWaypoint(cache)));

		for (com.arcao.geocaching.api.data.Waypoint waypoint : CollectionUtils.emptyIfNull(
				getWaypointsFromNote(cache))) {
			CollectionUtils.addIgnoreNull(d.waypoints, toLocusWaypoint(waypoint));
		}

		p.gcData = d;

		addCacheLogs(p, cache.getGeocacheLogs());
		addTrackables(p, cache.getTrackables());

		updateCacheLocationByCorrectedCoordinates(p, cache.getUserWaypoints());

		if (!mPremiumMember)
			applyListingForBasicMembers(p);

		return p;
	}

	public void addTrackables(@NonNull Waypoint toPoint, @Nullable List<Trackable> trackables) {
		if (trackables == null || toPoint.gcData == null)
			return;

		boolean trackableLightData = trackables.size() > 100;
		for (Trackable trackable : trackables) {
			CollectionUtils.addIgnoreNull(toPoint.gcData.trackables, toLocusTrackable(trackable, trackableLightData));
		}
	}

	private void applyListingForBasicMembers(@NonNull Waypoint toPoint) {
		if (toPoint.gcData == null)
			return;

		String longDescription = String.format(LITE_GEOCACHE_LISTING_HTML, toPoint.gcData.getCacheUrlFull(), toPoint.getName());
		toPoint.gcData.setDescriptions("", false, longDescription, true);
	}

	private void sortCacheLogsByCreated(@Nullable List<GeocacheLog> cacheLogs) {
		if (cacheLogs == null)
			return;

		Collections.sort(cacheLogs, new Comparator<GeocacheLog>() {
			@Override
			public int compare(GeocacheLog lhs, GeocacheLog rhs) {
				return lhs.getCreated().compareTo(rhs.getCreated());
			}
		});
	}


	@Nullable
	private GeocachingImage toLocusImage(@Nullable ImageData image) {
		if (image == null)
			return null;

		GeocachingImage i = new GeocachingImage();
		i.setName(image.getName());
		i.setDescription(image.getDescription());
		i.setThumbUrl(image.getThumbUrl());
		i.setUrl(image.getUrl());

		return i;
	}

	private void updateCacheLocationByCorrectedCoordinates(@NonNull Waypoint p, @Nullable Collection<UserWaypoint> userWaypoints) {
		if (CollectionUtils.isEmpty(userWaypoints))
			return;


		// find corrected coordinate user waypoint
		UserWaypoint correctedCoordinateUserWaypoint = null;
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

	@Nullable
	private GeocachingWaypoint toLocusWaypoint(@Nullable com.arcao.geocaching.api.data.Waypoint waypoint) {
		if (waypoint == null)
			return null;

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

	@Nullable
	private GeocachingTrackable toLocusTrackable(@Nullable Trackable trackable, boolean trackableLightData) {
		if (trackable == null)
			return null;

		GeocachingTrackable t = new GeocachingTrackable();
		t.setId(trackable.getId());
		t.setImgUrl(trackable.getTrackableTypeImage());
		t.setName(trackable.getName());
		if (trackable.getCurrentOwner() != null) {
			t.setCurrentOwner(trackable.getCurrentOwner().getUserName());
		}
		if (trackable.getOwner() != null) {
			t.setOriginalOwner(trackable.getOwner().getUserName());
		}
		t.setSrcDetails(trackable.getTrackablePage());
		t.setReleased(toSafeDateLong(trackable.getCreated()));

		if (!trackableLightData) {
			t.setDetails(trackable.getDescription());
			t.setGoal(trackable.getGoal());
		}
		return t;
	}

	public void addCacheLogs(@NonNull Waypoint toPoint, @Nullable Collection<GeocacheLog> logs) {
		if (toPoint.gcData == null || CollectionUtils.isEmpty(logs))
			return;

		for (GeocacheLog log : logs) {
			CollectionUtils.addIgnoreNull(toPoint.gcData.logs, toLocusCacheLog(log));
		}
	}

	@Nullable
	private GeocachingLog toLocusCacheLog(@Nullable GeocacheLog log) {
		if (log == null)
			return null;

		GeocachingLog l = new GeocachingLog();
		l.setId(log.getId());
		l.setDate(toSafeDateLong(log.getVisited()));
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

		if (log.getUpdatedCoordinates() != null) {
			l.setCooLat(log.getUpdatedCoordinates().getLatitude());
			l.setCooLon(log.getUpdatedCoordinates().getLongitude());
		}

		return l;
	}

	private int toLocusCacheType(@Nullable GeocacheType cacheType) {
		if (cacheType == null)
			return GeocachingData.CACHE_TYPE_UNDEFINED;

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

	private int toLocusContainerType(@Nullable ContainerType containerType) {
		if (containerType == null)
			return GeocachingData.CACHE_SIZE_NOT_CHOSEN;

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
				return GeocachingData.CACHE_SIZE_NOT_CHOSEN;
		}
	}

	@NonNull
	private String toLocusWaypointType(@Nullable WaypointType waypointType) {
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

	private int toLocusLogType(@Nullable GeocacheLogType logType) {
		if (logType == null)
			return  GeocachingLog.CACHE_LOG_TYPE_UNKNOWN;

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


	@Nullable
	private Collection<com.arcao.geocaching.api.data.Waypoint> getWaypointsFromNote(@NonNull Geocache geocache ) {
		String note = geocache.getPersonalNote();
		final String cacheCode = geocache.getCode();

		if (StringUtils.isBlank(note))
			return null;

		Collection<com.arcao.geocaching.api.data.Waypoint> res = new ArrayList<>();

		int count = 0;
		int nameCount = 0;
		String namePrefix = "";

		Matcher matcher = NOTE__COORDINATE_PATTERN.matcher(note);
		while (matcher.find()) {
			try {
				final Coordinates point = CoordinatesParser.parse(note.substring(matcher.start()));
				count++;

				String name = namePrefix + note.substring(0, matcher.start());

				// name can contains more lines, use the last one for name only
				int lastLineEnd = name.lastIndexOf('\n');
				if (lastLineEnd != -1)
					name = name.substring(lastLineEnd +1);

				Matcher nameMatcher = NOTE__NAME_PATTERN.matcher(name);

				WaypointType waypointType = WaypointType.ReferencePoint;

				if (nameMatcher.find() && nameMatcher.group(1).trim().length() > 0) {
					name = nameMatcher.group(1).trim();

					if (FINAL_WAYPOINT_NAME_PATTERN.matcher(name).matches()) {
						waypointType = WaypointType.FinalLocation;
					}
				} else {
					nameCount++;
					name = mContext.getString(R.string.user_waypoint_name, nameCount);
				}

				final String code = GeocachingUtils.base31Encode(WAYPOINT_BASE_ID + count) + cacheCode.substring(2);

				res.add(new com.arcao.geocaching.api.data.Waypoint(point, new Date(), code, name, "", waypointType));

				namePrefix = "";
			} catch (ParseException e) {
				Timber.w(e, e.getMessage());

				// fix for "S1: N 49 ..."
				namePrefix += note.substring(0, matcher.start() + 1);
			}

			note = note.substring(matcher.start() + 1);
			matcher = NOTE__COORDINATE_PATTERN.matcher(note);
		}

		return res;
	}

	@Nullable
	private com.arcao.geocaching.api.data.Waypoint getCorrectedCoordinateWaypoint(@NonNull Geocache geocache) {
		final Collection<UserWaypoint> userWaypoints = geocache.getUserWaypoints();
		final String cacheCode = geocache.getCode();

		if (CollectionUtils.isEmpty(userWaypoints))
			return null;

		for (UserWaypoint uw : userWaypoints) {
			if (uw.isCorrectedCoordinate()) {

				final String name = mContext.getString(R.string.final_location_name);
				final String waypointCode = GeocachingUtils.base31Encode(WAYPOINT_BASE_ID) + cacheCode.substring(2);

				return new com.arcao.geocaching.api.data.Waypoint(uw.getCoordinates(), new Date(),
						waypointCode, name, uw.getDescription(), WaypointType.FinalLocation);
			}
		}

		return null;
	}

	public void mergePoints(@NonNull Waypoint toPoint, @Nullable Waypoint fromPoint) {
		toPoint.removeExtraOnDisplay();

		if (fromPoint == null || fromPoint.gcData == null)
			return;

		fixArchivedCacheLocation(toPoint, fromPoint);
		copyGsakGeocachingLogs(toPoint.gcData.logs, fromPoint.gcData.logs);
		fixComputedCoordinates(toPoint, fromPoint);
		copyWaypointId(toPoint, fromPoint);
		copyGcVote(toPoint, fromPoint);
		fixEditedWaypoints(toPoint, fromPoint);
	}

	public void mergeCacheLogs(@NonNull Waypoint dstWaypoint, @Nullable Waypoint srcWaypoint) {
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
		for(GeocachingLog fromLog : new ReverseListIterator<>(srcLogs)) {
			if (GSAK_USERNAME.equalsIgnoreCase(fromLog.getFinder())) {
				fromLog.setDate(System.currentTimeMillis());
				dstLogs.add(0, fromLog);
			}
		}
	}

	private static long toSafeDateLong(@Nullable Date date) {
		return date != null ? date.getTime() : 0;
	}

	// issue #13: Use old coordinates when cache is archived after update
	private void fixArchivedCacheLocation(@NonNull Waypoint toPoint, @NonNull Waypoint fromPoint) {
		if (fromPoint.gcData == null || toPoint.gcData == null)
			return;

		double latitude = fromPoint.getLocation().getLatitude();
		double longitude  = fromPoint.getLocation().getLongitude();

		// are valid coordinates
		if (Double.isNaN(latitude) || Double.isNaN(longitude) || (latitude == 0 && longitude == 0))
			return;

		// is new point not archived or has computed coordinates
		if (!toPoint.gcData.isArchived() || fromPoint.gcData.isComputed())
			return;

		// store coordinates to new point
		toPoint.getLocation().setLatitude(latitude);
		toPoint.getLocation().setLongitude(longitude);
	}

	private void fixComputedCoordinates(@NonNull Waypoint toPoint, @NonNull Waypoint fromPoint) {
		if (fromPoint.gcData == null || toPoint.gcData == null)
			return;

		if (!fromPoint.gcData.isComputed() || toPoint.gcData.isComputed())
			return;

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
	}

	private void copyWaypointId(@NonNull Waypoint toPoint, @NonNull Waypoint fromPoint) {
		if (fromPoint.id == 0)
			return;

		toPoint.id = fromPoint.id;
	}


	@Nullable
	private GeocachingWaypoint getWaypointByNamePrefix(@NonNull Waypoint fromPoint, @NonNull String prefix) {
		if (fromPoint.gcData == null || CollectionUtils.isEmpty(fromPoint.gcData.waypoints))
			return null;

		for (GeocachingWaypoint waypoint : fromPoint.gcData.waypoints) {
			if (waypoint == null || StringUtils.isEmpty(waypoint.getCode()))
				continue;

			if (waypoint.getCode().startsWith(prefix))
				return waypoint;
		}

		return null;
	}

	private void copyGcVote(@NonNull Waypoint toPoint, @NonNull Waypoint fromPoint) {
		if (fromPoint.gcData == null || toPoint.gcData == null)
			return;

		toPoint.gcData.setGcVoteAverage(fromPoint.gcData.getGcVoteAverage());
		toPoint.gcData.setGcVoteNumOfVotes(fromPoint.gcData.getGcVoteNumOfVotes());
		toPoint.gcData.setGcVoteUserVote(fromPoint.gcData.getGcVoteUserVote());
	}

	private void fixEditedWaypoints(@NonNull Waypoint toPoint, @NonNull Waypoint fromPoint) {
		if (toPoint.gcData == null || CollectionUtils.isEmpty(fromPoint.gcData.waypoints) ||
				CollectionUtils.isEmpty(toPoint.gcData.waypoints))
			return;

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
	}
}
