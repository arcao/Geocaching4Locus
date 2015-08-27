package com.arcao.geocaching4locus.constants;

import android.net.Uri;

import org.osgi.framework.Version;

public interface AppConstants {
	String OAUTH_CALLBACK_URL = "x-locus://oauth.callback/callback/geocaching";

	Uri MANUAL_URI = Uri.parse("http://geocaching4locus.eu/manual/");
	Uri WEBSITE_URI = Uri.parse("http://geocaching4locus.eu/");
	Uri GEOCACHING_LIVE_URI = Uri.parse("http://www.geocaching.com/live");
	String DONATE_PAYPAL_URI = "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=arcao%%40arcao%%2ecom&lc=CZ&item_name=Geocaching4Locus&item_number=g4l&currency_code=%s&bn=PP%%2dDonationsBF%%3abtn_donateCC_LG%%2egif%%3aNonHosted";

	Version LOCUS_MIN_VERSION = Version.parseVersion("3.7.0");

	/* Adaptive downloading configuration */
	int CACHES_PER_REQUEST = 10;
	int ADAPTIVE_DOWNLOADING_MIN_CACHES = CACHES_PER_REQUEST;
	int ADAPTIVE_DOWNLOADING_MAX_CACHES = 50;
	int ADAPTIVE_DOWNLOADING_STEP = 5;
	int ADAPTIVE_DOWNLOADING_MIN_TIME_MS = 3500;
	int ADAPTIVE_DOWNLOADING_MAX_TIME_MS = 10000;

	int SECONDS_PER_MINUTE = 60;

  /* Search nearest cache count configuration */
	int DOWNLOADING_COUNT_OF_CACHES_DEFAULT = 20;
	int DOWNLOADING_COUNT_OF_CACHES_MAX = 500;
	int DOWNLOADING_COUNT_OF_CACHES_MAX_LOW_MEMORY = 200;
	int DOWNLOADING_COUNT_OF_CACHES_STEP_DEFAULT = 10;

	long LOW_MEMORY_THRESHOLD = 16777216;

	String UPDATE_WITH_LOGS_COMPONENT = "com.arcao.geocaching4locus.UpdateWithLogsActivity";
	int LOGS_PER_REQUEST = 30;
	int LOGS_TO_UPDATE_MAX = 100;

	float MILES_PER_KILOMETER = 1.609344F;
}
