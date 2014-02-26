package com.arcao.geocaching.api.impl;

import com.arcao.geocaching.api.configuration.OAuthGeocachingApiConfiguration;
import com.arcao.geocaching.api.configuration.resolver.GeocachingApiConfigurationResolver;
import com.arcao.geocaching.api.exception.GeocachingApiException;
import com.arcao.geocaching4locus.constants.AppConstants;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;

public class LiveGeocachingApiFactory {
	private static LiveGeocachingApi liveGeocachingApi;
	private static OAuthConsumer oAuthConsumer;
	private static OAuthProvider oAuthProvider;

	public static LiveGeocachingApi getLiveGeocachingApi() throws GeocachingApiException {
		if (liveGeocachingApi == null) {
			liveGeocachingApi = new LiveGeocachingApi(getConfiguration());
		}

		return liveGeocachingApi;
	}

	public static OAuthConsumer getOAuthConsumer() throws GeocachingApiException {
		if (oAuthConsumer == null) {
			OAuthGeocachingApiConfiguration configuration = getConfiguration();
			oAuthConsumer = new CommonsHttpOAuthConsumer(configuration.getConsumerKey(), configuration.getConsumerSecret());
		}

		return oAuthConsumer;
	}

	public static OAuthProvider getOAuthProvider() throws GeocachingApiException {
		if (oAuthProvider == null) {
			OAuthGeocachingApiConfiguration configuration = getConfiguration();
			oAuthProvider = new CommonsHttpOAuthProvider(configuration.getOAuthRequestUrl(), configuration.getOAuthAccessUrl(), configuration.getOAuthAuthorizeUrl());
			// always use OAuth 1.0a
			oAuthProvider.setOAuth10a(true);
		}

		return oAuthProvider;
	}

	private static OAuthGeocachingApiConfiguration getConfiguration() throws GeocachingApiException {
		OAuthGeocachingApiConfiguration configuration;

		if (AppConstants.USE_PRODUCTION_CONFIGURATION) {
			configuration = GeocachingApiConfigurationResolver.resolve(OAuthGeocachingApiConfiguration.class, AppConstants.PRODUCTION_CONFIGURATION_CLASS);
		} else {
			configuration = GeocachingApiConfigurationResolver.resolve(OAuthGeocachingApiConfiguration.class, AppConstants.STAGGING_CONFIGURATION_CLASS);
		}

		if (configuration == null) {
			throw new GeocachingApiException("GeocachingApi configuration class wasn't found.");
		}

		return configuration;
	}
}
