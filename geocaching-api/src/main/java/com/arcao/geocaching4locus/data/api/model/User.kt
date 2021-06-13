package com.arcao.geocaching4locus.data.api.model

import com.arcao.geocaching4locus.data.api.model.enums.MembershipType
import com.arcao.geocaching4locus.data.api.util.ReferenceCode
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User(
    val referenceCode: String?, // string
    val findCount: Int = 0, // 0
    val hideCount: Int = 0, // 0
    val favoritePoints: Int = 0, // 0
    val username: String?, // string
    @Json(name = "membershipLevelId") val membership: MembershipType = MembershipType.UNKNOWN, // 0
    val avatarUrl: String?, // string
    val bannerUrl: String?, // string
    val profileText: String?, // string
    val url: String?, // string
    val homeCoordinates: Coordinates?,
    val geocacheLimits: GeocacheLimits?
) {
    val id by lazy {
        ReferenceCode.toId(requireNotNull(referenceCode) {
            "Reference code is null."
        })
    }

    companion object {
        private const val FIELD_REFERENCE_CODE = "referenceCode"
        private const val FIELD_FIND_COUNT = "findCount"
        private const val FIELD_HIDE_COUNT = "hideCount"
        private const val FIELD_FAVORITE_POINTS = "favoritePoints"
        private const val FIELD_USERNAME = "username"
        private const val FIELD_MEMBERSHIP = "membershipLevelId"
        private const val FIELD_AVATAR_URL = "avatarUrl"
        private const val FIELD_BANNER_URL = "bannerUrl"
        private const val FIELD_PROFILE_TEXT = "profileText"
        private const val FIELD_URL = "url"
        private const val FIELD_HOME_COORDINATES = "homeCoordinates"
        private const val FIELD_GEOCACHE_LIMITS = "geocacheLimits"

        // GDPR safe fields
        val FIELDS_ALL_NO_HOME_COORDINATES = fieldsOf(
            FIELD_REFERENCE_CODE,
            FIELD_FIND_COUNT,
            FIELD_HIDE_COUNT,
            FIELD_FAVORITE_POINTS,
            FIELD_USERNAME,
            FIELD_MEMBERSHIP,
            FIELD_AVATAR_URL,
            FIELD_BANNER_URL,
            FIELD_PROFILE_TEXT,
            FIELD_URL,
            FIELD_GEOCACHE_LIMITS
        )

        val FIELDS_ALL = fieldsOf(
            FIELD_REFERENCE_CODE,
            FIELD_FIND_COUNT,
            FIELD_HIDE_COUNT,
            FIELD_FAVORITE_POINTS,
            FIELD_USERNAME,
            FIELD_MEMBERSHIP,
            FIELD_AVATAR_URL,
            FIELD_BANNER_URL,
            FIELD_PROFILE_TEXT,
            FIELD_URL,
            FIELD_HOME_COORDINATES,
            FIELD_GEOCACHE_LIMITS
        )

        val FIELDS_MIN = fieldsOf(
            FIELD_REFERENCE_CODE,
            FIELD_USERNAME
        )

        val FIELDS_LIMITS = fieldsOf(
            FIELD_MEMBERSHIP,
            FIELD_GEOCACHE_LIMITS
        )
    }
}
