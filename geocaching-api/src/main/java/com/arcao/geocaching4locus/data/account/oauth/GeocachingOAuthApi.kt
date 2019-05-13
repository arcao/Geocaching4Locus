package com.arcao.geocaching4locus.data.account.oauth

import com.github.scribejava.core.builder.api.DefaultApi20
import com.github.scribejava.core.oauth2.bearersignature.BearerSignature
import com.github.scribejava.core.oauth2.bearersignature.BearerSignatureAuthorizationRequestHeaderField
import com.github.scribejava.core.oauth2.clientauthentication.ClientAuthentication
import com.github.scribejava.core.oauth2.clientauthentication.RequestBodyAuthenticationScheme

open class GeocachingOAuthApi : DefaultApi20() {
    override fun getAuthorizationBaseUrl(): String = "https://www.geocaching.com/oauth/authorize.aspx"
    override fun getAccessTokenEndpoint(): String = "https://oauth.geocaching.com/token"
    override fun getClientAuthentication(): ClientAuthentication = RequestBodyAuthenticationScheme.instance()
    override fun getBearerSignature(): BearerSignature = BearerSignatureAuthorizationRequestHeaderField.instance()

    class Staging : GeocachingOAuthApi() {
        override fun getAuthorizationBaseUrl(): String = "https://staging.geocaching.com/oauth/authorize.aspx"
        override fun getAccessTokenEndpoint(): String = "https://oauth-staging.geocaching.com/token"
    }
}