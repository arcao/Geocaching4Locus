package locus.api.mapper

import android.content.Context
import android.preference.PreferenceManager
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.arcao.geocaching.api.data.Geocache
import com.arcao.geocaching.api.data.UserWaypoint
import com.arcao.geocaching.api.data.Waypoint
import com.arcao.geocaching.api.data.coordinates.CoordinatesParser
import com.arcao.geocaching.api.data.type.ContainerType
import com.arcao.geocaching.api.data.type.GeocacheType
import com.arcao.geocaching.api.data.type.WaypointType
import com.arcao.geocaching.api.util.GeocachingUtils
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.base.constants.PrefConstants
import locus.api.mapper.Util.applyUnavailabilityForGeocache
import locus.api.objects.extra.Location
import locus.api.objects.extra.Point
import locus.api.objects.geocaching.GeocachingAttribute
import locus.api.objects.geocaching.GeocachingData
import locus.api.utils.toTime
import timber.log.Timber
import java.text.ParseException
import java.util.Date
import java.util.regex.Pattern

class GeocacheConverter(context: Context) {
    private val context: Context = context.applicationContext

    private val imageDataConverter = ImageDataConverter()
    val geocacheLogConverter = GeocacheLogConverter(imageDataConverter)
    val trackableConverter = TrackableConverter()
    private val waypointConverter = WaypointConverter()

    private val disableDnfNmNaGeocaches: Boolean
    private val disableDnfNmNaGeocachesThreshold: Int

    init {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
        disableDnfNmNaGeocaches = preferences.getBoolean(PrefConstants.DOWNLOADING_DISABLE_DNF_NM_NA_CACHES, false)
        disableDnfNmNaGeocachesThreshold = preferences.getInt(PrefConstants.DOWNLOADING_DISABLE_DNF_NM_NA_CACHES_LOGS_COUNT, 1)
    }

    fun createLocusPoint(cache: Geocache): Point {
        val loc = Location()
                .setLatitude(cache.coordinates().latitude())
                .setLongitude(cache.coordinates().longitude())

        val p = Point(cache.name(), loc).apply {
            gcData = GeocachingData().apply {
                cacheID = cache.code()
                id = cache.id()
                name = cache.name()
                type = getLocusCacheType(cache.geocacheType())
                difficulty = cache.difficulty()
                terrain = cache.terrain()
                owner = cache.owner()?.userName()
                placedBy = cache.placedBy()
                isAvailable = cache.available()
                isArchived = cache.archived()
                isPremiumOnly = cache.premium()
                if (cache.guid() != null) {
                    cacheUrl = GEOCACHE_GUID_LINK_PREFIX + cache.guid()
                }

                dateHidden = cache.placeDate().toTime()
                datePublished = cache.publishDate().toTime()
                dateUpdated = cache.lastUpdateDate().toTime()

                container = getLocusContainerType(cache.containerType())
                isFound = cache.foundByUser()

                country = cache.countryName()
                state = cache.stateName()

                setDescriptions(BadBBCodeFixer.fix(cache.shortDescription()), cache.shortDescriptionHtml(),
                        BadBBCodeFixer.fix(cache.longDescription()), cache.longDescriptionHtml())
                encodedHints = cache.hint()
                notes = cache.personalNote()
                favoritePoints = cache.favoritePoints()

                for (image in cache.images().orEmpty()) {
                    addImage(imageDataConverter.createLocusGeocachingImage(image))
                }

                for (attribute in cache.attributes().orEmpty()) {
                    if (attribute != null)
                        attributes.add(GeocachingAttribute(attribute.id, attribute.on))
                }
            }
        }

        waypointConverter.addWaypoints(p, cache.waypoints()!!)
        waypointConverter.addWaypoints(p, listOf(getCorrectedCoordinateWaypoint(cache)))
        waypointConverter.addWaypoints(p, getWaypointsFromNote(cache))
        geocacheLogConverter.addGeocacheLogs(p, cache.geocacheLogs()!!)
        trackableConverter.addTrackables(p, cache.trackables()!!)

        updateGeocacheLocationByCorrectedCoordinates(p, cache.userWaypoints())

        if (disableDnfNmNaGeocaches)
            applyUnavailabilityForGeocache(p, disableDnfNmNaGeocachesThreshold)

        return p
    }

