package com.arcao.geocaching4locus.data.account.oauth

import com.github.scribejava.core.extractors.OAuth2AccessTokenJsonExtractor
import com.github.scribejava.core.model.OAuth2AccessTokenErrorResponse
import java.net.URI
import java.util.regex.Pattern

object GeocachingOAuth2AccessTokenJsonExtractor : OAuth2AccessTokenJsonExtractor() {
    private val ERROR_REGEX_PATTERN = Pattern.compile("\"errorMessage\"\\s*:\\s*\"(\\S*?)\"")
    private val ERROR_DESCRIPTION_REGEX_PATTERN = Pattern.compile("\"detail\"\\s*:\\s*\"([^\"]*?)\"")
    private val ERROR_URI_REGEX_PATTERN = Pattern.compile("\"error_uri\"\\s*:\\s*\"(\\S*?)\"")

    override fun generateError(response: String?) {
        val errorInString = extractParameter(response, ERROR_REGEX_PATTERN, true)
        val errorDescription = extractParameter(response, ERROR_DESCRIPTION_REGEX_PATTERN, false)
        val errorUriInString = extractParameter(response, ERROR_URI_REGEX_PATTERN, false)

        val errorUri = try {
            if (errorUriInString == null) null else URI.create(errorUriInString)
        } catch (e: IllegalArgumentException) {
            null
        }

        val errorCode = try {
            OAuth2AccessTokenErrorResponse.ErrorCode.valueOf(errorInString)
        } catch (e: IllegalArgumentException) {
            // non oauth standard error code
            null
        }

        throw OAuth2AccessTokenErrorResponse(errorCode, errorDescription, errorUri, response)
    }
}