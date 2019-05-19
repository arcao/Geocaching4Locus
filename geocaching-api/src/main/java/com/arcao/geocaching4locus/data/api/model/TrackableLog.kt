package com.arcao.geocaching4locus.data.api.model

import com.arcao.geocaching4locus.data.api.util.ReferenceCode
import org.threeten.bp.Instant

data class TrackableLog(
    val referenceCode: String, // string
    val ownerCode: String, // string
    val imageCount: Int, // 0
    val trackableCode: String, // string
    val geocacheCode: String, // string
    val geocacheName: String, // string
    val loggedDate: Instant, // 2018-06-06T06:16:54.589Z
    val text: String, // string
    val isRot13Encoded: Boolean, // true
    val trackableLogType: TrackableLogType, // type
    val coordinates: Coordinates, // Coordinates
    val url: String, // string
    val owner: User // user
) {
    val id by lazy {
        ReferenceCode.toId(referenceCode)
    }

    companion object {
        private const val FIELD_SEPARATOR = ","

        private const val FIELD_REFERENCE_CODE = "referenceCode"
        private const val FIELD_OWNER_CODE = "ownerCode"
        private const val FIELD_IMAGE_COUNT = "imageCount"
        private const val FIELD_TRACKABLE_CODE = "trackableCode"
        private const val FIELD_GEOCACHE_CODE = "geocacheCode"
        private const val FIELD_GEOCACHE_NAME = "geocacheName"
        private const val FIELD_LOGGED_DATE = "loggedDate"
        private const val FIELD_TEXT = "text"
        private const val FIELD_IS_ROT13_ENCODED = "isRot13Encoded"
        private const val FIELD_TRACKABLE_LOG_TYPE = "trackableLogType"
        private const val FIELD_COORDINATES = "coordinates"
        private const val FIELD_URL = "url"
        private const val FIELD_OWNER = "owner"

        val FIELDS_ALL = arrayOf(
            FIELD_REFERENCE_CODE,
            FIELD_OWNER_CODE,
            FIELD_IMAGE_COUNT,
            FIELD_TRACKABLE_CODE,
            FIELD_GEOCACHE_CODE,
            FIELD_GEOCACHE_NAME,
            FIELD_LOGGED_DATE,
            FIELD_TEXT,
            FIELD_IS_ROT13_ENCODED,
            FIELD_TRACKABLE_LOG_TYPE,
            FIELD_COORDINATES,
            FIELD_URL,
            FIELD_OWNER
        ).joinToString(FIELD_SEPARATOR)
    }
}