package com.arcao.geocaching4locus.data.api.internal.okhttp.interceptor

import com.arcao.geocaching4locus.data.account.AccountManager
import com.arcao.geocaching4locus.data.api.endpoint.GeocachingApiEndpoint
import com.arcao.geocaching4locus.data.api.exception.InvalidResponseException
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class GeocachingAuthenticationInterceptor(private val accountManager: AccountManager) : Interceptor {
    var endpoint: GeocachingApiEndpoint? = null

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        try {
            val endpoint = this.endpoint ?: throw IllegalStateException("Endpoint must be set.")
            val account = accountManager.account ?: throw IllegalStateException("Account is not present.")

            if (account.accessTokenExpired) {
                runBlocking {
                    account.refreshToken()
                    account.updateUserInfo(endpoint.user().await())
                }
            }
            return chain.proceed(chain.request().newBuilder().addHeader("Authorization", "bearer ${account.accessToken}").build())
        } catch (e : IOException) {
            throw e
        } catch (t: Throwable) {
            throw InvalidResponseException(t.message, t)
        }
    }
}