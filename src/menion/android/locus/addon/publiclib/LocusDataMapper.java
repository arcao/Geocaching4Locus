package menion.android.locus.addon.publiclib;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
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

import menion.android.locus.addon.publiclib.geoData.Point;
import menion.android.locus.addon.publiclib.geoData.PointGeocachingAttributes;
import menion.android.locus.addon.publiclib.geoData.PointGeocachingData;
import menion.android.locus.addon.publiclib.geoData.PointGeocachingDataLog;
import menion.android.locus.addon.publiclib.geoData.PointGeocachingDataTravelBug;
import menion.android.locus.addon.publiclib.geoData.PointGeocachingDataWaypoint;
import menion.android.locus.addon.publiclib.utils.RequiredVersionMissingException;
import net.sf.jtpl.Template;

import org.apache.commons.lang3.StringUtils;

import android.content.Context;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;

import com.arcao.geocaching.api.data.CacheLog;
import com.arcao.geocaching.api.data.Geocache;
import com.arcao.geocaching.api.data.ImageData;
import com.arcao.geocaching.api.data.SimpleGeocache;
import com.arcao.geocaching.api.data.Trackable;
import com.arcao.geocaching.api.data.User;
import com.arcao.geocaching.api.data.UserWaypoint;
import com.arcao.geocaching.api.data.Waypoint;
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
	
	public static List<Point> toLocusPoints(Context context, List<? extends SimpleGeocache> caches) {
		List<Point> points = new ArrayList<Point>();
		for (SimpleGeocache cache : caches) {
			points.add(toLocusPoint(context, cache));
		}
		
		return points;
	}
	
	public static Point toLocusPoint(Context context, SimpleGeocache cache) {
		if (cache == null)
			return null;
		
		Location loc = new Location(cache.getClass().getName());
		loc.setLatitude(cache.getLatitude());
		loc.setLongitude(cache.getLongitude());

		Point p = new Point(cache.getName(), loc);

		PointGeocachingData d = new PointGeocachingData();
		d.cacheID = cache.getCacheCode();

		long id = cache.getId();
		if (id < Integer.MAX_VALUE)
		  d.id = (int) id; 

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
		d.hidden = GPX_TIME_FMT.format(cache.getPlaced());
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
			d.notes = gc.getPersonalNote();

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

			for (Waypoint waypoint : getWaypointsFromUserWaypoints(context, gc.getUserWaypoints(), gc.getCacheCode())) {
				d.waypoints.add(toLocusWaypoint(waypoint));
			}
			
			for (Waypoint waypoint : getWaypointsFromNote(context, gc.getPersonalNote(), gc.getCacheCode())) {
				d.waypoints.add(toLocusWaypoint(waypoint));
			}
		}
		
		p.setGeocachingData(d);

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
      File locusBasePath = new File(LocusIntents.getLocusRootDirectory(context));
      
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
	

	protected static void updateCacheLocationByCorrectedCoordinates(Context mContext, Point p, List<UserWaypoint> userWaypoints) {
		if (userWaypoints.size() != 1)
			return;

		Location original = p.getLocation();
		UserWaypoint userWaypoint = userWaypoints.get(0);

		Location loc = new Location(userWaypoint.getClass().getName());
		loc.setLatitude(userWaypoint.getLatitude());
		loc.setLongitude(userWaypoint.getLongitude());
		p.getLocation().set(loc);

		p.getGeocachingData().computed = true;

		// store original location to waypoint
		PointGeocachingDataWaypoint waypoint = getWaypointByNamePrefix(p, ORIGINAL_COORDINATES_WAYPOINT_PREFIX);
		if (waypoint == null) {
			waypoint = new PointGeocachingDataWaypoint();
			p.getGeocachingData().waypoints.add(waypoint);
		}

		waypoint.code = ORIGINAL_COORDINATES_WAYPOINT_PREFIX + p.getGeocachingData().cacheID.substring(2);
		waypoint.type = PointGeocachingData.CACHE_WAYPOINT_TYPE_REFERENCE;
		waypoint.name = mContext.getString(R.string.original_coordinates_name);
		waypoint.lat = original.getLatitude();
		waypoint.lon = original.getLongitude();
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
		//t.origin = 
		if (trackable.getOwner() != null) {
			t.owner = trackable.getOwner().getUserName();
		}
		t.released = GPX_TIME_FMT.format(trackable.getCreated()); 
		t.srcDetails = String.format(TRACKABLE_URL, trackable.getTrackingNumber());
		return t;
	}

	protected static PointGeocachingDataLog toLocusCacheLog(CacheLog log) {
		PointGeocachingDataLog l = new PointGeocachingDataLog();
		
		long id = log.getId();
		if (id < Integer.MAX_VALUE) {
			l.id = (int) id;
		}
		
		l.date = GPX_TIME_FMT.format(log.getVisited());
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
			case GroundspeakBlockParty:
				return PointGeocachingData.CACHE_TYPE_GROUNDSPEAK;
			case GroudspeakHQ:
				return PointGeocachingData.CACHE_TYPE_GROUNDSPEAK;
			case GroudspeakLostAndFoundCelebration:
				return PointGeocachingData.CACHE_TYPE_LF_CELEBRATION;
			case LetterboxHybrid:
				return PointGeocachingData.CACHE_TYPE_LETTERBOX;
			case Locationless:
				return PointGeocachingData.CACHE_TYPE_LOCATIONLESS;
			case LostAndFoundEvent:
				return PointGeocachingData.CACHE_TYPE_LF_EVENT;
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
				return PointGeocachingData.CACHE_SIZE_OTHER;
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
	
    public static List<Waypoint> getWaypointsFromNote(Context context, String note, String cacheCode) {
    	List<Waypoint> res = new ArrayList<Waypoint>();
    	
    	if (StringUtils.isBlank(note)) {
            return res;
        }
        
        final Pattern coordPattern = Pattern.compile("\\b[nNsS]{1}\\s*\\d"); // begin of coordinates
        final Pattern namePattern = Pattern.compile("^(.+):\\s*\\z");
        final int waypointBaseId = (int) GeocachingUtils.base31Decode("N0");

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
                
                res.add(new Waypoint(point, new Date(), code, name, "", WaypointType.ReferencePoint));
                
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
    
    public static List<Waypoint> getWaypointsFromUserWaypoints(Context context, List<UserWaypoint> userWaypoints, String cacheCode) {
    	List<Waypoint> res = new ArrayList<Waypoint>();
    	
    	if (userWaypoints.size() == 0)
    		return res;

    	final int waypointBaseId = (int) GeocachingUtils.base31Decode("N0");
    	
    	int count = 1;
    	for (UserWaypoint uw : userWaypoints) {
    		final String name = context.getString(R.string.final_location_name, count);
    		final String waypointCode = GeocachingUtils.base31Encode(waypointBaseId + count) + cacheCode.substring(2);

    		res.add(new Waypoint(uw.getCoordinates(), new Date(), waypointCode, name, uw.getDescription(), WaypointType.FinalLocation));
    		count++;
    	}
          	
    	return res;
    }
    
    public static Point mergePoints(Context mContext, Point toPoint, Point fromPoint) {
    	if (fromPoint == null || fromPoint.getGeocachingData() == null)
    		return toPoint;
    	
    	fixArchivedCacheLocation(mContext, toPoint, fromPoint);
    	mergeCacheLogs(mContext, toPoint, fromPoint);
    	fixComputedCoordinates(mContext, toPoint, fromPoint);
    	clearExtraOnDisplayCallback(toPoint);
    	
    	return toPoint;
    }    
    
    public static void clearExtraOnDisplayCallback(Point p) {
    	try {
				Field field = Point.class.getDeclaredField("mExtraOnDisplay");
				field.setAccessible(true);
				field.set(p, "clear;;;;;");
			} catch (Exception e) {
				Log.e(TAG, "Clearing ExtraOnDisplay callback failed.", e);
			}
    	
    }

		public static Point mergeCacheLogs(Context mContext, Point toPoint, Point fromPoint) {
    	// issue #14: Keep cache logs from GSAK when updating cache
    	if (fromPoint.getGeocachingData().logs.size() == 0) 
    		return toPoint;
    	
    	for(PointGeocachingDataLog fromLog : new ReverseListIterator<PointGeocachingDataLog>(fromPoint.getGeocachingData().logs)) {
    		if (GSAK_USERNAME.equalsIgnoreCase(fromLog.finder)) {
    			fromLog.date = GPX_TIME_FMT.format(new Date());
    			toPoint.getGeocachingData().logs.add(0, fromLog);
    		}
    	}
    	
    	return toPoint;
    }
    
    public static Point fixArchivedCacheLocation(Context mContext, Point toPoint, Point fromPoint) {
    	// issue #13: Use old coordinates when cache is archived after update
    	if (!toPoint.getGeocachingData().archived || (fromPoint.getLocation().getLatitude() == 0 && fromPoint.getLocation().getLongitude() == 0) 
    			|| Double.isNaN(fromPoint.getLocation().getLatitude()) || Double.isNaN(fromPoint.getLocation().getLongitude())
    			|| fromPoint.getGeocachingData().computed) 
    		return toPoint;

    	toPoint.getLocation().setLatitude(fromPoint.getLocation().getLatitude());
    	toPoint.getLocation().setLongitude(fromPoint.getLocation().getLongitude());

    	return toPoint;
    }

    public static Point fixComputedCoordinates(Context mContext, Point toPoint, Point fromPoint) {
    	if (!fromPoint.getGeocachingData().computed || toPoint.getGeocachingData().computed)
    		return toPoint;

    	Location original = toPoint.getLocation();

    	toPoint.getLocation().set(fromPoint.getLocation());
    	toPoint.getGeocachingData().computed = true;

    	// store original location to waypoint
    	PointGeocachingDataWaypoint waypoint = getWaypointByNamePrefix(toPoint, ORIGINAL_COORDINATES_WAYPOINT_PREFIX);
    	if (waypoint == null) {
    		waypoint = new PointGeocachingDataWaypoint();
    		toPoint.getGeocachingData().waypoints.add(waypoint);
    	}

    	waypoint.code = ORIGINAL_COORDINATES_WAYPOINT_PREFIX + toPoint.getGeocachingData().cacheID.substring(2);
    	waypoint.type = PointGeocachingData.CACHE_WAYPOINT_TYPE_REFERENCE;
    	waypoint.name = mContext.getString(R.string.original_coordinates_name);
    	waypoint.lat = original.getLatitude();
    	waypoint.lon = original.getLongitude();


    	return toPoint;
    }

    protected static PointGeocachingDataWaypoint getWaypointByNamePrefix(Point fromPoint, String prefix) {
    	if (fromPoint.getGeocachingData() == null)
    		return null;

    	for (PointGeocachingDataWaypoint waypoint : fromPoint.getGeocachingData().waypoints) {
    		if (waypoint.code != null && waypoint.code.startsWith(prefix)) {
    			return waypoint;
    		}
    	}

    	return null;
    }
 }
