package com.arcao.geocaching4locus.data.api.model

import com.arcao.geocaching4locus.data.api.model.enum.GeocacheListType
import com.arcao.geocaching4locus.data.api.util.ReferenceCode
import com.squareup.moshi.Json
import org.threeten.bp.Instant

data class GeocacheList(
    val referenceCode: String = "", // string
    val lastUpdatedDateUtc: Instant = Instant.now(), // 2018-06-06T06:16:54.275Z
    val createdDateUtc: Instant = Instant.now(), // 2018-06-06T06:16:54.275Z
    val count: Int = 0, // 0
    val findCount: Int = 0, // 0
    val ownerCode: String = "", // string
    val name: String, // string
    val description: String = "", // string
    @Json(name = "typeId") val type: GeocacheListType, // 0
    val isPublic: Boolean, // true
    val isShared: Boolean, // true
    val url: String // string
) {
    val id by lazy {
        ReferenceCode.toId(referenceCode)
    }

    companion object {
        private const val FIELD_SEPARATOR = ","

        private const val FIELD_REFERENCE_CODE = "referenceCode"
        private const val FIELD_LAST_UPDATED_DATE_UTC = "lastUpdatedDateUtc"
        private const val FIELD_CREATED_DATE_UTC = "createdDateUtc"
        private const val FIELD_COUNT = "count"
        private const val FIELD_FIND_COUNT = "findCount"
        private const val FIELD_OWNER_CODE = "ownerCode"
        private const val FIELD_NAME = "name"
        private const val FIELD_DESCRIPTION = "description"
        private const val FIELD_TYPE = "type"
        private const val FIELD_IS_PUBLIC = "isPublic"
        private const val FIELD_IS_SHARED = "isShared"
        private const val FIELD_URL = "url"

        val FIELDS_ALL = arrayOf(
            FIELD_REFERENCE_CODE,
            FIELD_LAST_UPDATED_DATE_UTC,
            FIELD_CREATED_DATE_UTC,
            FIELD_COUNT,
            FIELD_FIND_COUNT,
            FIELD_OWNER_CODE,
            FIELD_NAME,
            FIELD_DESCRIPTION,
            FIELD_TYPE,
            FIELD_IS_PUBLIC,
            FIELD_IS_SHARED,
            FIELD_URL
        ).joinToString(FIELD_SEPARATOR)
    }
}