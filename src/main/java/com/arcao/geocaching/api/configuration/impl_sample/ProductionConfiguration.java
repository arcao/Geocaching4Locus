package com.arcao.geocaching.api.configuration.impl_sample;

import com.arcao.geocaching.api.configuration.OAuthGeocachingApiConfiguration;
import com.arcao.geocaching.api.configuration.impl.DefaultProductionGeocachingApiConfiguration;

public class ProductionConfiguration  extends DefaultProductionGeocachingApiConfiguration implements OAuthGeocachingApiConfiguration {
	private static final String OAUTH_URL = "https://www.geocaching.com/oauth/mobileoauth.ashx";

	private static final String CONSUMER_KEY = "YOUR_OAUTH_KEY";
	private static final String CONSUMER_SECRET = "YOUR_OAUTH_SECRET";

	public String getConsumerKey() {
		return CONSUMER_KEY;
	}

	public String getConsumerSecret() {
		return CONSUMER_SECRET;
	}

	public String getOAuthRequestUrl() {
		return OAUTH_URL;
	}

	public String getOAuthAuthorizeUrl() {
		return OAUTH_URL;
	}

	public String getOAuthAccessUrl() {
		return OAUTH_URL;
	}
}
