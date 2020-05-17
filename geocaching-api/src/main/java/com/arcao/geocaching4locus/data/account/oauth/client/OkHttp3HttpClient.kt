package com.arcao.geocaching4locus.data.account.oauth.client

import com.github.scribejava.core.httpclient.HttpClient
import com.github.scribejava.core.httpclient.multipart.MultipartPayload
import com.github.scribejava.core.model.OAuthAsyncRequestCallback
import com.github.scribejava.core.model.OAuthConstants
import com.github.scribejava.core.model.OAuthRequest.ResponseConverter
import com.github.scribejava.core.model.Response
import com.github.scribejava.core.model.Verb
import okhttp3.Call
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.internal.http.HttpMethod
import java.io.File
import java.io.IOException
import java.util.HashMap
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future

class OkHttp3HttpClient(private val client: OkHttpClient) : HttpClient {
    @Throws(IOException::class)
    override fun close() {
        client.dispatcher().executorService().shutdown()
        client.connectionPool().evictAll()
        val cache = client.cache()
        cache?.close()
    }

    override fun <T> executeAsync(
        userAgent: String?,
        headers: Map<String, String>,
        httpVerb: Verb,
        completeUrl: String,
        bodyContents: ByteArray,
        callback: OAuthAsyncRequestCallback<T>,
        converter: ResponseConverter<T>
    ): Future<T> {
        return doExecuteAsync(
            userAgent,
            headers,
            httpVerb,
            completeUrl,
            BodyType.BYTE_ARRAY,
            bodyContents,
            callback,
            converter
        )
    }

    override fun <T> executeAsync(
        userAgent: String?,
        headers: Map<String, String>,
        httpVerb: Verb,
        completeUrl: String,
        bodyContents: MultipartPayload,
        callback: OAuthAsyncRequestCallback<T>,
        converter: ResponseConverter<T>
    ): Future<T> {
        throw UnsupportedOperationException("OKHttpClient does not support Multipart payload for the moment")
    }

    override fun <T> executeAsync(
        userAgent: String?,
        headers: Map<String, String>,
        httpVerb: Verb,
        completeUrl: String,
        bodyContents: String,
        callback: OAuthAsyncRequestCallback<T>,
        converter: ResponseConverter<T>
    ): Future<T> {
        return doExecuteAsync(
            userAgent,
            headers,
            httpVerb,
            completeUrl,
            BodyType.STRING,
            bodyContents,
            callback,
            converter
        )
    }

    override fun <T> executeAsync(
        userAgent: String?,
        headers: Map<String, String>,
        httpVerb: Verb,
        completeUrl: String,
        bodyContents: File,
        callback: OAuthAsyncRequestCallback<T>,
        converter: ResponseConverter<T>
    ): Future<T> {
        return doExecuteAsync(
            userAgent,
            headers,
            httpVerb,
            completeUrl,
            BodyType.FILE,
            bodyContents,
            callback,
            converter
        )
    }

    private fun <T> doExecuteAsync(
        userAgent: String?,
        headers: Map<String, String>,
        httpVerb: Verb,
        completeUrl: String,
        bodyType: BodyType,
        bodyContents: Any,
        callback: OAuthAsyncRequestCallback<T>,
        converter: ResponseConverter<T>
    ): Future<T> {
        val call =
            createCall(userAgent, headers, httpVerb, completeUrl, bodyType, bodyContents)
        val okHttpFuture = OkHttp3Future<T>(call)
        call.enqueue(
            OAuthAsyncCompletionHandler(
                callback,
                converter,
                okHttpFuture
            )
        )
        return okHttpFuture
    }

    @Throws(
        InterruptedException::class,
        ExecutionException::class,
        IOException::class
    )
    override fun execute(
        userAgent: String?,
        headers: Map<String, String>,
        httpVerb: Verb,
        completeUrl: String,
        bodyContents: ByteArray
    ): Response {
        return doExecute(
            userAgent,
            headers,
            httpVerb,
            completeUrl,
            BodyType.BYTE_ARRAY,
            bodyContents
        )
    }

