package locus.api.mapper

import android.content.Context
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.data.api.model.AdditionalWaypoint
import com.arcao.geocaching4locus.data.api.model.Geocache
import com.arcao.geocaching4locus.data.api.model.GeocacheSize
import com.arcao.geocaching4locus.data.api.model.GeocacheType
import com.arcao.geocaching4locus.data.api.model.enums.AdditionalWaypointType
import com.arcao.geocaching4locus.data.api.model.enums.GeocacheStatus
import com.arcao.geocaching4locus.data.api.util.CoordinatesParser
import com.arcao.geocaching4locus.data.api.util.ReferenceCode
import com.arcao.geocaching4locus.settings.manager.DefaultPreferenceManager
import locus.api.mapper.Util.applyUnavailabilityForGeocache
import locus.api.objects.extra.Location
import locus.api.objects.geoData.Point
import locus.api.objects.geocaching.GeocachingAttribute
import locus.api.objects.geocaching.GeocachingData
import locus.api.objects.geocaching.GeocachingImage
import timber.log.Timber
import java.text.ParseException
import java.util.regex.Pattern

class GeocacheConverter(
    private val context: Context,
    private val defaultPreferenceManager: DefaultPreferenceManager,
    private val geocacheLogConverter: GeocacheLogConverter,
    private val imageDataConverter: ImageDataConverter,
    private val trackableConverter: TrackableConverter,
    private val waypointConverter: WaypointConverter
) {
    fun createLocusPoint(cache: Geocache): Point {
        val loc = Location().apply {
            latitude = requireNotNull(cache.postedCoordinates?.latitude) { "Coordinates missing" }
            longitude = requireNotNull(cache.postedCoordinates?.longitude) { "Coordinates missing" }
        }

        val p = Point(cache.name, loc).apply {
            gcData = GeocachingData().apply {
                source = GeocachingData.CACHE_SOURCE_GEOCACHING_COM
                cacheID = cache.referenceCode
                id = cache.id
                name = cache.name
                type = cache.geocacheType.toLocusMapGeocacheType()
                difficulty = cache.difficulty ?: 1F
                terrain = cache.terrain ?: 1F
                owner = cache.owner?.username ?: cache.ownerAlias.orEmpty()
                placedBy = cache.ownerAlias.orEmpty()
                isAvailable = cache.status == GeocacheStatus.ACTIVE
                isArchived = cache.status == GeocacheStatus.ARCHIVED
                isPremiumOnly = cache.isPremiumOnly ?: false
                cacheUrl = cache.url.orEmpty()

                dateHidden = cache.placedDateInstant?.toEpochMilli() ?: 0
                datePublished = cache.publishedDateInstant?.toEpochMilli() ?: 0
                dateUpdated = cache.lastVisitedDateInstant?.toEpochMilli() ?: 0

                container = cache.geocacheSize.getLocusMapGeocacheSize()
                isFound = cache.userData?.foundDate != null

                country = cache.location?.country.orEmpty()
                state = cache.location?.state.orEmpty()

                setDescriptions(
                    BadBBCodeFixer.fix(cache.shortDescription).orEmpty(),
                    cache.containsHtml ?: false,
                    BadBBCodeFixer.fix(cache.longDescription).orEmpty(),
                    cache.containsHtml ?: false
                )
                encodedHints = cache.hints.orEmpty()
                notes = cache.userData?.note.orEmpty()
                favoritePoints = cache.favoritePoints ?: 0

                images = mutableListOf<GeocachingImage>().apply {
                    for (image in cache.images.orEmpty()) {
                        imageDataConverter.createLocusGeocachingImage(image)?.let(this::add)
                    }
                }

                for (attribute in cache.attributes.orEmpty()) {
                    attributes.add(GeocachingAttribute(attribute.id, attribute.isOn))
                }
            }
        }

        waypointConverter.addWaypoints(p, cache.additionalWaypoints, cache.id)
        waypointConverter.addWaypoints(p, listOf(getCorrectedCoordinateWaypoint(cache)), cache.id)
        waypointConverter.addWaypoints(p, getWaypointsFromNote(cache), cache.id)
        geocacheLogConverter.addGeocacheLogs(p, cache.geocacheLogs.orEmpty())
        trackableConverter.addTrackables(p, cache.trackables.orEmpty())

        updateGeocacheLocationByCorrectedCoordinates(p, cache)

        if (defaultPreferenceManager.disableDnfNmNaGeocaches)
            applyUnavailabilityForGeocache(
                p,
                defaultPreferenceManager.disableDnfNmNaGeocachesThreshold
            )

        return p
    }

    private fun GeocacheType?.toLocusMapGeocacheType(): Int {
        return when (this?.id ?: 0) {
            GeocacheType.CACHE_IN_TRASH_OUT_EVENT -> GeocachingData.CACHE_TYPE_CACHE_IN_TRASH_OUT
            GeocacheType.EARTHCACHE -> GeocachingData.CACHE_TYPE_EARTH
            GeocacheType.EVENT -> GeocachingData.CACHE_TYPE_EVENT
            GeocacheType.GPS_ADVENTURES_EXHIBIT -> GeocachingData.CACHE_TYPE_GPS_ADVENTURE
            GeocacheType.GEOCACHING_BLOCK_PARTY -> GeocachingData.CACHE_TYPE_GC_HQ
            GeocacheType.GEOCACHING_HQ -> GeocachingData.CACHE_TYPE_GC_HQ
            GeocacheType.GEOCACHING_LOST_AND_FOUND_CELEBRATION -> GeocachingData.CACHE_TYPE_GC_HQ_CELEBRATION
            GeocacheType.LETTERBOX_HYBRID -> GeocachingData.CACHE_TYPE_LETTERBOX
            GeocacheType.LOCATIONLESS_CACHE -> GeocachingData.CACHE_TYPE_LOCATIONLESS
            GeocacheType.LOST_AND_FOUND_EVENT_CACHE -> GeocachingData.CACHE_TYPE_COMMUNITY_CELEBRATION
            GeocacheType.MEGA_EVENT -> GeocachingData.CACHE_TYPE_MEGA_EVENT
            GeocacheType.MULTI_CACHE -> GeocachingData.CACHE_TYPE_MULTI
            GeocacheType.PROJECT_APE -> GeocachingData.CACHE_TYPE_PROJECT_APE
            GeocacheType.TRADITIONAL -> GeocachingData.CACHE_TYPE_TRADITIONAL
            GeocacheType.MYSTERY_UNKNOWN -> GeocachingData.CACHE_TYPE_MYSTERY
            GeocacheType.VIRTUAL -> GeocachingData.CACHE_TYPE_VIRTUAL
            GeocacheType.WEBCAM -> GeocachingData.CACHE_TYPE_WEBCAM
            GeocacheType.WHERIGO -> GeocachingData.CACHE_TYPE_WHERIGO
            GeocacheType.GIGA_EVENT -> GeocachingData.CACHE_TYPE_GIGA_EVENT
            else -> GeocachingData.CACHE_TYPE_UNDEFINED
        }
    }

    private fun GeocacheSize?.getLocusMapGeocacheSize(): Int {
        return when (this?.id ?: 0) {
            GeocacheSize.LARGE -> GeocachingData.CACHE_SIZE_LARGE
            GeocacheSize.MICRO -> GeocachingData.CACHE_SIZE_MICRO
            GeocacheSize.NOT_CHOSEN -> GeocachingData.CACHE_SIZE_NOT_CHOSEN
            GeocacheSize.OTHER -> GeocachingData.CACHE_SIZE_OTHER
            GeocacheSize.REGULAR -> GeocachingData.CACHE_SIZE_REGULAR
            GeocacheSize.SMALL -> GeocachingData.CACHE_SIZE_SMALL
            else -> GeocachingData.CACHE_SIZE_OTHER
        }
    }

    private fun updateGeocacheLocationByCorrectedCoordinates(p: Point, cache: Geocache) {
        val correctedCoordinates = cache.userData?.correctedCoordinates
        if (p.gcData == null || correctedCoordinates == null)
            return

        val location = p.location

        p.gcData?.apply {
            isComputed = true
            latOriginal = location.latitude
            lonOriginal = location.longitude
        }

        // update coordinates to new location
        location.set(
            Location().apply {
                latitude = correctedCoordinates.latitude
                longitude = correctedCoordinates.longitude
            }
        )
    }

    private fun getCorrectedCoordinateWaypoint(geocache: Geocache): AdditionalWaypoint? {
        val correctedCoordinates = geocache.userData?.correctedCoordinates ?: return null

        val name = context.getString(R.string.var_final_location_name)

        return AdditionalWaypoint(
            coordinates = correctedCoordinates,
            name = name,
            prefix = "N0",
            description = null,
            type = AdditionalWaypointType.FINAL_LOCATION,
            url = null
        )
    }

    @Nullable
    private fun getWaypointsFromNote(@NonNull geocache: Geocache): Collection<AdditionalWaypoint>? {
        var note = geocache.userData?.note ?: return null

        if (note.isBlank())
            return null

        val list = mutableListOf<AdditionalWaypoint>()

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

                var waypointType = AdditionalWaypointType.REFERENCE_POINT

                if (nameMatcher.find() && nameMatcher.group(1).orEmpty().trim { it <= ' ' }
                        .isNotEmpty()) {
                    name = nameMatcher.group(1).orEmpty().trim { it <= ' ' }

                    if (FINAL_WAYPOINT_NAME_PATTERN.matcher(name).matches()) {
                        waypointType = AdditionalWaypointType.FINAL_LOCATION
                    }
                } else {
                    nameCount++
                    name = context.getString(R.string.var_user_waypoint_name, nameCount)
                }

                val prefix = ReferenceCode.base31Encode(WAYPOINT_BASE_ID + count)

                list.add(
                    AdditionalWaypoint(
                        coordinates = point,
                        description = null,
                        prefix = prefix,
                        name = name,
                        type = waypointType,
                        url = null
                    )
                )

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
        private val WAYPOINT_BASE_ID = ReferenceCode.base31Decode("N0")

        private val FINAL_WAYPOINT_NAME_PATTERN =
            Pattern.compile("fin[a|á]+[l|ł]", Pattern.CASE_INSENSITIVE)

        private val NOTE_COORDINATE_PATTERN =
            Pattern.compile("\\b[nNsS]\\s*\\d") // begin of coordinates
        private val NOTE_NAME_PATTERN = Pattern.compile("^(.+):\\s*\\z")
    }
}
