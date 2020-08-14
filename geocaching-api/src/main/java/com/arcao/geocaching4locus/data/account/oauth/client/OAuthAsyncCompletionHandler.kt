package com.arcao.geocaching4locus.data.account.oauth.client

import com.github.scribejava.core.model.OAuthAsyncRequestCallback
import com.github.scribejava.core.model.OAuthRequest.ResponseConverter
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

internal class OAuthAsyncCompletionHandler<T>(
    private val callback: OAuthAsyncRequestCallback<T>?,
    private val converter: ResponseConverter<T>?,
    private val okHttpFuture: OkHttp3Future<T>
) : Callback {
    override fun onFailure(call: Call, exception: IOException) {
        try {
            okHttpFuture.setException(exception)
            callback?.onThrowable(exception)
        } finally {
            okHttpFuture.finish()
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onResponse(
        call: Call,
        okHttpResponse: Response
    ) {
        try {
            val response =
                OkHttp3HttpClient.convertResponse(okHttpResponse)
            try {
                val t = if (converter == null) response as T else converter.convert(response)
                okHttpFuture.setResult(t)
                callback?.onCompleted(t)
            } catch (e: IOException) {
                okHttpFuture.setException(e)
                callback?.onThrowable(e)
            } catch (e: RuntimeException) {
                okHttpFuture.setException(e)
                callback?.onThrowable(e)
            }
        } finally {
            okHttpFuture.finish()
        }
    }
}