    private fun getLocusCacheType(@Nullable cacheType: GeocacheType?): Int {
        return when (cacheType) {
            GeocacheType.CacheInTrashOutEvent -> GeocachingData.CACHE_TYPE_CACHE_IN_TRASH_OUT
            GeocacheType.Earth -> GeocachingData.CACHE_TYPE_EARTH
            GeocacheType.Event -> GeocachingData.CACHE_TYPE_EVENT
            GeocacheType.GpsAdventuresExhibit -> GeocachingData.CACHE_TYPE_GPS_ADVENTURE
            GeocacheType.GroundspeakBlockParty -> GeocachingData.CACHE_TYPE_GROUNDSPEAK
            GeocacheType.GroudspeakHQ -> GeocachingData.CACHE_TYPE_GROUNDSPEAK
            GeocacheType.GroudspeakLostAndFoundCelebration -> GeocachingData.CACHE_TYPE_LF_CELEBRATION
            GeocacheType.LetterboxHybrid -> GeocachingData.CACHE_TYPE_LETTERBOX
            GeocacheType.Locationless -> GeocachingData.CACHE_TYPE_LOCATIONLESS
            GeocacheType.LostAndFoundEvent -> GeocachingData.CACHE_TYPE_LF_EVENT
            GeocacheType.MegaEvent -> GeocachingData.CACHE_TYPE_MEGA_EVENT
            GeocacheType.Multi -> GeocachingData.CACHE_TYPE_MULTI
            GeocacheType.ProjectApe -> GeocachingData.CACHE_TYPE_PROJECT_APE
            GeocacheType.Traditional -> GeocachingData.CACHE_TYPE_TRADITIONAL
            GeocacheType.Mystery -> GeocachingData.CACHE_TYPE_MYSTERY
            GeocacheType.Virtual -> GeocachingData.CACHE_TYPE_VIRTUAL
            GeocacheType.Webcam -> GeocachingData.CACHE_TYPE_WEBCAM
            GeocacheType.Wherigo -> GeocachingData.CACHE_TYPE_WHERIGO
            GeocacheType.GigaEvent -> GeocachingData.CACHE_TYPE_GIGA_EVENT
            else -> GeocachingData.CACHE_TYPE_UNDEFINED
        }
    }

    private fun getLocusContainerType(@Nullable containerType: ContainerType?): Int {
        return when (containerType) {
            ContainerType.Huge -> GeocachingData.CACHE_SIZE_HUGE
            ContainerType.Large -> GeocachingData.CACHE_SIZE_LARGE
            ContainerType.Micro -> GeocachingData.CACHE_SIZE_MICRO
            ContainerType.NotChosen -> GeocachingData.CACHE_SIZE_NOT_CHOSEN
            ContainerType.Other -> GeocachingData.CACHE_SIZE_OTHER
            ContainerType.Regular -> GeocachingData.CACHE_SIZE_REGULAR
            ContainerType.Small -> GeocachingData.CACHE_SIZE_SMALL
            else -> GeocachingData.CACHE_SIZE_NOT_CHOSEN
        }
    }

    private fun updateGeocacheLocationByCorrectedCoordinates(@NonNull p: Point, @Nullable userWaypoints: Collection<UserWaypoint>?) {
        if (p.gcData == null || userWaypoints?.isEmpty() != false)
            return

        // find corrected coordinate user waypoint
        var correctedCoordinateUserWaypoint: UserWaypoint? = null
        for (w in userWaypoints) {
            if (w.correctedCoordinate()) {
                correctedCoordinateUserWaypoint = w
                break
            }
        }

        // continue only if something was found
        if (correctedCoordinateUserWaypoint == null)
            return

        val location = p.location

        p.gcData.apply {
            isComputed = true
            latOriginal = location.getLatitude()
            lonOriginal = location.getLongitude()
        }

        // update coordinates to new location
        location.set(
                Location()
                        .setLatitude(correctedCoordinateUserWaypoint.coordinates().latitude())
                        .setLongitude(correctedCoordinateUserWaypoint.coordinates().longitude())
        )
    }

