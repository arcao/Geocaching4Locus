package com.arcao.geocaching.api.downloader

import com.arcao.geocaching.api.exception.InvalidResponseException
import com.arcao.geocaching.api.exception.NetworkException
import com.arcao.geocaching4locus.BuildConfig
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import timber.log.Timber
import java.io.Reader
import java.net.URL

class OkHttpClientDownloader(private val client: OkHttpClient) : Downloader {

    @Throws(NetworkException::class, InvalidResponseException::class)
    override fun get(url: URL): Reader {
        try {
            val request = Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "Geocaching4Locus/${BuildConfig.VERSION_NAME}")
                    .addHeader("Accept-Language", "en-US")
                    .addHeader("Accept-Encoding", "gzip, deflate")
                    .addHeader("Accept", "application/json")
                    .build()

            val response = client.newCall(request).execute()
            val body = response.body()

            if (!response.isSuccessful) {
                // read error response
                throw InvalidResponseException(response.code(), response.message(), body?.string())
            }

            if (body == null)
                throw InvalidResponseException("Body is null!")

            return body.charStream()
        } catch (e: InvalidResponseException) {
            Timber.e(e)
            throw e
        } catch (e: Throwable) {
            Timber.e(e)
            throw NetworkException("Error while downloading data (${e.javaClass.simpleName})", e)
        }

    }

    @Throws(NetworkException::class, InvalidResponseException::class)
    override fun post(url: URL, postData: ByteArray): Reader {
        try {
            val request = Request.Builder()
                    .url(url)
                    .post(RequestBody.create(MEDIA_TYPE_JSON, postData))
                    .addHeader("User-Agent", "Geocaching4Locus/${BuildConfig.VERSION_NAME}")
                    .addHeader("Accept", "application/json")
                    .addHeader("Accept-Language", "en-US")
                    .addHeader("Accept-Encoding", "gzip, deflate")
                    .build()

            val response = client.newCall(request).execute()
            val body = response.body()

            if (!response.isSuccessful) {
                // read error response
                throw InvalidResponseException(response.code(), response.message(), body?.string())
            }

            if (body == null)
                throw InvalidResponseException("Body is null!")

            return body.charStream()
        } catch (e: InvalidResponseException) {
            Timber.e(e)
            throw e
        } catch (e: Throwable) {
            Timber.e(e)
            throw NetworkException("Error while downloading data (${e.javaClass.simpleName})", e)
        }

    }

    companion object {
        private val MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8")
    }
}
