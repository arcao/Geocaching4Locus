package com.arcao.geocaching4locus.data.api.model

import com.arcao.geocaching4locus.data.api.util.ReferenceCode
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

data class GeocacheLog(
    val referenceCode: String, // string
    val owner: User?, // User
    val imageCount: Int = 0, // 0
    val isEncoded: Boolean = false, // true
    val isArchived: Boolean = false, // true
    val images: List<Image>?,
    val loggedDate: LocalDateTime?, // 2018-06-06T06:16:54.165
    val text: String?, // string
    val geocacheLogType: GeocacheLogType, // Found It
    val updatedCoordinates: Coordinates?,
    val geocacheCode: String?, // string
    val ianaTimezoneId: String?, // string
    val usedFavoritePoint: Boolean = false, // true
    val url: String? // string
) {
    val id by lazy {
        ReferenceCode.toId(referenceCode)
    }

    val loggedDateInstant by lazy {
        if (loggedDate != null && ianaTimezoneId != null) {
            ZonedDateTime.of(loggedDate, ZoneId.of(ianaTimezoneId)).toInstant()
        } else {
            null
        }
    }

    companion object {
        private const val FIELD_SEPARATOR = ","

        private const val FIELD_REFERENCE_CODE = "referenceCode"
        private const val FIELD_OWNER_MIN = "owner[referenceCode,findCount,username]"
        private const val FIELD_IMAGE_COUNT = "imageCount"
        private const val FIELD_IS_ENCODED = "isEncoded"
        private const val FIELD_IS_ARCHIVED = "isArchived"
        private const val FIELD_LOGGED_DATE = "loggedDate"
        private const val FIELD_TEXT = "text"
        private const val FIELD_GEOCACHE_LOG_TYPE = "geocacheLogType"
        private const val FIELD_UPDATED_COORDINATES = "updatedCoordinates"
        private const val FIELD_GEOCACHE_CODE = "geocacheCode"
        private const val FIELD_IANA_TIMEZONE_ID = "ianaTimezoneId"
        private const val FIELD_USED_FAVORITE_POINT = "usedFavoritePoint"
        private const val FIELD_URL = "url"
        private val FIELD_IMAGES_MIN = "images[${Image.FIELDS_MIN}]"

        val FIELDS_ALL = arrayOf(
            FIELD_REFERENCE_CODE,
            FIELD_OWNER_MIN,
            FIELD_IMAGE_COUNT,
            FIELD_IS_ENCODED,
            FIELD_IS_ARCHIVED,
            FIELD_LOGGED_DATE,
            FIELD_TEXT,
            FIELD_GEOCACHE_LOG_TYPE,
            FIELD_UPDATED_COORDINATES,
            FIELD_GEOCACHE_CODE,
            FIELD_IANA_TIMEZONE_ID,
            FIELD_USED_FAVORITE_POINT,
            FIELD_URL
        ).joinToString(FIELD_SEPARATOR)

        val FIELDS_MIN = arrayOf(
            FIELD_REFERENCE_CODE,
            FIELD_OWNER_MIN,
            FIELD_LOGGED_DATE,
            FIELD_TEXT,
            FIELD_GEOCACHE_LOG_TYPE,
            FIELD_UPDATED_COORDINATES,
            FIELD_IANA_TIMEZONE_ID,
            FIELD_IMAGES_MIN
        ).joinToString(FIELD_SEPARATOR)
    }
}