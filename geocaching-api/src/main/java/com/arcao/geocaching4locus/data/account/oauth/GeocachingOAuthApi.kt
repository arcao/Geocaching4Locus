package com.arcao.geocaching4locus.data.account.oauth

import com.github.scribejava.core.builder.api.DefaultApi20
import com.github.scribejava.core.extractors.TokenExtractor
import com.github.scribejava.core.httpclient.HttpClient
import com.github.scribejava.core.httpclient.HttpClientConfig
import com.github.scribejava.core.model.OAuth2AccessToken
import com.github.scribejava.core.oauth.OAuth20Service
import com.github.scribejava.core.oauth2.bearersignature.BearerSignature
import com.github.scribejava.core.oauth2.bearersignature.BearerSignatureAuthorizationRequestHeaderField
import com.github.scribejava.core.oauth2.clientauthentication.ClientAuthentication
import com.github.scribejava.core.oauth2.clientauthentication.RequestBodyAuthenticationScheme
import java.io.OutputStream

open class GeocachingOAuthApi : DefaultApi20() {
    override fun getAuthorizationBaseUrl(): String =
        "https://www.geocaching.com/oauth/authorize.aspx"

    override fun getAccessTokenEndpoint(): String = "https://oauth.geocaching.com/token"
    override fun getClientAuthentication(): ClientAuthentication =
        RequestBodyAuthenticationScheme.instance()

    override fun getBearerSignature(): BearerSignature =
        BearerSignatureAuthorizationRequestHeaderField.instance()

    override fun getAccessTokenExtractor(): TokenExtractor<OAuth2AccessToken> =
        GeocachingOAuth2AccessTokenJsonExtractor

    override fun createService(
        apiKey: String?,
        apiSecret: String?,
        callback: String?,
        defaultScope: String?,
        responseType: String?,
        debugStream: OutputStream?,
        userAgent: String?,
        httpClientConfig: HttpClientConfig?,
        httpClient: HttpClient?
    ): OAuth20Service {
        return GeocachingOAuthService(
            this,
            apiKey,
            apiSecret,
            callback,
            defaultScope,
            responseType,
            debugStream,
            userAgent,
            httpClientConfig,
            httpClient
        )
    }

    class Staging : GeocachingOAuthApi() {
        override fun getAuthorizationBaseUrl(): String =
            "https://staging.geocaching.com/oauth/authorize.aspx"

        override fun getAccessTokenEndpoint(): String = "https://oauth-staging.geocaching.com/token"
    }
}
