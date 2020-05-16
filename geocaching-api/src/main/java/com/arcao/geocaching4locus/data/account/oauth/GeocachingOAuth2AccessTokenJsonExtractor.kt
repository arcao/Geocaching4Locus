package com.arcao.geocaching4locus.data.account.oauth

import com.github.scribejava.core.extractors.OAuth2AccessTokenJsonExtractor
import com.github.scribejava.core.model.OAuth2AccessTokenErrorResponse
import com.github.scribejava.core.oauth2.OAuth2Error
import java.net.URI

object GeocachingOAuth2AccessTokenJsonExtractor : OAuth2AccessTokenJsonExtractor() {
    private const val FIELD_ERROR_MESSAGE = "errorMessage"
    private const val FIELD_DETAiL = "detail"
    private const val FIELD_ERROR_URI = "error_uri"

    override fun generateError(rawResponse: String) {
        val response = OBJECT_MAPPER.readTree(rawResponse)

        val errorMessage = extractRequiredParameter(response, FIELD_ERROR_MESSAGE, rawResponse)
            .asText()
        val detail = response.get(FIELD_DETAiL)?.asText()
        val errorUriString = response.get(FIELD_ERROR_URI)?.asText()

        val errorUri = try {
            if (errorUriString == null) null else URI.create(errorUriString)
        } catch (e: IllegalArgumentException) {
            null
        }

        val errorCode = try {
            OAuth2Error.parseFrom(errorMessage)
        } catch (e: IllegalArgumentException) {
            // non oauth standard error code
            null
        }

        throw OAuth2AccessTokenErrorResponse(errorCode, detail, errorUri, rawResponse)
    }
}
