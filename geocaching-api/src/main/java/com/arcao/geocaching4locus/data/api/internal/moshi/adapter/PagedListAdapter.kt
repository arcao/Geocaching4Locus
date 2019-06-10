package com.arcao.geocaching4locus.data.api.internal.moshi.adapter

import com.arcao.geocaching4locus.data.api.model.response.MutablePagedList
import com.arcao.geocaching4locus.data.api.model.response.PagedArrayList
import com.arcao.geocaching4locus.data.api.model.response.PagedList
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonAdapter.Factory
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.io.IOException
import java.lang.reflect.Type

/** Converts collection types to JSON arrays containing their converted contents.  */
internal class PagedListAdapter<T> private constructor(private val elementAdapter: JsonAdapter<T>) :
    JsonAdapter<PagedList<T?>>() {
    @Throws(IOException::class)
    override fun fromJson(reader: JsonReader): PagedList<T?> {
        val result = PagedArrayList<T?>()

        when (reader.peek()) {
            JsonReader.Token.NULL -> return result
            JsonReader.Token.BEGIN_ARRAY -> readArray(reader, result)
            JsonReader.Token.BEGIN_OBJECT -> {
                reader.beginObject()
                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "total" -> result.totalCount = reader.nextLong()
                        "data" -> readArray(reader, result)
                    }
                }
                reader.endObject()
            }
            else -> throw JsonDataException("Expected BEGIN_ARRAY or BEGIN_OBJECT, but was ${reader.peek()}")
        }

        return result
    }

    private fun readArray(reader: JsonReader, result: MutablePagedList<T?>) {
        reader.beginArray()
        while (reader.hasNext()) {
            result.add(elementAdapter.fromJson(reader))
        }
        reader.endArray()
    }

    @Throws(IOException::class)
    override fun toJson(writer: JsonWriter, value: PagedList<T?>?) {
        writer.beginArray()
        if (value != null) {
            for (element in value) {
                elementAdapter.toJson(writer, element)
            }
        }
        writer.endArray()
    }

    override fun toString(): String {
        return "$elementAdapter.collection()"
    }

    companion object {
        val FACTORY = Factory { type, annotations, moshi ->
            val rawType = Types.getRawType(type)
            when {
                annotations.isNotEmpty() -> null
                rawType == PagedList::class.java -> newTotalCountArrayListAdapter<Any>(type, moshi).nullSafe()
                else -> null
            }
        }

        private fun <T> newTotalCountArrayListAdapter(type: Type, moshi: Moshi): JsonAdapter<PagedList<T?>> {
            val elementType = Types.collectionElementType(type, Collection::class.java)
            val elementAdapter = moshi.adapter<T>(elementType)
            return PagedListAdapter<T>(elementAdapter)
        }
    }
}
