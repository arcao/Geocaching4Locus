package com.arcao.geocaching4locus.constants;

import org.osgi.framework.Version;

import android.net.Uri;

public interface AppConstants {
  static final String OAUTH_CONSUMER_KEY = "90C7F340-7998-477D-B4D3-AC48A9A0F560";
  static final String OAUTH_CONSUMER_SECRET = "55BBC3C1-24CF-4D1B-B7EC-7A8E75DAB7D1";

  static final String OAUTH_AUTHORIZE_URL = "https://www.geocaching.com/oauth/mobileoauth.ashx";
  static final String OAUTH_REQUEST_URL = OAUTH_AUTHORIZE_URL;
  static final String OAUTH_ACCESS_URL = OAUTH_AUTHORIZE_URL;
  
  static final String OAUTH_CALLBACK_URL = "x-g4l://oauth.callback/callback";
	static final String COMPRESSION_PROXY_SERVICE_URL = "http://wherigo-service.appspot.com/geocaching_proxy";
	static final String GEOCACHING_WEBSITE_URL = "http://www.geocaching.com/";

	static final int CACHES_PER_REQUEST = 10;

	static final String ERROR_FORM_KEY = "dFJfSDQzTlI2ZzhxaFJndm1MYjhkWHc6MQ";

	static final Uri MANUAL_URI = Uri.parse("http://geocaching4locus.eu/manual/");
	static final Uri WEBSITE_URI = Uri.parse("http://geocaching4locus.eu/");
	static final String DONATE_PAYPAL_URI = "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=arcao%%40arcao%%2ecom&lc=CZ&item_name=Geocaching4Locus&item_number=g4l&currency_code=%s&bn=PP%%2dDonationsBF%%3abtn_donateCC_LG%%2egif%%3aNonHosted";
	
	/** OAuth response parameter that contains error message - specific Groundspeak extension */
	static final String OAUTH_ERROR_MESSAGE_PARAMETER = "oauth_error_message";
	
	static final Version LOCUS_MIN_VERSION = Version.parseVersion("2.8.3.2");
	static final String GOOGLE_PLAY_PREFIX = "https://play.google.com/store/apps/details?id=";
	static final Uri ANDROIDPIT_LOCUS_FREE_LINK = Uri.parse("http://www.androidpit.com/en/android/market/apps/app/menion.android.locus/Locus-Free");
	static final Uri ANDROIDPIT_LOCUS_PRO_LINK = Uri.parse("http://www.androidpit.com/en/android/market/apps/app/menion.android.locus.pro/Locus-Pro");
}
