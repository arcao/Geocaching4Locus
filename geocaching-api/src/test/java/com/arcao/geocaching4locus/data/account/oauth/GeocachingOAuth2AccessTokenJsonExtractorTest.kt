package com.arcao.geocaching4locus.data.account.oauth

import com.github.scribejava.core.model.OAuth2AccessTokenErrorResponse
import com.github.scribejava.core.oauth2.OAuth2Error
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class GeocachingOAuth2AccessTokenJsonExtractorTest {

    @Test
    fun generateError_verifyErrorParsed() {
        @Language("JSON")
        val errorJson =
            "{\"statusCode\":\"BadRequest\",\"errorMessage\":\"invalid_grant\",\"errors\":[{\"message\":\"invalid_grant\",\"detail\":\"the provided code is invalid, used, expired, or revoked\"}]}"

        val exception = Assertions.assertThrows(OAuth2AccessTokenErrorResponse::class.java) {
            GeocachingOAuth2AccessTokenJsonExtractor.generateError(errorJson)
        }

        // expected
        Assertions.assertEquals(OAuth2Error.INVALID_GRANT, exception.error)
        Assertions.assertEquals(
            "the provided code is invalid, used, expired, or revoked",
            exception.errorDescription
        )
        Assertions.assertNull(exception.errorUri)
    }
}
