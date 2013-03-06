package com.arcao.geocaching4locus.constants;

import org.osgi.framework.Version;

import android.net.Uri;

public interface AppConstants {
	static final boolean USE_PRODUCTION_CONFIGURATION = false;
	static final String STAGGING_CONFIGURATION = "com.arcao.geocaching.api.configuration.impl.StaggingConfiguration";
	static final String PRODUCTION_CONFIGURATION = "com.arcao.geocaching.api.configuration.impl.ProductionConfiguration";
  
	static final String STAGGING_COMPRESSION_PROXY_SERVICE_URL = "http://wherigo-service.appspot.com/stagging_geocaching_proxy";
	static final String COMPRESSION_PROXY_SERVICE_URL = "http://wherigo-service.appspot.com/geocaching_proxy";

	static final String GEOCACHING_WEBSITE_URL = "http://www.geocaching.com/";
  static final String OAUTH_CALLBACK_URL = "x-locus://oauth.callback/callback/geocaching";

	static final int CACHES_PER_REQUEST = 10;

	static final String ERROR_FORM_KEY = "dFJfSDQzTlI2ZzhxaFJndm1MYjhkWHc6MQ";

	static final Uri MANUAL_URI = Uri.parse("http://geocaching4locus.eu/manual/");
	static final Uri WEBSITE_URI = Uri.parse("http://geocaching4locus.eu/");
	static final String DONATE_PAYPAL_URI = "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=arcao%%40arcao%%2ecom&lc=CZ&item_name=Geocaching4Locus&item_number=g4l&currency_code=%s&bn=PP%%2dDonationsBF%%3abtn_donateCC_LG%%2egif%%3aNonHosted";
	static final Uri GEOCACHING_LIVE_URI = Uri.parse("http://www.geocaching.com/live");
	
	/** OAuth response parameter that contains error message - specific Groundspeak extension */
	static final String OAUTH_ERROR_MESSAGE_PARAMETER = "oauth_error_message";
	
	static final Version LOCUS_MIN_VERSION = Version.parseVersion("2.8.7");
	static final String GOOGLE_PLAY_PREFIX = "https://play.google.com/store/apps/details?id=";
	static final Uri ANDROIDPIT_LOCUS_FREE_LINK = Uri.parse("http://www.androidpit.com/en/android/market/apps/app/menion.android.locus/Locus-Free");
	static final Uri ANDROIDPIT_LOCUS_PRO_LINK = Uri.parse("http://www.androidpit.com/en/android/market/apps/app/menion.android.locus.pro/Locus-Pro");
	
	/** Login dialog visible state */
	static final String STATE_AUTHENTICATOR_ACTIVITY_VISIBLE = "AUTHENTICATOR_ACTIVITY_VISIBLE";
}
