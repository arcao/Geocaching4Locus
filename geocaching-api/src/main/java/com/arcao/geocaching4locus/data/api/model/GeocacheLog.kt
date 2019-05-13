package com.arcao.geocaching4locus.data.api.model

import com.arcao.geocaching4locus.data.api.internal.moshi.adapter.LocalDateTimePTZone
import com.arcao.geocaching4locus.data.api.model.enum.GeocacheLogType
import org.threeten.bp.Instant

data class GeocacheLog(
        val referenceCode: String, // string
        val owner : User?, // User
        val imageCount: Int, // 0
        val isEncoded: Boolean, // true
        val isArchived: Boolean, // true
        val images: List<Image>?,
        @LocalDateTimePTZone val loggedDate: Instant, // 2018-06-06T06:16:54.165Z
        val text: String?, // string
        val type: GeocacheLogType, // Found It
        val updatedCoordinates: Coordinates?,
        val geocacheCode: String, // string
        val usedFavoritePoint: Boolean, // true
        val url : String // string
) {
    companion object {
        private const val FIELD_SEPARATOR = ","

        private const val FIELD_REFERENCE_CODE = "referenceCode"
        private const val FIELD_OWNER = "owner"
        private const val FIELD_IMAGE_COUNT = "imageCount"
        private const val FIELD_IS_ENCODED = "isEncoded"
        private const val FIELD_IS_ARCHIVED = "isArchived"
        private const val FIELD_LOGGED_DATE = "loggedDate"
        private const val FIELD_TEXT = "text"
        private const val FIELD_TYPE = "type"
        private const val FIELD_UPDATED_COORDINATES = "updatedCoordinates"
        private const val FIELD_GEOCACHE_CODE = "geocacheCode"
        private const val FIELD_USED_FAVORITE_POINT = "usedFavoritePoint"
        private const val FIELD_URL = "url"

        val FIELDS_ALL = arrayOf(
                FIELD_REFERENCE_CODE,
                FIELD_OWNER,
                FIELD_IMAGE_COUNT,
                FIELD_IS_ENCODED,
                FIELD_IS_ARCHIVED,
                FIELD_LOGGED_DATE,
                FIELD_TEXT,
                FIELD_TYPE,
                FIELD_UPDATED_COORDINATES,
                FIELD_GEOCACHE_CODE,
                FIELD_USED_FAVORITE_POINT,
                FIELD_URL
        ).joinToString(FIELD_SEPARATOR)
    }
}