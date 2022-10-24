package com.arcao.geocaching4locus.data.api.internal.moshi.adapter

import com.arcao.geocaching4locus.data.api.model.enums.AdditionalWaypointType
import com.arcao.geocaching4locus.data.api.model.enums.GeocacheListType
import com.arcao.geocaching4locus.data.api.model.enums.GeocacheStatus
import com.arcao.geocaching4locus.data.api.model.enums.IdType
import com.arcao.geocaching4locus.data.api.model.enums.IdValueType
import com.arcao.geocaching4locus.data.api.model.enums.MembershipType
import com.arcao.geocaching4locus.data.api.model.enums.StatusCode
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import java.lang.reflect.Type

class EnumAdapterFactory : JsonAdapter.Factory {
    override fun create(
        type: Type,
        annotations: MutableSet<out Annotation>,
        moshi: Moshi
    ): JsonAdapter<*>? {
        return when (type) {
            AdditionalWaypointType::class.java -> AdditionalWaypointTypeAdapter.INSTANCE
            GeocacheListType::class.java -> GeocacheListTypeAdapter.INSTANCE
            GeocacheStatus::class.java -> GeocacheStatusAdapter.INSTANCE
            MembershipType::class.java -> MembershipTypeAdapter.INSTANCE
            StatusCode::class.java -> StatusCodeAdapter.INSTANCE
            else -> null
        }
    }

    abstract class ValueEnumAdapter<T : IdValueType> : JsonAdapter<T>() {
        abstract fun from(value: String?): T
        final override fun fromJson(reader: JsonReader): T = from(reader.nextString())
        final override fun toJson(writer: JsonWriter, value: T?) {
            writer.value(value?.value)
        }
    }

    abstract class IdEnumAdapter<T : IdType> : JsonAdapter<T>() {
        abstract fun from(id: Int?): T
        final override fun fromJson(reader: JsonReader): T = from(reader.nextInt())
        final override fun toJson(writer: JsonWriter, value: T?) {
            writer.value(value?.id)
        }
    }

    class AdditionalWaypointTypeAdapter : IdEnumAdapter<AdditionalWaypointType>() {
        companion object {
            val INSTANCE = AdditionalWaypointTypeAdapter()
        }

        override fun from(id: Int?) = AdditionalWaypointType.from(id)
    }

    class GeocacheListTypeAdapter : IdEnumAdapter<GeocacheListType>() {
        companion object {
            val INSTANCE = GeocacheListTypeAdapter()
        }

        override fun from(id: Int?) = GeocacheListType.from(id)
    }

    class GeocacheStatusAdapter : ValueEnumAdapter<GeocacheStatus>() {
        companion object {
            val INSTANCE = GeocacheStatusAdapter()
        }

        override fun from(value: String?) = GeocacheStatus.from(value)
    }

    class MembershipTypeAdapter : IdEnumAdapter<MembershipType>() {
        companion object {
            val INSTANCE = MembershipTypeAdapter()
        }

        override fun from(id: Int?) = MembershipType.from(id)
    }

    class StatusCodeAdapter : IdEnumAdapter<StatusCode>() {
        companion object {
            val INSTANCE = StatusCodeAdapter()
        }

        override fun from(id: Int?) = StatusCode.from(id) ?: StatusCode.BAD_REQUEST
    }
}
