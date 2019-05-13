package com.arcao.geocaching4locus.data.api.internal.moshi.adapter

import com.arcao.geocaching4locus.data.api.model.response.MutableTotalCountList
import com.arcao.geocaching4locus.data.api.model.response.TotalCountArrayList
import com.arcao.geocaching4locus.data.api.model.response.TotalCountList
import com.squareup.moshi.*
import com.squareup.moshi.JsonAdapter.Factory
import java.io.IOException
import java.lang.reflect.Type

/** Converts collection types to JSON arrays containing their converted contents.  */
internal class TotalCountListAdapter<T> private constructor(private val elementAdapter: JsonAdapter<T>) : JsonAdapter<TotalCountList<T?>>() {
    @Throws(IOException::class)
    override fun fromJson(reader: JsonReader): TotalCountList<T?> {
        val result = TotalCountArrayList<T?>()

        when(reader.peek()) {
            JsonReader.Token.NULL -> {}
            JsonReader.Token.BEGIN_ARRAY -> readArray(reader, result)
            JsonReader.Token.BEGIN_OBJECT -> {
                reader.beginObject()
                while (reader.hasNext()) {
                    val name = reader.nextName()
                    when (name) {
                        "total" -> result.totalCount = reader.nextLong()
                        "data" -> readArray(reader, result)
                    }
                }
                reader.endObject()
            }
            else -> JsonDataException("Expected BEGIN_ARRAY or BEGIN_OBJECT, but was ${reader.peek()}")
        }

        return result
    }

    private fun readArray(reader: JsonReader, result: MutableTotalCountList<T?>) {
        reader.beginArray()
        while (reader.hasNext()) {
            result.add(elementAdapter.fromJson(reader))
        }
        reader.endArray()
    }

    @Throws(IOException::class)
    override fun toJson(writer: JsonWriter, value: TotalCountList<T?>?) {
        writer.beginArray()
        if (value != null) {
            for (element in value) {
                elementAdapter.toJson(writer, element)
            }
        }
        writer.endArray()
    }

    override fun toString(): String {
        return elementAdapter.toString() + ".collection()"
    }

    companion object {
        val FACTORY: JsonAdapter.Factory = Factory { type, annotations, moshi ->
            val rawType = Types.getRawType(type)
            when {
                !annotations.isEmpty() -> null
                rawType == TotalCountList::class.java -> newTotalCountArrayListAdapter<Any>(type, moshi).nullSafe()
                else -> null
            }
        }

        private fun <T> newTotalCountArrayListAdapter(type: Type, moshi: Moshi): JsonAdapter<TotalCountList<T?>> {
            val elementType = Types.collectionElementType(type, Collection::class.java)
            val elementAdapter = moshi.adapter<T>(elementType)
            return TotalCountListAdapter<T>(elementAdapter)
        }
    }
}