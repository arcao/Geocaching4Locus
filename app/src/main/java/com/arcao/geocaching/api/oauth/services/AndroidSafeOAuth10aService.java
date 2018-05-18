package com.arcao.geocaching.api.oauth.services;

import com.github.scribejava.core.builder.api.DefaultApi10a;
import com.github.scribejava.core.httpclient.HttpClient;
import com.github.scribejava.core.httpclient.HttpClientConfig;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.model.OAuthConstants;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.oauth.OAuth10aService;

import java.io.OutputStream;

public class AndroidSafeOAuth10aService extends OAuth10aService {
    public AndroidSafeOAuth10aService(DefaultApi10a api, String apiKey, String apiSecret, String callback, String scope, OutputStream debugStream, String state, String responseType, String userAgent, HttpClientConfig httpClientConfig, HttpClient httpClient) {
        super(api, apiKey, apiSecret, callback, scope, debugStream, state, responseType, userAgent, httpClientConfig, httpClient);
    }

    protected OAuthRequest prepareAccessTokenRequest(OAuth1RequestToken requestToken, String oauthVerifier) {
        final OAuthRequest request = new OAuthRequest(getApi().getAccessTokenVerb(), getApi().getAccessTokenEndpoint());
        request.addOAuthParameter(OAuthConstants.TOKEN, requestToken.getToken());
        request.addOAuthParameter(OAuthConstants.VERIFIER, oauthVerifier);
        // FIXME this cause crash on Android devices with SDK lower than 19
        //log("setting token to: " + requestToken + " and verifier to: " + oauthVerifier);
        addOAuthParams(request, requestToken.getTokenSecret());
        appendSignature(request);
        return request;
    }
}
