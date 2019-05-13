package com.arcao.wherigoservice.api

import com.arcao.geocaching.api.downloader.Downloader
import com.arcao.geocaching.api.exception.InvalidResponseException
import com.arcao.geocaching.api.exception.NetworkException
import com.arcao.geocaching.api.util.DefaultValueJsonReader
import com.arcao.wherigoservice.api.parser.WherigoJsonResultParser
import com.google.gson.stream.JsonReader
import com.google.gson.stream.MalformedJsonException
import timber.log.Timber
import java.io.EOFException
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL

class WherigoServiceImpl(private val downloader: Downloader) : WherigoService {
    @Throws(WherigoServiceException::class)
    override fun getCacheCodeFromGuid(cacheGuid: String): String? {
        var cacheCode: String? = null

        try {
            val r = callGet("getCacheCodeFromGuid?CacheGUID=$cacheGuid&format=json")

            r.beginObject()
            checkError(r)

            while (r.hasNext()) {
                when (r.nextName()) {
                    "CacheResult" -> {
                        r.beginObject()
                        while (r.hasNext()) {
                            when (r.nextName()) {
                                "CacheCode" -> cacheCode = r.nextString()
                                else -> r.skipValue()
                            }
                        }
                        r.endObject()
                    }
                    else -> r.skipValue()
                }
            }
            r.endObject()
            r.close()
            Timber.i("Cache code: $cacheCode")
        } catch (e: NetworkException) {
            throw WherigoServiceException(WherigoServiceException.ERROR_CONNECTION_ERROR, e.message, e)
        } catch (e: InvalidResponseException) {
            throw WherigoServiceException(
                WherigoServiceException.ERROR_API_ERROR,
                "Response is not valid JSON string: ${e.message}",
                e
            )
        } catch (e: IOException) {
            Timber.e(e)
            if (!isGsonException(e)) {
                throw WherigoServiceException(WherigoServiceException.ERROR_CONNECTION_ERROR, e.message, e)
            }

            throw WherigoServiceException(
                WherigoServiceException.ERROR_API_ERROR,
                "Response is not valid JSON string: ${e.message}",
                e
            )
        }

        return cacheCode
    }

    @Throws(WherigoServiceException::class)
    override fun getTime(): Long {
        var time: Long = 0

        try {
            val r = callGet("getTime?format=json")

            r.beginObject()
            checkError(r)

            while (r.hasNext()) {
                when (r.nextName()) {
                    "TimeResult" -> {
                        r.beginObject()
                        while (r.hasNext()) {
                            when (r.nextName()) {
                                "Time" -> time = r.nextLong()
                                else -> r.skipValue()
                            }
                        }
                        r.endObject()
                    }
                    else -> r.skipValue()
                }
            }
            r.endObject()
            r.close()
            Timber.i("Time: $time")
        } catch (e: NetworkException) {
            throw WherigoServiceException(WherigoServiceException.ERROR_CONNECTION_ERROR, e.message, e)
        } catch (e: InvalidResponseException) {
            throw WherigoServiceException(
                WherigoServiceException.ERROR_API_ERROR,
                "Response is not valid JSON string: ${e.message}",
                e
            )
        } catch (e: IOException) {
            Timber.e(e)
            if (!isGsonException(e)) {
                throw WherigoServiceException(WherigoServiceException.ERROR_CONNECTION_ERROR, e.message, e)
            }

            throw WherigoServiceException(
                WherigoServiceException.ERROR_API_ERROR,
                "Response is not valid JSON string: ${e.message}",
                e
            )
        }

        return time
    }

    // -------------------- Helper methods ----------------------------------------

    @Throws(IOException::class)
    private fun checkError(r: JsonReader) {
        when (r.nextName()) {
            "Status" -> {
                val status = WherigoJsonResultParser.parse(r)

                when (status.statusCode) {
                    WherigoServiceException.ERROR_OK -> return
                    else -> throw WherigoServiceException(status.statusCode, status.statusMessage)
                }
            }
            else -> throw WherigoServiceException(
                WherigoServiceException.ERROR_API_ERROR,
                "Missing Status in a response."
            )
        }
    }

    @Throws(NetworkException::class, InvalidResponseException::class)
    private fun callGet(function: String): JsonReader {
        Timber.d("Getting %s", maskParameterValues(function))

        try {
            val url = URL("$BASE_URL/$function")
            return DefaultValueJsonReader(downloader.get(url))
        } catch (e: MalformedURLException) {
            Timber.e(e)
            throw NetworkException("Error while downloading data (" + e.javaClass.simpleName + ")", e)
        }
    }

    private fun maskParameterValues(function: String): String {
        return function.replace("([Aa]ccess[Tt]oken=)([^&]+)".toRegex(), "$1******")
    }

    private fun isGsonException(t: Throwable): Boolean {
        return (t is MalformedJsonException || t is IllegalStateException ||
            t is NumberFormatException || t is EOFException)
    }

    companion object {
        private const val BASE_URL = "https://wherigo-service.appspot.com/api"
    }
}
