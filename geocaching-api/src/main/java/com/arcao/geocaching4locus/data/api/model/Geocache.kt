package com.arcao.geocaching4locus.data.api.model

import com.arcao.geocaching4locus.data.api.model.enums.GeocacheStatus
import com.arcao.geocaching4locus.data.api.util.ReferenceCode
import com.arcao.geocaching4locus.data.api.util.toSafeInstant
import org.threeten.bp.LocalDateTime

data class Geocache(
    val referenceCode: String, // string
    val name: String, // string
    val difficulty: Float, // 0
    val terrain: Float, // 0
    val favoritePoints: Int?, // 0
    val trackableCount: Int?, // 0
    val placedDate: LocalDateTime?, // 2018-06-06T06:16:54.160
    val publishedDate: LocalDateTime?, // 2018-06-06T06:16:54.165
    val geocacheType: GeocacheType, // Traditional
    val geocacheSize: GeocacheSize, // Unknown
    val userData: UserData?,
    val status: GeocacheStatus, // Unpublished
    val location: Location?,
    val postedCoordinates: Coordinates,
    val lastVisitedDate: LocalDateTime?, // 2018-06-06T06:16:54.165
    val ownerCode: String?, // string
    val ownerAlias: String, // string
    val isPremiumOnly: Boolean, // true
    val shortDescription: String?, // string
    val longDescription: String?, // string
    val hints: String?, // string
    val attributes: Set<Attribute>?,
    val ianaTimezoneId: String?, // string
    val relatedWebPage: String?, // string
    val url: String?, // string
    val containsHtml: Boolean?, // true
    val additionalWaypoints: List<AdditionalWaypoint>?,
    val trackables: List<Trackable>?,
    val geocacheLogs: List<GeocacheLog>?,
    val images: List<Image>?,
    val userWaypoints: List<UserWaypoint>?
) {
    val id by lazy {
        ReferenceCode.toId(referenceCode)
    }

    val placedDateInstant by lazy {
        placedDate.toSafeInstant(ianaTimezoneId)
    }

    val publishedDateInstant by lazy {
        publishedDate.toSafeInstant(ianaTimezoneId)
    }

    val lastVisitedDateInstant by lazy {
        lastVisitedDate.toSafeInstant(ianaTimezoneId)
    }

    val foundDateInstant by lazy {
        userData?.foundDate.toSafeInstant(ianaTimezoneId)
    }

    val dnfDateInstant by lazy {
        userData?.dnfDate.toSafeInstant(ianaTimezoneId)
    }

    companion object {
        private const val FIELD_SEPARATOR = ","

        private const val FIELD_REFERENCE_CODE = "referenceCode"
        private const val FIELD_NAME = "name"
        private const val FIELD_DIFFICULTY = "difficulty"
        private const val FIELD_TERRAIN = "terrain"
        private const val FIELD_FAVORITE_POINTS = "favoritePoints"
        private const val FIELD_TRACKABLE_COUNT = "trackableCount"
        private const val FIELD_PLACED_DATE = "placedDate"
        private const val FIELD_PUBLISHED_DATE = "publishedDate"
        private const val FIELD_GEOCACHE_TYPE = "geocacheType"
        private const val FIELD_GEOCACHE_SIZE = "geocacheSize"
        private const val FIELD_USER_DATA = "userData"
        private const val FIELD_STATUS = "status"
        private const val FIELD_LOCATION = "location"
        private const val FIELD_POSTED_COORDINATES = "postedCoordinates"
        private const val FIELD_LAST_VISITED_DATE = "lastVisitedDate"
        private const val FIELD_OWNER_CODE = "ownerCode"
        private const val FIELD_OWNER_ALIAS = "ownerAlias"
        private const val FIELD_IS_PREMIUM_ONLY = "isPremiumOnly"
        private const val FIELD_SHORT_DESCRIPTION = "shortDescription"
        private const val FIELD_LONG_DESCRIPTION = "longDescription"
        private const val FIELD_HINTS = "hints"
        private const val FIELD_ATTRIBUTES = "attributes"
        private const val FIELD_IANA_TIMEZONE_ID = "ianaTimezoneId"
        private const val FIELD_RELATED_WEBPAGE = "relatedWebPage"
        private const val FIELD_URL = "url"
        private const val FIELD_CONTAINS_HTML = "containsHtml"
        private const val FIELD_ADDITIONAL_WAYPOINTS = "additionalWaypoints"
        private val FIELD_GEOCACHE_LOGS_MIN = "geocachelogs[${GeocacheLog.FIELDS_MIN}]"
        private val FIELD_TRACKABLES_MIN = "trackables[${Trackable.FIELDS_MIN}]"
        private val FIELD_IMAGES_MIN = "images[${Image.FIELDS_MIN}]"

        val FIELDS_ALL = arrayOf(
            FIELD_REFERENCE_CODE,
            FIELD_NAME,
            FIELD_DIFFICULTY,
            FIELD_TERRAIN,
            FIELD_FAVORITE_POINTS,
            FIELD_TRACKABLE_COUNT,
            FIELD_PLACED_DATE,
            FIELD_PUBLISHED_DATE,
            FIELD_GEOCACHE_TYPE,
            FIELD_GEOCACHE_SIZE,
            FIELD_USER_DATA,
            FIELD_STATUS,
            FIELD_LOCATION,
            FIELD_POSTED_COORDINATES,
            FIELD_LAST_VISITED_DATE,
            FIELD_OWNER_CODE,
            FIELD_OWNER_ALIAS,
            FIELD_IS_PREMIUM_ONLY,
            FIELD_SHORT_DESCRIPTION,
            FIELD_LONG_DESCRIPTION,
            FIELD_HINTS,
            FIELD_ATTRIBUTES,
            FIELD_IANA_TIMEZONE_ID,
            FIELD_RELATED_WEBPAGE,
            FIELD_URL,
            FIELD_CONTAINS_HTML,
            FIELD_ADDITIONAL_WAYPOINTS,
            FIELD_GEOCACHE_LOGS_MIN,
            FIELD_TRACKABLES_MIN,
            FIELD_IMAGES_MIN
        ).joinToString(FIELD_SEPARATOR)

        val FIELDS_LITE = arrayOf(
            FIELD_REFERENCE_CODE,
            FIELD_NAME,
            FIELD_DIFFICULTY,
            FIELD_TERRAIN,
            FIELD_FAVORITE_POINTS,
            FIELD_TRACKABLE_COUNT,
            FIELD_PLACED_DATE,
            FIELD_GEOCACHE_TYPE,
            FIELD_GEOCACHE_SIZE,
            FIELD_USER_DATA,
            FIELD_STATUS,
            FIELD_LOCATION,
            FIELD_POSTED_COORDINATES,
            FIELD_LAST_VISITED_DATE,
            FIELD_OWNER_CODE,
            FIELD_OWNER_ALIAS,
            FIELD_IS_PREMIUM_ONLY,
            FIELD_IANA_TIMEZONE_ID,
            FIELD_RELATED_WEBPAGE,
            FIELD_URL
        ).joinToString(FIELD_SEPARATOR)

        val FIELDS_LITE_LIVEMAP = arrayOf(
            FIELD_REFERENCE_CODE,
            FIELD_NAME,
            FIELD_DIFFICULTY,
            FIELD_TERRAIN,
//                FIELD_FAVORITE_POINTS,
//                FIELD_TRACKABLE_COUNT,
//                FIELD_PLACED_DATE,
            FIELD_GEOCACHE_TYPE,
            FIELD_GEOCACHE_SIZE,
            FIELD_USER_DATA,
            FIELD_STATUS,
//                FIELD_LOCATION,
            FIELD_POSTED_COORDINATES,
//                FIELD_LAST_VISITED_DATE,
//                FIELD_OWNER_CODE,
            FIELD_OWNER_ALIAS,
            FIELD_IS_PREMIUM_ONLY
//                FIELD_IANA_TIMEZONE_ID,
//                FIELD_RELATED_WEBPAGE,
//                FIELD_URL,
//                FIELD_CONTAINS_HTML
        ).joinToString(FIELD_SEPARATOR)

        val FIELDS_ALL_LIVEMAP = arrayOf(
            FIELD_REFERENCE_CODE,
            FIELD_NAME,
            FIELD_DIFFICULTY,
            FIELD_TERRAIN,
            FIELD_FAVORITE_POINTS,
            FIELD_TRACKABLE_COUNT,
//                FIELD_PLACED_DATE,
//                FIELD_PUBLISHED_DATE,
            FIELD_GEOCACHE_TYPE,
            FIELD_GEOCACHE_SIZE,
            FIELD_USER_DATA,
            FIELD_STATUS,
//                FIELD_LOCATION,
            FIELD_POSTED_COORDINATES,
//                FIELD_LAST_VISITED_DATE,
            FIELD_OWNER_CODE,
            FIELD_OWNER_ALIAS,
            FIELD_IS_PREMIUM_ONLY,
//                FIELD_SHORT_DESCRIPTION,
//                FIELD_LONG_DESCRIPTION,
            FIELD_HINTS
//                FIELD_ATTRIBUTES,
//                FIELD_IANA_TIMEZONE_ID,
//                FIELD_RELATED_WEBPAGE,
//                FIELD_URL,
//                FIELD_CONTAINS_HTML,
//                FIELD_ADDITIONAL_WAYPOINTS,
        ).joinToString(FIELD_SEPARATOR)
    }
}

