package locus.api.mapper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import locus.api.android.ActionTools;
import locus.api.android.utils.RequiredVersionMissingException;
import locus.api.objects.extra.ExtraData;
import locus.api.objects.extra.Location;
import locus.api.objects.extra.Waypoint;
import locus.api.objects.geocaching.GeocachingAttribute;
import locus.api.objects.geocaching.GeocachingData;
import locus.api.objects.geocaching.GeocachingLog;
import locus.api.objects.geocaching.GeocachingTrackable;
import locus.api.objects.geocaching.GeocachingWaypoint;
import net.sf.jtpl.Template;

import org.apache.commons.lang3.StringUtils;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import com.arcao.geocaching.api.data.CacheLog;
import com.arcao.geocaching.api.data.Geocache;
import com.arcao.geocaching.api.data.ImageData;
import com.arcao.geocaching.api.data.SimpleGeocache;
import com.arcao.geocaching.api.data.Trackable;
import com.arcao.geocaching.api.data.User;
import com.arcao.geocaching.api.data.UserWaypoint;
import com.arcao.geocaching.api.data.coordinates.Coordinates;
import com.arcao.geocaching.api.data.coordinates.CoordinatesParser;
import com.arcao.geocaching.api.data.type.AttributeType;
import com.arcao.geocaching.api.data.type.CacheLogType;
import com.arcao.geocaching.api.data.type.CacheType;
import com.arcao.geocaching.api.data.type.ContainerType;
import com.arcao.geocaching.api.data.type.WaypointType;
import com.arcao.geocaching.api.util.GeocachingUtils;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.constants.PrefConstants;
import com.arcao.geocaching4locus.util.ReverseListIterator;

public class LocusDataMapper {
	private static final String TAG = "LocusDataMapper";
	
	protected static final DateFormat GPX_TIME_FMT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
	protected static final String TRACKABLE_URL = "http://www.geocaching.com/track/details.aspx?tracker=%s";
	protected static final String GSAK_USERNAME = "gsak";
	protected static final String ORIGINAL_COORDINATES_WAYPOINT_PREFIX = "RX";

	static {
		GPX_TIME_FMT.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
	}
	
	protected static File locusGeocachingDataBasePath;
	
	public static List<Waypoint> toLocusPoints(Context context, List<? extends SimpleGeocache> caches) {
		List<Waypoint> points = new ArrayList<Waypoint>();
		for (SimpleGeocache cache : caches) {
			points.add(toLocusPoint(context, cache));
		}
		
		return points;
	}
	
	public static locus.api.objects.extra.Waypoint toLocusPoint(Context context, SimpleGeocache cache) {
		if (cache == null)
			return null;
		
		Location loc = new Location(cache.getClass().getName());
		loc.setLatitude(cache.getLatitude());
		loc.setLongitude(cache.getLongitude());

		Waypoint p = new Waypoint(cache.getName(), loc);

		GeocachingData d = new GeocachingData();
		d.setCacheID(cache.getCacheCode());
		d.setId(cache.getId()); 
		d.setName(cache.getName());
		d.type = toLocusCacheType(cache.getCacheType());
		d.difficulty = cache.getDifficultyRating();
		d.terrain = cache.getTerrainRating();
		if (cache.getAuthor() != null) {
			d.setOwner(cache.getAuthor().getUserName());
		}
		d.setPlacedBy(cache.getContactName());
		d.available = cache.isAvailable();
		d.archived = cache.isArchived();
		d.premiumOnly = cache.isPremiumListing();
		d.hidden = cache.getPlaced().getTime();
		d.exported = new Date().getTime();
		d.container = toLocusContainerType(cache.getContainerType());
		d.found = cache.isFound();

		if (cache instanceof Geocache) {
			Geocache gc = (Geocache) cache;

			d.setCountry(gc.getCountryName());
			d.setState(gc.getStateName());

			d.setShortDescription(gc.getShortDescription(), true);
			d.setLongDescription(gc.getLongDescription(), true);
			d.setEncodedHints(gc.getHint());
			d.setNotes(gc.getPersonalNote());
			d.favoritePoints = gc.getFavoritePoints();

			for (CacheLog log : gc.getCacheLogs()) {
				d.logs.add(toLocusCacheLog(log));
			}

			for (Trackable trackable : gc.getTrackables()) {
				d.trackables.add(toLocusTrackable(trackable));
			}

			for (com.arcao.geocaching.api.data.Waypoint waypoint : gc.getWaypoints()) {
				d.waypoints.add(toLocusWaypoint(waypoint));
			}

			for (AttributeType attribute : gc.getAttributes()) {
				d.attributes.add(new GeocachingAttribute(attribute.getId(), attribute.isOn()));
			}

			for (com.arcao.geocaching.api.data.Waypoint waypoint : getWaypointsFromUserWaypoints(context, gc.getUserWaypoints(), gc.getCacheCode())) {
				d.waypoints.add(toLocusWaypoint(waypoint));
			}
			
			for (com.arcao.geocaching.api.data.Waypoint waypoint : getWaypointsFromNote(context, gc.getPersonalNote(), gc.getCacheCode())) {
				d.waypoints.add(toLocusWaypoint(waypoint));
			}
		}
		
		p.gcData = d;

		if (cache instanceof Geocache) {
			Geocache gc = (Geocache) cache;
			updateCacheLocationByCorrectedCoordinates(context, p, gc.getUserWaypoints());
			if (gc.getImages().size() > 0 && isCacheImagesTabAllowed(context)) {
				generateImagesHtml(context, gc);
			}
		}

		return p;
	}

