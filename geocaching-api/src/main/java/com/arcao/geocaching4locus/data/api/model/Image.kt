package com.arcao.geocaching4locus.data.api.model

import com.arcao.geocaching4locus.data.api.internal.moshi.adapter.LocalDateTimePTZone
import org.threeten.bp.Instant

data class Image(
        val url: String, // string
        val referenceCode: String, // string
        @LocalDateTimePTZone val createdDate: Instant, // 2018-06-06T06:16:54.165Z
        val description: String?, // string
        val guid: String // string
) {
    companion object {
        private const val FIELD_SEPARATOR = ","

        private const val FIELD_URL = "url"
        private const val FIELD_REFERENCE_CODE = "referenceCode"
        private const val FIELD_CREATED_DATE = "createdDate"
        private const val FIELD_DESCRIPTION = "description"
        private const val FIELD_GUID = "guid"

        val FIELDS_ALL = arrayOf(
                FIELD_URL,
                FIELD_REFERENCE_CODE,
                FIELD_CREATED_DATE,
                FIELD_DESCRIPTION,
                FIELD_GUID
        ).joinToString(FIELD_SEPARATOR)
    }
}