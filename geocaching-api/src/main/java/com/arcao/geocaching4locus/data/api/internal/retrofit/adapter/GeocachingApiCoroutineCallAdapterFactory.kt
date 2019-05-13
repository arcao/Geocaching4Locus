package com.arcao.geocaching4locus.data.api.internal.retrofit.adapter

import com.arcao.geocaching4locus.data.api.exception.AuthenticationException
import com.arcao.geocaching4locus.data.api.exception.GeocachingApiException
import com.arcao.geocaching4locus.data.api.exception.InvalidResponseException
import com.arcao.geocaching4locus.data.api.model.enum.StatusCode
import com.arcao.geocaching4locus.data.api.model.response.Error
import com.arcao.geocaching4locus.data.api.model.response.MutableTotalCountList
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import retrofit2.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.regex.Pattern

class GeocachingApiCoroutineCallAdapterFactory private constructor() : CallAdapter.Factory() {
    companion object {
        fun create() = GeocachingApiCoroutineCallAdapterFactory()

        private val WWW_AUTHENTICATE_ERROR_PATTERN: Pattern by lazy {
            Pattern.compile("\\s*bearer\\s*realm\\s*=\\s*(\")?.+\\1\\s*,\\s*error\\s*=\\s*(\")?([^\"]+)\\2(?:\\s*,\\s*error_descriptions*=s*(\")?([^\"]+)\\4)?", Pattern.CASE_INSENSITIVE)
        }
    }


    override fun get(returnType: Type, annotations: Array<out Annotation>, retrofit: Retrofit): CallAdapter<*, *>? {
        if (Deferred::class.java != getRawType(returnType)) {
            return null
        }
        if (returnType !is ParameterizedType) {
            throw IllegalStateException(
                    "Deferred return type must be parameterized as Deferred<Foo> or Deferred<out Foo>")
        }

        return BodyCallAdapter<Any>(getParameterUpperBound(0, returnType), retrofit)
    }

    private class BodyCallAdapter<T>(private val responseType: Type, private val retrofit: Retrofit) : CallAdapter<T, Deferred<T>> {
        override fun responseType() = responseType

        override fun adapt(call: Call<T>): Deferred<T> {
            val deferred = CompletableDeferred<T>()

            deferred.invokeOnCompletion {
                if (deferred.isCancelled) {
                    call.cancel()
                }
            }

            call.enqueue(object : Callback<T> {
                override fun onFailure(call: Call<T>, t: Throwable) {
                    if (t is JsonDataException || t is JsonEncodingException) {
                        deferred.completeExceptionally(InvalidResponseException(t.message, t))
                        return
                    }

                    deferred.completeExceptionally(t)
                }

                override fun onResponse(call: Call<T>, response: Response<T>) {
                    if (response.isSuccessful) {
                        handleTotalCount(response)
                        deferred.complete(response.body()!!)
                    } else {
                        deferred.completeExceptionally(handleResponseError(response))
                    }
                }
            })

            return deferred
        }

        private fun handleResponseError(response: Response<T>): Throwable {
            // try to handle authentication errors
            val authenticateHeader = response.headers()["www-authenticate"]
            if (authenticateHeader != null) {
                val matcher = WWW_AUTHENTICATE_ERROR_PATTERN.matcher(authenticateHeader)

                if (matcher.find()) {
                    val code = matcher.group(3)
                    val message = when {
                        matcher.groupCount() >= 5 -> matcher.group(5)
                        else -> null
                    }

                    return AuthenticationException(code, message)
                }
            }

            // try handle errorBody
            val errorBody = response.errorBody()
            if (errorBody != null) {
                try {
                    val error = retrofit.responseBodyConverter<Error>(Error::class.java, emptyArray()).convert(errorBody)
                    if (error != null) {
                        return GeocachingApiException(error.statusCode, error.statusMessage, error.errorMessage)
                    }
                } catch (t: Throwable) {
                    // ignore and fallback to HttpException
                }
            }

            // try to handle known API error codes
            val statusCode = StatusCode.from(response.code())
            if (statusCode != null) {
                return GeocachingApiException(statusCode, response.message(), "")
            }

            return HttpException(response)
        }

        private fun handleTotalCount(response: Response<T>) {
            val headers = response.headers()
            val body = response.body()

            if (body is MutableTotalCountList<*> && headers["x-total-count"] != null) {
                body.totalCount = headers["x-total-count"]?.toLongOrNull() ?: body.totalCount
            }
        }
    }
}