package com.arcao.geocaching4locus.data.account.oauth

import com.github.scribejava.core.builder.api.DefaultApi20
import com.github.scribejava.core.httpclient.HttpClient
import com.github.scribejava.core.httpclient.HttpClientConfig
import com.github.scribejava.core.model.OAuthConstants
import com.github.scribejava.core.model.OAuthRequest
import com.github.scribejava.core.oauth.OAuth20Service
import java.io.OutputStream

class GeocachingOAuthService(
    api: DefaultApi20,
    apiKey: String?,
    apiSecret: String?,
    callback: String?,
    defaultScope: String?,
    responseType: String?,
    debugStream: OutputStream?,
    userAgent: String?,
    httpClientConfig: HttpClientConfig?,
    httpClient: HttpClient?
) : OAuth20Service(
    api,
    apiKey,
    apiSecret,
    callback,
    defaultScope,
    responseType,
    debugStream,
    userAgent,
    httpClientConfig,
    httpClient
) {
    override fun createRefreshTokenRequest(refreshToken: String, scope: String?): OAuthRequest =
        super.createRefreshTokenRequest(refreshToken, scope).apply {
            addParameter(OAuthConstants.REDIRECT_URI, callback)
        }
}
