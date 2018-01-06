package locus.api.mapper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.arcao.geocaching.api.data.Geocache;
import com.arcao.geocaching.api.data.ImageData;
import com.arcao.geocaching.api.data.UserWaypoint;
import com.arcao.geocaching.api.data.coordinates.Coordinates;
import com.arcao.geocaching.api.data.coordinates.CoordinatesParser;
import com.arcao.geocaching.api.data.type.AttributeType;
import com.arcao.geocaching.api.data.type.ContainerType;
import com.arcao.geocaching.api.data.type.GeocacheType;
import com.arcao.geocaching.api.data.type.WaypointType;
import com.arcao.geocaching.api.util.GeocachingUtils;
import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.R;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import locus.api.objects.extra.Location;
import locus.api.objects.extra.Waypoint;
import locus.api.objects.geocaching.GeocachingAttribute;
import locus.api.objects.geocaching.GeocachingData;
import timber.log.Timber;

import static locus.api.mapper.Util.applyUnavailabilityForGeocache;
import static locus.api.mapper.Util.safeDateLong;

final class GeocacheConverter {
    private static final String GEOCACHE_GUID_LINK_PREFIX = "http://www.geocaching.com/seek/cache_details.aspx?guid=";
    private static final String LITE_GEOCACHE_LISTING_HTML = "<meta http-equiv=\"refresh\" content=\"0;url=%1$s#ctl00_ContentBody_ShortDescription\" />"
            + "<p><a href=\"%1$s#ctl00_ContentBody_ShortDescription\">%2$s</a></p>";
    private static final long WAYPOINT_BASE_ID = GeocachingUtils.base31Decode("N0");

    private static final DateFormat
            GPX_TIME_FMT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
    private static final Pattern
            FINAL_WAYPOINT_NAME_PATTERN = Pattern.compile("fin[a|á]+[l|ł]", Pattern.CASE_INSENSITIVE);

    private static final Pattern NOTE__COORDINATE_PATTERN = Pattern.compile("\\b[nNsS]\\s*\\d"); // begin of coordinates
    private static final Pattern NOTE__NAME_PATTERN = Pattern.compile("^(.+):\\s*\\z");

    static {
        GPX_TIME_FMT.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
    }


    private final Context context;
    private final boolean premiumMember;
    private final SharedPreferences preferences;

    private final ImageDataConverter imageDataConverter;
    private final GeocacheLogConverter geocacheLogConverter;
    private final TrackableConverter trackableConverter;
    private final WaypointConverter waypointConverter;

    GeocacheConverter(@NonNull Context context) {
        this.context = context.getApplicationContext();
        premiumMember = App.get(this.context).getAccountManager().isPremium();
        preferences = PreferenceManager.getDefaultSharedPreferences(this.context);

        imageDataConverter = new ImageDataConverter();
        geocacheLogConverter = new GeocacheLogConverter(imageDataConverter);
        trackableConverter = new TrackableConverter();
        waypointConverter = new WaypointConverter();
    }

    GeocacheLogConverter getGeocacheLogConverter() {
        return geocacheLogConverter;
    }

    TrackableConverter getTrackableConverter() {
        return trackableConverter;
    }

    @Nullable
    Waypoint createLocusWaypoint(@Nullable Geocache cache) {
        if (cache == null)
            return null;

        Location loc = new Location(cache.code());
        loc.setLatitude(cache.coordinates().latitude());
        loc.setLongitude(cache.coordinates().longitude());

        Waypoint p = new Waypoint(cache.name(), loc);

        GeocachingData d = new GeocachingData();
        d.setCacheID(cache.code());
        d.setId(cache.id());
        d.setName(cache.name());
        d.setType(getLocusCacheType(cache.geocacheType()));
        d.setDifficulty(cache.difficulty());
        d.setTerrain(cache.terrain());
        if (cache.owner() != null) {
            d.setOwner(cache.owner().userName());
        }
        d.setPlacedBy(cache.placedBy());
        d.setAvailable(cache.available());
        d.setArchived(cache.archived());
        d.setPremiumOnly(cache.premium());
        if (cache.guid() != null) {
            d.setCacheUrl(GEOCACHE_GUID_LINK_PREFIX + cache.guid());
        }

        d.setDateHidden(safeDateLong(cache.placeDate()));
        d.setDatePublished(safeDateLong(cache.publishDate()));
        d.setDateUpdated(safeDateLong(cache.lastUpdateDate()));

        d.setContainer(getLocusContainerType(cache.containerType()));
        d.setFound(cache.foundByUser());

        d.setCountry(cache.countryName());
        d.setState(cache.stateName());

        d.setDescriptions(BadBBCodeFixer.fix(cache.shortDescription()), cache.shortDescriptionHtml(),
                BadBBCodeFixer.fix(cache.longDescription()), cache.longDescriptionHtml());
        d.setEncodedHints(cache.hint());
        d.setNotes(cache.personalNote());
        d.setFavoritePoints(cache.favoritePoints());


        for (ImageData image : CollectionUtils.emptyIfNull(cache.images())) {
            d.addImage(imageDataConverter.createLocusGeocachingImage(image));
        }

        for (AttributeType attribute : CollectionUtils.emptyIfNull(cache.attributes())) {
            if (attribute == null)
                continue;

            d.attributes.add(new GeocachingAttribute(attribute.id, attribute.on));
        }

        p.gcData = d;

        waypointConverter.addWaypoints(p, cache.waypoints());
        waypointConverter.addWaypoints(p, Collections.singletonList(getCorrectedCoordinateWaypoint(cache)));
        waypointConverter.addWaypoints(p, getWaypointsFromNote(cache));
        geocacheLogConverter.addGeocacheLogs(p, cache.geocacheLogs());
        trackableConverter.addTrackables(p, cache.trackables());

        updateGeocacheLocationByCorrectedCoordinates(p, cache.userWaypoints());

        if (!premiumMember)
            applyListingForBasicMembers(p);

        applyUnavailabilityForGeocache(preferences, p);

        return p;
    }

