package com.arcao.geocaching4locus.data.api

import com.arcao.geocaching4locus.data.api.exception.AuthenticationException
import com.arcao.geocaching4locus.data.api.exception.GeocachingApiException
import com.arcao.geocaching4locus.data.api.model.enums.StatusCode
import io.mockk.coVerify
import io.mockk.every
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

internal class GeocachingApiRepositoryTest : GeocachingApiRepositoryBaseTest() {
    @Test
    fun verifyRefreshTokenCalled() {
        // given
        // first is expired to call refresh
        // second is not expired to prevent infinite loop in GeocachingAuthenticationInterceptor
        every { account.accessTokenExpired } returns true andThen false

        // required for GeocachingAuthenticationInterceptor
        server.enqueue(MockResponse().loadJsonBody("user-me-valid"))
        // required for test
        server.enqueue(MockResponse().loadJsonBody("user-me-valid"))

        // under test
        runBlocking {
            repository.user()
        }

        // expected
        coVerify {
            account.refreshToken()
        }
    }

    @Test
    fun verifyAuthorizationExceptionThrown() {
        // given
        server.enqueue(MockResponse().authorizationException(404, "error", "description"))

        // under test
        val exception = assertThrows(AuthenticationException::class.java) {
            runBlocking {
                repository.user()
            }
        }

        // expected
        assertEquals(StatusCode.NOT_FOUND, exception.statusCode)
        assertEquals("error", exception.statusMessage)
        assertEquals("description", exception.errorMessage)
    }

    @Test
    fun verifyGeocachingApiExceptionThrown() {
        // given
        server.enqueue(MockResponse().apiException(StatusCode.FORBIDDEN, "Forbidden", "user is forbidden"))

        // under test
        val exception = assertThrows(GeocachingApiException::class.java) {
            runBlocking {
                repository.user()
            }
        }

        // expected
        assertEquals(StatusCode.FORBIDDEN, exception.statusCode)
        assertEquals("Forbidden", exception.statusMessage)
        assertEquals("user is forbidden", exception.errorMessage)
    }

    @Test
    fun verifyTotalCount() {
        // given
        server.enqueue(MockResponse().totalCount(1234).loadJsonBody("geocache-logs-gc12345-valid"))

        // under test
        val logs = runBlocking {
            repository.geocacheLogs("GC1234")
        }

        // expected
        assertNotNull(logs)
        assertEquals(10, logs.size)
        assertEquals(1234, logs.totalCount)
    }
}