	protected static boolean isCacheImagesTabAllowed(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PrefConstants.DOWNLOADING_CREATE_IMAGES_TAB, false);
	}

	protected static void generateImagesHtml(Context context, Geocache gc) {
		try {
			Template t = new Template(new InputStreamReader(context.getResources().openRawResource(R.raw.images), "UTF-8"));
			
			for (ImageData image : gc.getImages()) {
				t.parse("main.image", image);
			}
			
			t.parse("main", gc);
			
			File imagesHtmlFile = new File(getCacheImageBasePath(context, gc), "images.html");
			if (imagesHtmlFile.exists()) {
				imagesHtmlFile.delete();
			}
			
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(imagesHtmlFile), "UTF-8"));
			bw.write(t.out());
			bw.flush();
			bw.close();
			
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}
		
	protected static File getCacheImageBasePath(Context context, Geocache cache) throws RequiredVersionMissingException {
	  if (locusGeocachingDataBasePath == null) {
		  File locusBasePath = new File(ActionTools.getLocusRootDirectory(context));
      
		  locusGeocachingDataBasePath = new File (locusBasePath, "data" + File.separator + "geocaching");
	  }
	  
	  String cacheCode = cache.getCacheCode();
	  
	  StringBuilder sb = new StringBuilder();
	  sb.append(cacheCode.charAt(cacheCode.length() - 1));
	  sb.append(File.separatorChar);
	  sb.append(cacheCode.charAt(cacheCode.length() - 2));
	  sb.append(File.separatorChar);
	  sb.append(cacheCode);
	  
	  File cacheImageDir = new File(locusGeocachingDataBasePath, sb.toString());
	  
	  cacheImageDir.mkdirs();
	  
	  return cacheImageDir;
	}
	

	protected static void updateCacheLocationByCorrectedCoordinates(Context mContext, Waypoint p, List<UserWaypoint> userWaypoints) {
		if (userWaypoints.size() != 1)
			return;

		Location original = p.getLocation();
		UserWaypoint userWaypoint = userWaypoints.get(0);

		Location loc = new Location(userWaypoint.getClass().getName());
		loc.setLatitude(userWaypoint.getLatitude());
		loc.setLongitude(userWaypoint.getLongitude());
		p.getLocation().set(loc);

		p.gcData.computed = true;

		// store original location to waypoint
		GeocachingWaypoint waypoint = getWaypointByNamePrefix(p, ORIGINAL_COORDINATES_WAYPOINT_PREFIX);
		if (waypoint == null) {
			waypoint = new GeocachingWaypoint();
			p.gcData.waypoints.add(waypoint);
		}

		waypoint.code = ORIGINAL_COORDINATES_WAYPOINT_PREFIX + p.gcData.getCacheID().substring(2);
		waypoint.type = GeocachingWaypoint.CACHE_WAYPOINT_TYPE_REFERENCE;
		waypoint.name = mContext.getString(R.string.original_coordinates_name);
		waypoint.lat = original.getLatitude();
		waypoint.lon = original.getLongitude();
	}

	protected static GeocachingWaypoint toLocusWaypoint(com.arcao.geocaching.api.data.Waypoint waypoint) {
		GeocachingWaypoint w = new GeocachingWaypoint();
		
		w.code = waypoint.getWaypointCode();
		w.lat = waypoint.getLatitude();
		w.lon = waypoint.getLongitude();
		w.desc = waypoint.getNote();
		w.name = waypoint.getName();
		w.typeImagePath = waypoint.getIconName();
		w.type = toLocusWaypointType(waypoint.getWaypointType());
		return w;
	}

	protected static GeocachingTrackable toLocusTrackable(Trackable trackable) {
		GeocachingTrackable t = new GeocachingTrackable();

		t.id = trackable.getId();
		t.details = trackable.getDescription();
		t.goal = trackable.getGoal();
		t.imgUrl = trackable.getTrackableTypeImage();
		t.name = trackable.getName();
		if (trackable.getOwner() != null) {
			t.currentOwner = trackable.getOwner().getUserName();
		}
		t.released = trackable.getCreated().getTime(); 
		t.srcDetails = String.format(TRACKABLE_URL, trackable.getTrackingNumber());
		return t;
	}

	protected static GeocachingLog toLocusCacheLog(CacheLog log) {
		GeocachingLog l = new GeocachingLog();
		
		l.id = log.getId();		
		l.date = log.getVisited().getTime();
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
			case Unknown:
				return GeocachingData.CACHE_TYPE_MYSTERY;
			case Virtual:
				return GeocachingData.CACHE_TYPE_VIRTUAL;
			case Webcam:
				return GeocachingData.CACHE_TYPE_WEBCAM;
			case Wherigo:
				return GeocachingData.CACHE_TYPE_WHERIGO;
			default:
				return GeocachingData.CACHE_TYPE_MYSTERY;
		}
	}
	
	protected static int toLocusContainerType(ContainerType containerType) {
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
	
	protected static String toLocusWaypointType(WaypointType waypointType) {
		switch (waypointType) {
			case FinalLocation:
				return GeocachingWaypoint.CACHE_WAYPOINT_TYPE_FINAL;
			case ParkingArea:
				return GeocachingWaypoint.CACHE_WAYPOINT_TYPE_PARKING;
			case QuestionToAnswer:
				return GeocachingWaypoint.CACHE_WAYPOINT_TYPE_QUESTION;
			case ReferencePoint:
				return GeocachingWaypoint.CACHE_WAYPOINT_TYPE_REFERENCE;
			case StagesOfAMulticache:
				return GeocachingWaypoint.CACHE_WAYPOINT_TYPE_STAGES;
			case Trailhead:
				return GeocachingWaypoint.CACHE_WAYPOINT_TYPE_TRAILHEAD;
			default:
				return GeocachingWaypoint.CACHE_WAYPOINT_TYPE_REFERENCE;
		} 
	}
	
	protected static int toLocusLogType(CacheLogType logType) {
		switch (logType) {
			case Announcement:
				return GeocachingLog.CACHE_LOG_TYPE_ANNOUNCEMENT;
			case Attended:
				return GeocachingLog.CACHE_LOG_TYPE_ATTENDED;
			case DidntFindIt:
				return GeocachingLog.CACHE_LOG_TYPE_NOT_FOUNDED;
			case EnableListing:
				return GeocachingLog.CACHE_LOG_TYPE_ENABLE_LISTING;
			case FoundIt:
				return GeocachingLog.CACHE_LOG_TYPE_FOUNDED;
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
			default:
				return GeocachingLog.CACHE_LOG_TYPE_UNKNOWN;
		}
	}
	
    public static List<com.arcao.geocaching.api.data.Waypoint> getWaypointsFromNote(Context context, String note, String cacheCode) {
    	List<com.arcao.geocaching.api.data.Waypoint> res = new ArrayList<com.arcao.geocaching.api.data.Waypoint>();
    	
    	if (StringUtils.isBlank(note)) {
            return res;
        }
        
        final Pattern coordPattern = Pattern.compile("\\b[nNsS]{1}\\s*\\d"); // begin of coordinates
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
                if (lastLineEnd != -1);
                	name = name.substring(lastLineEnd +1);
                
                Matcher nameMatcher = namePattern.matcher(name);
                
                if (nameMatcher.find() && nameMatcher.group(1).trim().length() > 0) {
                	name = nameMatcher.group(1).trim();
                } else {
                	nameCount++;
                	name = context.getString(R.string.user_waypoint_name, nameCount);
                }
                
                final String code = GeocachingUtils.base31Encode(waypointBaseId + count) + cacheCode.substring(2);
                
                res.add(new com.arcao.geocaching.api.data.Waypoint(point, new Date(), code, name, "", WaypointType.ReferencePoint));
                
                namePrefix = "";
            } catch (ParseException e) {
                Log.w(TAG, e.getMessage());
                
                // fix for "S1: N 49 ..."
                namePrefix = namePrefix + note.substring(0, matcher.start() + 1);
            }

            note = note.substring(matcher.start() + 1);
            matcher = coordPattern.matcher(note);
        }
        return res;
    }
    
    public static List<com.arcao.geocaching.api.data.Waypoint> getWaypointsFromUserWaypoints(Context context, List<UserWaypoint> userWaypoints, String cacheCode) {
    	List<com.arcao.geocaching.api.data.Waypoint> res = new ArrayList<com.arcao.geocaching.api.data.Waypoint>();
    	
    	if (userWaypoints.size() == 0)
    		return res;

    	final int waypointBaseId = (int) GeocachingUtils.base31Decode("N0");
    	
    	int count = 1;
    	for (UserWaypoint uw : userWaypoints) {
    		final String name = context.getString(R.string.final_location_name, count);
    		final String waypointCode = GeocachingUtils.base31Encode(waypointBaseId + count) + cacheCode.substring(2);

    		res.add(new com.arcao.geocaching.api.data.Waypoint(uw.getCoordinates(), new Date(), waypointCode, name, uw.getDescription(), WaypointType.FinalLocation));
    		count++;
    	}
          	
    	return res;
    }
    
    public static Waypoint mergePoints(Context mContext, Waypoint toPoint, Waypoint fromPoint) {
    	if (fromPoint == null || fromPoint.gcData == null)
    		return toPoint;
    	
    	fixArchivedCacheLocation(mContext, toPoint, fromPoint);
    	mergeCacheLogs(mContext, toPoint, fromPoint);
    	fixComputedCoordinates(mContext, toPoint, fromPoint);
    	clearExtraOnDisplayCallback(toPoint);
    	
    	return toPoint;
    }    
    
    public static void clearExtraOnDisplayCallback(Waypoint p) {
    	p.addParameter(ExtraData.PAR_INTENT_EXTRA_CALLBACK, "clear;;;;;;");
    }

    public static Waypoint mergeCacheLogs(Context mContext, Waypoint toPoint, Waypoint fromPoint) {
    	// issue #14: Keep cache logs from GSAK when updating cache
    	if (fromPoint.gcData.logs.size() == 0) 
    		return toPoint;
    	
    	for(GeocachingLog fromLog : new ReverseListIterator<GeocachingLog>(fromPoint.gcData.logs)) {
    		if (GSAK_USERNAME.equalsIgnoreCase(fromLog.finder)) {
    			fromLog.date = new Date().getTime();
    			toPoint.gcData.logs.add(0, fromLog);
    		}
    	}
    	
    	return toPoint;
    }
    
    public static Waypoint fixArchivedCacheLocation(Context mContext, Waypoint toPoint, Waypoint fromPoint) {
    	// issue #13: Use old coordinates when cache is archived after update
    	if (!toPoint.gcData.archived || (fromPoint.getLocation().getLatitude() == 0 && fromPoint.getLocation().getLongitude() == 0) 
    			|| Double.isNaN(fromPoint.getLocation().getLatitude()) || Double.isNaN(fromPoint.getLocation().getLongitude())
    			|| fromPoint.gcData.computed) 
    		return toPoint;

    	toPoint.getLocation().setLatitude(fromPoint.getLocation().getLatitude());
    	toPoint.getLocation().setLongitude(fromPoint.getLocation().getLongitude());

    	return toPoint;
    }

    public static Waypoint fixComputedCoordinates(Context mContext, Waypoint toPoint, Waypoint fromPoint) {
    	if (!fromPoint.gcData.computed || toPoint.gcData.computed)
    		return toPoint;

    	Location original = toPoint.getLocation();

    	toPoint.getLocation().set(fromPoint.getLocation());
    	toPoint.gcData.computed = true;

    	// store original location to waypoint
    	GeocachingWaypoint waypoint = getWaypointByNamePrefix(toPoint, ORIGINAL_COORDINATES_WAYPOINT_PREFIX);
    	if (waypoint == null) {
    		waypoint = new GeocachingWaypoint();
    		toPoint.gcData.waypoints.add(waypoint);
    	}

    	waypoint.code = ORIGINAL_COORDINATES_WAYPOINT_PREFIX + toPoint.gcData.getCacheID().substring(2);
    	waypoint.type = GeocachingWaypoint.CACHE_WAYPOINT_TYPE_REFERENCE;
    	waypoint.name = mContext.getString(R.string.original_coordinates_name);
    	waypoint.lat = original.getLatitude();
    	waypoint.lon = original.getLongitude();


    	return toPoint;
    }

    protected static GeocachingWaypoint getWaypointByNamePrefix(Waypoint fromPoint, String prefix) {
    	if (fromPoint.gcData == null)
    		return null;

    	for (GeocachingWaypoint waypoint : fromPoint.gcData.waypoints) {
    		if (waypoint.code != null && waypoint.code.startsWith(prefix)) {
    			return waypoint;
    		}
    	}

    	return null;
    }
 }
