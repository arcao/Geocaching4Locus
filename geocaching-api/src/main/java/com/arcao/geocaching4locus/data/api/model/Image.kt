package com.arcao.geocaching4locus.data.api.model

import com.arcao.geocaching4locus.data.api.internal.moshi.adapter.LocalDateTimeUTC
import com.arcao.geocaching4locus.data.api.util.ReferenceCode
import com.squareup.moshi.JsonClass
import java.time.Instant

@JsonClass(generateAdapter = true)
data class Image(
    val description: String?, // string
    val url: String, // string
    val thumbnailUrl: String?,
    val referenceCode: String?, // string
    @LocalDateTimeUTC val createdDate: Instant?, // 2018-06-06T06:16:54.165
    val guid: String // string
) {
    val id by lazy {
        ReferenceCode.toId(requireNotNull(referenceCode) {
            "Reference code is null"
        })
    }

    companion object {
        private const val FIELD_DESCRIPTION = "description"
        private const val FIELD_URL = "url"
        private const val FIELD_THUMBNAIL_URL = "thumbnailUrl"
        private const val FIELD_REFERENCE_CODE = "referenceCode"
        private const val FIELD_CREATED_DATE = "createdDate"
        private const val FIELD_GUID = "guid"

        val FIELDS_ALL = fieldsOf(
            FIELD_DESCRIPTION,
            FIELD_URL,
            FIELD_THUMBNAIL_URL,
            FIELD_REFERENCE_CODE,
            FIELD_CREATED_DATE,
            FIELD_GUID
        )

        val FIELDS_MIN = fieldsOf(
            FIELD_DESCRIPTION,
            FIELD_URL,
            FIELD_THUMBNAIL_URL,
            FIELD_GUID
        )
    }
}