    @Nullable
    private fun getCorrectedCoordinateWaypoint(@NonNull geocache: Geocache): com.arcao.geocaching.api.data.Waypoint? {
        val userWaypoints = geocache.userWaypoints()
        val cacheCode = geocache.code()

        if (userWaypoints?.isEmpty() != false)
            return null

        for (uw in userWaypoints) {
            if (uw.correctedCoordinate()) {

                val name = context.getString(R.string.var_final_location_name)
                val waypointCode = GeocachingUtils.base31Encode(WAYPOINT_BASE_ID) + cacheCode.substring(2)

                return Waypoint.builder()
                        .coordinates(uw.coordinates())
                        .time(Date())
                        .waypointCode(waypointCode)
                        .name(name)
                        .note(uw.description())
                        .waypointType(WaypointType.FinalLocation)
                        .build()
            }
        }

        return null
    }

    @Nullable
    private fun getWaypointsFromNote(@NonNull geocache: Geocache): Collection<com.arcao.geocaching.api.data.Waypoint?>? {
        var note = geocache.personalNote().orEmpty()
        val cacheCode = geocache.code()

        if (note.isBlank())
            return null

        val list = ArrayList<Waypoint>()

        var count = 0
        var nameCount = 0
        val namePrefix = StringBuilder()

        var matcher = NOTE_COORDINATE_PATTERN.matcher(note)
        while (matcher.find()) {
            try {
                val point = CoordinatesParser.parse(note.substring(matcher.start()))
                count++

                var name = namePrefix.toString() + note.substring(0, matcher.start())

                // name can contains more lines, use the last one for name only
                val lastLineEnd = name.lastIndexOf('\n')
                if (lastLineEnd != -1)
                    name = name.substring(lastLineEnd + 1)

                val nameMatcher = NOTE_NAME_PATTERN.matcher(name)

                var waypointType = WaypointType.ReferencePoint

                if (nameMatcher.find() && !nameMatcher.group(1).trim { it <= ' ' }.isEmpty()) {
                    name = nameMatcher.group(1).trim { it <= ' ' }

                    if (FINAL_WAYPOINT_NAME_PATTERN.matcher(name).matches()) {
                        waypointType = WaypointType.FinalLocation
                    }
                } else {
                    nameCount++
                    name = context.getString(R.string.var_user_waypoint_name, nameCount)
                }

                val code = GeocachingUtils.base31Encode(WAYPOINT_BASE_ID + count) + cacheCode.substring(2)

                list.add(Waypoint.builder()
                        .coordinates(point)
                        .time(Date())
                        .waypointCode(code)
                        .name(name)
                        .note("")
                        .waypointType(waypointType)
                        .build())

                namePrefix.setLength(0)
            } catch (e: ParseException) {
                Timber.w(e)

                // fix for "S1: N 49 ..."
                namePrefix.append(note.substring(0, matcher.start() + 1))
            }

            note = note.substring(matcher.start() + 1)
            matcher = NOTE_COORDINATE_PATTERN.matcher(note)
        }

        return list
    }

    companion object {
        private const val GEOCACHE_GUID_LINK_PREFIX = "http://www.geocaching.com/seek/cache_details.aspx?guid="
        private val WAYPOINT_BASE_ID = GeocachingUtils.base31Decode("N0")

        private val FINAL_WAYPOINT_NAME_PATTERN = Pattern.compile("fin[a|á]+[l|ł]", Pattern.CASE_INSENSITIVE)

        private val NOTE_COORDINATE_PATTERN = Pattern.compile("\\b[nNsS]\\s*\\d") // begin of coordinates
        private val NOTE_NAME_PATTERN = Pattern.compile("^(.+):\\s*\\z")
    }
}
