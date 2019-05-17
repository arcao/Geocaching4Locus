package com.arcao.geocaching4locus.data.api.model

data class Trackable(
    val referenceCode: String, // string
    val iconUrl: String, // string
    val name: String, // string
    val goal: String?, // string
    val description: String?, // string
    val releasedDate: String, // string
    val originCountry: String, // string
    val allowedToBeCollected: Boolean, // true
    val ownerCode: String, // string
    val holderCode: String?, // string
    val inHolderCollection: Boolean, // true
    val currentGeocacheCode: String?, // string
    val isMissing: Boolean, // true
    val type: String, // string
    val imageCount: Int, // 0
    val trackingNumber: String, // string
    val url: String, // string
    val owner: User, // user
    val holder: User // user
) {
    companion object {
        private const val FIELD_SEPARATOR = ","

        private const val FIELD_REFERENCE_CODE = "referenceCode"
        private const val FIELD_ICON_URL = "iconUrl"
        private const val FIELD_NAME = "name"
        private const val FIELD_GOAL = "goal"
        private const val FIELD_DESCRIPTION = "description"
        private const val FIELD_RELEASE_DDATE = "releasedDate"
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
        private const val FIELD_OWNER = "owner"
        private const val FIELD_HOLDER = "holder"

        val FIELDS_ALL = arrayOf(
            FIELD_REFERENCE_CODE,
            FIELD_ICON_URL,
            FIELD_NAME,
            FIELD_GOAL,
            FIELD_DESCRIPTION,
            FIELD_RELEASE_DDATE,
            FIELD_ORIGIN_COUNTRY,
            FIELD_ALLOWED_TO_BE_COLLECTED,
            FIELD_OWNER_CODE,
            FIELD_HOLDER_CODE,
            FIELD_IN_HOLDER_COLLECTION,
            FIELD_CURRENT_GEOCACHE_CODE,
            FIELD_IS_MISSING,
            FIELD_TYPE,
            FIELD_IMAGE_COUNT,
            FIELD_TRACKING_NUMBER,
            FIELD_OWNER,
            FIELD_HOLDER
        ).joinToString(FIELD_SEPARATOR)
    }
}