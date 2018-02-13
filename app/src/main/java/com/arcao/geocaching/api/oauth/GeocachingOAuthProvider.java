package com.arcao.geocaching.api.oauth;

import com.arcao.geocaching.api.oauth.services.ServerTimestampServiceImpl;
import com.github.scribejava.core.builder.api.DefaultApi10a;
import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.extractors.OAuth1AccessTokenExtractor;
import com.github.scribejava.core.extractors.OAuth1RequestTokenExtractor;
import com.github.scribejava.core.extractors.TokenExtractor;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.services.TimestampService;
import com.github.scribejava.core.utils.OAuthEncoder;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeocachingOAuthProvider extends DefaultApi10a {
    private static final Pattern ERROR_MESSAGE_REGEX = Pattern.compile("oauth_error_message=([^&]*)");
    private static final String OAUTH_URL = "https://www.geocaching.com/oauth/mobileoauth.ashx";
    private TimestampService timestampService;

    @Override
    public TokenExtractor<OAuth1RequestToken> getRequestTokenExtractor() {
        return new GeocachingRequestTokenExtractor();
    }

    @Override
    public TokenExtractor<OAuth1AccessToken> getAccessTokenExtractor() {
        return new GeocachingAccessTokenExtractor();
    }

    @Override
    public TimestampService getTimestampService() {
        if (timestampService == null)
            timestampService = new ServerTimestampServiceImpl();
        return timestampService;
    }

    protected String getOAuthUrl() {
        return OAUTH_URL;
    }

    @Override
    public String getRequestTokenEndpoint() {
        return getOAuthUrl();
    }

    @Override
    public String getAccessTokenEndpoint() {
        return getOAuthUrl();
    }

    @Override
    public String getAuthorizationUrl(OAuth1RequestToken requestToken) {
        return getOAuthUrl() + "?oauth_token=" + OAuthEncoder.encode(requestToken.getToken());
    }

    private static void checkError(Response response) throws IOException {
        Matcher matcher = ERROR_MESSAGE_REGEX.matcher(response.getBody());
        if (matcher.find() && matcher.groupCount() >= 1) {
            throw new OAuthException(OAuthEncoder.decode(matcher.group(1)));
        }
    }

    public static class Staging extends GeocachingOAuthProvider {
        private static final String OAUTH_URL = "https://staging.geocaching.com/oauth/mobileoauth.ashx";

        @Override
        protected String getOAuthUrl() {
            return OAUTH_URL;
        }
    }

    private static class GeocachingRequestTokenExtractor extends OAuth1RequestTokenExtractor {
        GeocachingRequestTokenExtractor() {
        }

        @Override
        public OAuth1RequestToken extract(Response response) throws IOException {
            checkError(response);
            return super.extract(response);
        }
    }

    private static class GeocachingAccessTokenExtractor extends OAuth1AccessTokenExtractor {
        GeocachingAccessTokenExtractor() {
        }

        @Override
        public OAuth1AccessToken extract(Response response) throws IOException {
            checkError(response);
            return super.extract(response);
        }
    }
}
