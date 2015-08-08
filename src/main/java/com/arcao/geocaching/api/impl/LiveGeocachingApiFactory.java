package com.arcao.geocaching.api.impl;

import com.arcao.geocaching.api.configuration.impl.DefaultStagingGeocachingApiConfiguration;
import com.arcao.geocaching.api.exception.GeocachingApiException;
import com.arcao.geocaching4locus.BuildConfig;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;

public class LiveGeocachingApiFactory {
	private static final String OAUTH_URL_PRODUCTION = "https://www.geocaching.com/oauth/mobileoauth.ashx";
	private static final String OAUTH_URL_STAGING = "https://staging.geocaching.com/oauth/mobileoauth.ashx";

	private static LiveGeocachingApi liveGeocachingApi;
	private static OAuthConsumer oAuthConsumer;
	private static OAuthProvider oAuthProvider;

	public static LiveGeocachingApi getLiveGeocachingApi() throws GeocachingApiException {
		if (liveGeocachingApi == null) {
			if (BuildConfig.GEOCACHING_API_STAGING) {
				liveGeocachingApi = new LiveGeocachingApi.Builder().setConfiguration(new DefaultStagingGeocachingApiConfiguration()).build();
			} else {
				liveGeocachingApi = new LiveGeocachingApi.Builder().build();
			}
		}

		return liveGeocachingApi;
	}

	public static OAuthConsumer getOAuthConsumer() throws GeocachingApiException {
		if (oAuthConsumer == null) {
			oAuthConsumer = new CommonsHttpOAuthConsumer(BuildConfig.GEOCACHING_API_KEY, BuildConfig.GEOCACHING_API_SECRET);
		}

		return oAuthConsumer;
	}

	public static OAuthProvider getOAuthProvider() throws GeocachingApiException {
		if (oAuthProvider == null) {
			final String oAuthUrl = (BuildConfig.GEOCACHING_API_STAGING) ? OAUTH_URL_STAGING : OAUTH_URL_PRODUCTION;

			oAuthProvider = new CommonsHttpOAuthProvider(oAuthUrl, oAuthUrl, oAuthUrl);
			// always use OAuth 1.0a
			oAuthProvider.setOAuth10a(true);
		}

		return oAuthProvider;
	}
}
