package com.arcao.wherigoservice.api.parser

import com.google.gson.stream.JsonReader

import java.io.IOException

object WherigoJsonResultParser {
    @Throws(IOException::class)
    fun parse(r: JsonReader): Result {
        val status = Result()
        r.beginObject()
        while (r.hasNext()) {
            when (r.nextName()) {
                "Code" -> status.statusCode = r.nextInt()
                "Text" -> status.statusMessage = r.nextString()
                else -> r.skipValue()
            }
        }
        r.endObject()
        return status
    }

    class Result {
        var statusCode: Int = 0
            internal set
        var statusMessage: String? = null
            internal set
    }
}
