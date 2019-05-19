package com.arcao.geocaching4locus.data.api.model

import com.arcao.geocaching4locus.data.api.internal.moshi.adapter.LocalDateTimeUTC
import org.threeten.bp.Instant

data class Image(
    val description: String?, // string
    val url: String, // string
    val thumbnailUrl: String?,
    val referenceCode: String?, // string
    @LocalDateTimeUTC val createdDate: Instant?, // 2018-06-06T06:16:54.165
    val guid: String // string
) {
    companion object {
        private const val FIELD_SEPARATOR = ","

        private const val FIELD_DESCRIPTION = "description"
        private const val FIELD_URL = "url"
        private const val FIELD_THUMBNAIL_URL = "thumbnailUrl"
        private const val FIELD_REFERENCE_CODE = "referenceCode"
        private const val FIELD_CREATED_DATE = "createdDate"
        private const val FIELD_GUID = "guid"

        val FIELDS_ALL = arrayOf(
            FIELD_DESCRIPTION,
            FIELD_URL,
            FIELD_THUMBNAIL_URL,
            FIELD_REFERENCE_CODE,
            FIELD_CREATED_DATE,
            FIELD_GUID
        ).joinToString(FIELD_SEPARATOR)

        val FIELDS_MIN = arrayOf(
            FIELD_DESCRIPTION,
            FIELD_URL,
            FIELD_THUMBNAIL_URL,
            FIELD_GUID
        ).joinToString(FIELD_SEPARATOR)
    }
}