    private int getLocusCacheType(@Nullable GeocacheType cacheType) {
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

    private int getLocusContainerType(@Nullable ContainerType containerType) {
        if (containerType == null)
            return GeocachingData.CACHE_SIZE_NOT_CHOSEN;

        switch (containerType) {
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

    private void applyListingForBasicMembers(@NonNull Waypoint p) {
        if (p.gcData == null)
            return;

        String longDescription = String.format(LITE_GEOCACHE_LISTING_HTML, p.gcData.getCacheUrlFull(), p.getName());
        p.gcData.setDescriptions("", false, longDescription, true);
    }


    private void updateGeocacheLocationByCorrectedCoordinates(@NonNull Waypoint p, @Nullable Collection<UserWaypoint> userWaypoints) {
        if (p.gcData == null || CollectionUtils.isEmpty(userWaypoints))
            return;


        // find corrected coordinate user waypoint
        UserWaypoint correctedCoordinateUserWaypoint = null;
        for (UserWaypoint w : userWaypoints) {
            if (w.correctedCoordinate()) {
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

        // update coordinates to new location
        Location newLocation = new Location(location.getProvider());
        newLocation.setLatitude(correctedCoordinateUserWaypoint.coordinates().latitude());
        newLocation.setLongitude(correctedCoordinateUserWaypoint.coordinates().longitude());
        location.set(newLocation);
    }

    @Nullable
    private com.arcao.geocaching.api.data.Waypoint getCorrectedCoordinateWaypoint(@NonNull Geocache geocache) {
        final Collection<UserWaypoint> userWaypoints = geocache.userWaypoints();
        final String cacheCode = geocache.code();

        if (CollectionUtils.isEmpty(userWaypoints))
            return null;

        for (UserWaypoint uw : userWaypoints) {
            if (uw.correctedCoordinate()) {

                final String name = context.getString(R.string.var_final_location_name);
                final String waypointCode = GeocachingUtils.base31Encode(WAYPOINT_BASE_ID) + cacheCode.substring(2);

                return com.arcao.geocaching.api.data.Waypoint.builder()
                        .coordinates(uw.coordinates())
                        .time(new Date())
                        .waypointCode(waypointCode)
                        .name(name)
                        .note(uw.description())
                        .waypointType(WaypointType.FinalLocation)
                        .build();
            }
        }

        return null;
    }

    @Nullable
    private Collection<com.arcao.geocaching.api.data.Waypoint> getWaypointsFromNote(@NonNull Geocache geocache) {
        String note = geocache.personalNote();
        final String cacheCode = geocache.code();

        if (StringUtils.isBlank(note))
            return null;

        Collection<com.arcao.geocaching.api.data.Waypoint> res = new ArrayList<>();

        int count = 0;
        int nameCount = 0;
        StringBuilder namePrefix = new StringBuilder();

        Matcher matcher = NOTE__COORDINATE_PATTERN.matcher(note);
        while (matcher.find()) {
            try {
                final Coordinates point = CoordinatesParser.parse(note.substring(matcher.start()));
                count++;

                String name = namePrefix + note.substring(0, matcher.start());

                // name can contains more lines, use the last one for name only
                int lastLineEnd = name.lastIndexOf('\n');
                if (lastLineEnd != -1)
                    name = name.substring(lastLineEnd + 1);

                Matcher nameMatcher = NOTE__NAME_PATTERN.matcher(name);

                WaypointType waypointType = WaypointType.ReferencePoint;

                if (nameMatcher.find() && !nameMatcher.group(1).trim().isEmpty()) {
                    name = nameMatcher.group(1).trim();

                    if (FINAL_WAYPOINT_NAME_PATTERN.matcher(name).matches()) {
                        waypointType = WaypointType.FinalLocation;
                    }
                } else {
                    nameCount++;
                    name = context.getString(R.string.var_user_waypoint_name, nameCount);
                }

                final String code = GeocachingUtils.base31Encode(WAYPOINT_BASE_ID + count) + cacheCode.substring(2);

                res.add(com.arcao.geocaching.api.data.Waypoint.builder()
                        .coordinates(point)
                        .time(new Date())
                        .waypointCode(code)
                        .name(name)
                        .note("")
                        .waypointType(waypointType)
                        .build());

                namePrefix.setLength(0);
            } catch (ParseException e) {
                Timber.w(e);

                // fix for "S1: N 49 ..."
                namePrefix.append(note.substring(0, matcher.start() + 1));
            }

            note = note.substring(matcher.start() + 1);
            matcher = NOTE__COORDINATE_PATTERN.matcher(note);
        }

        return res;
    }

}
