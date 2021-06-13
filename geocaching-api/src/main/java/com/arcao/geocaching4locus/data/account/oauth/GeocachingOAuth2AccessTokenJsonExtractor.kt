package com.arcao.geocaching4locus.data.account.oauth

import com.fasterxml.jackson.core.JsonProcessingException
import com.github.scribejava.core.extractors.OAuth2AccessTokenJsonExtractor
import com.github.scribejava.core.model.OAuth2AccessTokenErrorResponse
import com.github.scribejava.core.model.Response
import com.github.scribejava.core.oauth2.OAuth2Error
import java.net.URI

object GeocachingOAuth2AccessTokenJsonExtractor : OAuth2AccessTokenJsonExtractor() {
    private const val FIELD_ERRORS = "errors"
    private const val FIELD_ERROR_MESSAGE = "errorMessage"
    private const val FIELD_DETAIL = "detail"
    private const val FIELD_ERROR_URI = "error_uri"

    override fun generateError(response: Response?) {
        val responseBody = response?.body ?: return

        val jsonBody = try {
            OBJECT_MAPPER.readTree(responseBody)
        } catch (e: JsonProcessingException) {
            throw OAuth2AccessTokenErrorResponse(null, null, null, response)
        }

        val errorUri = try {
            jsonBody.get(FIELD_ERROR_URI)?.asText()?.let(URI::create)
        } catch (e: IllegalArgumentException) {
            null
        }

        val errorCode = try {
            OAuth2Error.parseFrom(
                extractRequiredParameter(
                    jsonBody,
                    FIELD_ERROR_MESSAGE,
                    responseBody
                ).asText()
            )
        } catch (e: IllegalArgumentException) {
            // non oauth standard error code
            null
        }

        val detail = jsonBody.get(FIELD_ERRORS)?.get(0)?.get(FIELD_DETAIL)?.asText()

        throw OAuth2AccessTokenErrorResponse(errorCode, detail, errorUri, response)
    }
}
