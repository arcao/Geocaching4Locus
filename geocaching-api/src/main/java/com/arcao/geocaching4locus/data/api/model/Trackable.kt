package com.arcao.geocaching4locus.data.api.model

import com.arcao.geocaching4locus.data.api.internal.moshi.adapter.LocalDateTimeUTC
import com.arcao.geocaching4locus.data.api.util.ReferenceCode
import org.threeten.bp.Instant

data class Trackable(
    val referenceCode: String, // string
    val iconUrl: String?, // string
    val name: String, // string
    val goal: String?, // string
    val description: String?, // string
    @LocalDateTimeUTC val releasedDate: Instant, // 2001-08-30T10:27:11
    val originCountry: String?, // string
    val allowedToBeCollected: Boolean = false, // true
    val ownerCode: String?, // string
    val holderCode: String?, // string
    val inHolderCollection: Boolean = false, // true
    val currentGeocacheCode: String?, // string
    val isMissing: Boolean = false, // true
    val type: String?, // string
    val imageCount: Int = 0, // 0
    val trackingNumber: String?, // string
    val url: String?, // string
    val owner: User, // user
    val holder: User? // user
) {
    val id by lazy {
        ReferenceCode.toId(referenceCode)
    }

    companion object {
        private const val FIELD_SEPARATOR = ","

        private const val FIELD_REFERENCE_CODE = "referenceCode"
        private const val FIELD_ICON_URL = "iconUrl"
        private const val FIELD_NAME = "name"
        private const val FIELD_GOAL = "goal"
        private const val FIELD_DESCRIPTION = "description"
        private const val FIELD_RELEASED_DATE = "releasedDate"
        private const val FIELD_ORIGIN_COUNTRY = "originCountry"
        private const val FIELD_ALLOWED_TO_BE_COLLECTED = "allowedToBeCollected"
        private const val FIELD_OWNER_CODE = "ownerCode"
        private const val FIELD_HOLDER_CODE = "holderCode"
        private const val FIELD_IN_HOLDER_COLLECTION = "inHolderCollection"
        private const val FIELD_CURRENT_GEOCACHE_CODE = "currentGeocacheCode"
        private const val FIELD_IS_MISSING = "isMissing"
        private const val FIELD_TYPE = "type"
        private const val FIELD_IMAGE_COUNT = "imageCount"
        private const val FIELD_TRACKING_NUMBER = "trackingNumber"
        private const val FIELD_URL = "url"
        private const val FIELD_OWNER = "owner"
        private const val FIELD_HOLDER = "holder"

        private val FIELD_OWNER_MIN = "owner[${User.FIELDS_MIN}]"
        private val FIELD_HOLDER_MIN = "holder[${User.FIELDS_MIN}]"

        val FIELDS_ALL = arrayOf(
            FIELD_REFERENCE_CODE,
            FIELD_ICON_URL,
            FIELD_NAME,
            FIELD_GOAL,
            FIELD_DESCRIPTION,
            FIELD_RELEASED_DATE,
            FIELD_ORIGIN_COUNTRY,
            FIELD_ALLOWED_TO_BE_COLLECTED,
            FIELD_OWNER_CODE,
            FIELD_HOLDER_CODE,
            FIELD_IN_HOLDER_COLLECTION,
            FIELD_CURRENT_GEOCACHE_CODE,
            FIELD_IS_MISSING,
            FIELD_TYPE,
            FIELD_IMAGE_COUNT,
            FIELD_URL,
            FIELD_OWNER,
            FIELD_HOLDER
        ).joinToString(FIELD_SEPARATOR)

        val FIELDS_MIN = arrayOf(
            FIELD_REFERENCE_CODE,
            FIELD_ICON_URL,
            FIELD_NAME,
            FIELD_RELEASED_DATE,
            FIELD_ORIGIN_COUNTRY,
            FIELD_URL,
            FIELD_OWNER_MIN,
            FIELD_HOLDER_MIN
        ).joinToString(FIELD_SEPARATOR)
    }
}