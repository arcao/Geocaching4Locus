package com.arcao.geocaching4locus.data.api.internal.okhttp.interceptor

import com.arcao.geocaching4locus.data.account.AccountManager
import com.arcao.geocaching4locus.data.account.GeocachingAccount
import com.arcao.geocaching4locus.data.api.endpoint.GeocachingApiEndpoint
import com.arcao.geocaching4locus.data.api.model.User
import io.mockk.Called
import io.mockk.Ordering
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal object GeocachingAuthenticationInterceptorTest {
    private lateinit var account: GeocachingAccount
    private lateinit var interceptor: GeocachingAuthenticationInterceptor
    private lateinit var chain: Interceptor.Chain
    private lateinit var endpoint: GeocachingApiEndpoint

    @BeforeEach
    fun setup() {
        chain = mockkClass(Interceptor.Chain::class) {
            every { request() } returns Request.Builder().url("http://test").build()
            every { proceed(any()) } returns mockkClass(Response::class)
        }

        account = mockkClass(GeocachingAccount::class) {
            every { accessToken } returns "accessToken1234"
            coEvery { refreshToken() } returns true
            every { updateUserInfo(any()) } returns Unit
        }

        val accountManager = mockkClass(AccountManager::class) {
            every { account } returns this@GeocachingAuthenticationInterceptorTest.account
        }

        val user = mockkClass(User::class)

        endpoint = mockkClass(GeocachingApiEndpoint::class) {
            every { userAsync() } returns CompletableDeferred(user)
        }

        interceptor = GeocachingAuthenticationInterceptor(accountManager)
        interceptor.endpoint = endpoint
    }

    @Test
    fun verifyRefreshTokenCalled() {
        // given
        every { account.accessTokenExpired } returns true

        // under test
        interceptor.intercept(chain)

        // expected
        verify(timeout = 5000) { chain.proceed(any()) }
        coVerify(Ordering.ORDERED, timeout = 5000) {
            account.refreshToken()

            @Suppress("DeferredResultUnused")
            endpoint.userAsync()
        }
    }

    @Test
    fun verifyRefreshTokenNotCalled() {
        // given
        every { account.accessTokenExpired } returns false

        // under test
        interceptor.intercept(chain)

        // expected
        verify(timeout = 5000) { chain.proceed(any()) }
        coVerify(timeout = 5000) {
            account.refreshToken() wasNot Called
            endpoint.userAsync() wasNot Called
        }
    }

    @Test
    fun verifyAccessTokenInHeaderPresent() {
        // given
        every { account.accessTokenExpired } returns false

        // under test
        interceptor.intercept(chain)

        // expected
        verify {
            chain.proceed(match { it.header("authorization") == "bearer accessToken1234" })
        }
        coVerify { account.refreshToken() wasNot Called }
    }
}