    @Throws(
        InterruptedException::class,
        ExecutionException::class,
        IOException::class
    )
    override fun execute(
        userAgent: String?,
        headers: Map<String, String>,
        httpVerb: Verb,
        completeUrl: String,
        bodyContents: MultipartPayload
    ): Response {
        throw UnsupportedOperationException("OKHttpClient does not support Multipart payload for the moment")
    }

    @Throws(
        InterruptedException::class,
        ExecutionException::class,
        IOException::class
    )
    override fun execute(
        userAgent: String?,
        headers: Map<String, String>,
        httpVerb: Verb,
        completeUrl: String,
        bodyContents: String
    ): Response {
        return doExecute(
            userAgent,
            headers,
            httpVerb,
            completeUrl,
            BodyType.STRING,
            bodyContents
        )
    }

    @Throws(
        InterruptedException::class,
        ExecutionException::class,
        IOException::class
    )
    override fun execute(
        userAgent: String?,
        headers: Map<String, String>,
        httpVerb: Verb,
        completeUrl: String,
        bodyContents: File
    ): Response {
        return doExecute(
            userAgent,
            headers,
            httpVerb,
            completeUrl,
            BodyType.FILE,
            bodyContents
        )
    }

    @Throws(IOException::class)
    private fun doExecute(
        userAgent: String?,
        headers: Map<String, String>,
        httpVerb: Verb,
        completeUrl: String,
        bodyType: BodyType,
        bodyContents: Any
    ): Response {
        val call =
            createCall(userAgent, headers, httpVerb, completeUrl, bodyType, bodyContents)
        return convertResponse(call.execute())
    }

    private fun createCall(
        userAgent: String?,
        headers: Map<String, String>,
        httpVerb: Verb,
        completeUrl: String,
        bodyType: BodyType,
        bodyContents: Any?
    ): Call {
        val requestBuilder = Request.Builder()
        requestBuilder.url(completeUrl)
        val method = httpVerb.name

        // prepare body
        val body = if (bodyContents != null && HttpMethod.permitsRequestBody(method)) {
            val mediaType = headers[HttpClient.CONTENT_TYPE]?.let(MediaType::parse)
                ?: DEFAULT_CONTENT_TYPE_MEDIA_TYPE
            bodyType.createBody(mediaType, bodyContents)
        } else {
            null
        }

        // fill HTTP method and body
        requestBuilder.method(method, body)

        // fill headers
        for ((key, value) in headers) {
            requestBuilder.addHeader(key, value)
        }
        if (userAgent != null) {
            requestBuilder.header(OAuthConstants.USER_AGENT_HEADER_NAME, userAgent)
        }

        // create a new call
        return client.newCall(requestBuilder.build())
    }

    private enum class BodyType {
        BYTE_ARRAY {
            override fun createBody(
                mediaType: MediaType?,
                bodyContents: Any?
            ): RequestBody? {
                return RequestBody.create(mediaType, bodyContents as ByteArray)
            }
        },
        STRING {
            override fun createBody(
                mediaType: MediaType?,
                bodyContents: Any?
            ): RequestBody? {
                return RequestBody.create(mediaType, bodyContents as String)
            }
        },
        FILE {
            override fun createBody(
                mediaType: MediaType?,
                bodyContents: Any?
            ): RequestBody? {
                return RequestBody.create(mediaType, bodyContents as File)
            }
        };

        abstract fun createBody(
            mediaType: MediaType?,
            bodyContents: Any?
        ): RequestBody?
    }

    companion object {
        private val DEFAULT_CONTENT_TYPE_MEDIA_TYPE =
            MediaType.parse(HttpClient.DEFAULT_CONTENT_TYPE)

        fun convertResponse(okHttpResponse: okhttp3.Response): Response {
            val headers = okHttpResponse.headers()
            val headersMap: MutableMap<String, String?> =
                HashMap()
            for (headerName in headers.names()) {
                headersMap[headerName] = headers[headerName]
            }
            val body = okHttpResponse.body()
            return Response(
                okHttpResponse.code(), okHttpResponse.message(), headersMap,
                body?.byteStream()
            )
        }
    }
}
