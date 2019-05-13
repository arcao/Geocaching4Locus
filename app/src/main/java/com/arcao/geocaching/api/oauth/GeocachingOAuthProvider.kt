package com.arcao.geocaching.api.oauth

import com.arcao.geocaching.api.oauth.services.ServerTimestampServiceImpl
import com.github.scribejava.core.builder.api.DefaultApi10a
import com.github.scribejava.core.exceptions.OAuthException
import com.github.scribejava.core.extractors.OAuth1AccessTokenExtractor
import com.github.scribejava.core.extractors.OAuth1RequestTokenExtractor
import com.github.scribejava.core.extractors.TokenExtractor
import com.github.scribejava.core.model.OAuth1AccessToken
import com.github.scribejava.core.model.OAuth1RequestToken
import com.github.scribejava.core.model.Response
import com.github.scribejava.core.services.TimestampService
import com.github.scribejava.core.utils.OAuthEncoder
import java.io.IOException
import java.util.regex.Pattern

open class GeocachingOAuthProvider : DefaultApi10a() {
    private val timestampService by lazy {
        ServerTimestampServiceImpl()
    }

    override fun getTimestampService(): TimestampService {
        return timestampService
    }

    protected open val oAuthUrl: String
        get() = OAUTH_URL

    override fun getRequestTokenExtractor(): TokenExtractor<OAuth1RequestToken> {
        return GeocachingRequestTokenExtractor()
    }

    override fun getAccessTokenExtractor(): TokenExtractor<OAuth1AccessToken> {
        return GeocachingAccessTokenExtractor()
    }

    override fun getRequestTokenEndpoint(): String {
        return oAuthUrl
    }

    override fun getAccessTokenEndpoint(): String {
        return oAuthUrl
    }

    override fun getAuthorizationBaseUrl(): String {
        return oAuthUrl
    }

    class Staging : GeocachingOAuthProvider() {
        override val oAuthUrl: String
            get() = OAUTH_URL

        companion object {
            private const val OAUTH_URL = "https://staging.geocaching.com/oauth/mobileoauth.ashx"
        }
    }

    private class GeocachingRequestTokenExtractor internal constructor() : OAuth1RequestTokenExtractor() {
        @Throws(IOException::class)
        override fun extract(response: Response): OAuth1RequestToken {
            checkError(response)
            return super.extract(response)
        }
    }

    private class GeocachingAccessTokenExtractor internal constructor() : OAuth1AccessTokenExtractor() {
        @Throws(IOException::class)
        override fun extract(response: Response): OAuth1AccessToken {
            checkError(response)
            return super.extract(response)
        }
    }

    companion object {
        private val ERROR_MESSAGE_REGEX = Pattern.compile("oauth_error_message=([^&]*)")
        private const val OAUTH_URL = "https://www.geocaching.com/oauth/mobileoauth.ashx"

        @Throws(IOException::class)
        internal fun checkError(response: Response) {
            val matcher = ERROR_MESSAGE_REGEX.matcher(response.body)
            if (matcher.find() && matcher.groupCount() >= 1) {
                throw OAuthException(OAuthEncoder.decode(matcher.group(1)))
            }
        }
    }
}
