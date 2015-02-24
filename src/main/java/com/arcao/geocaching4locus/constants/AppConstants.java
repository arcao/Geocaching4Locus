package com.arcao.geocaching4locus.constants;

import android.net.Uri;

import org.osgi.framework.Version;

public interface AppConstants {
	static final boolean USE_PRODUCTION_CONFIGURATION = true;
	static final String STAGGING_CONFIGURATION_CLASS = "com.arcao.geocaching.api.configuration.impl.StaggingConfiguration";
	static final String PRODUCTION_CONFIGURATION_CLASS = "com.arcao.geocaching.api.configuration.impl.ProductionConfiguration";

	static final String GEOCACHING_WEBSITE_URL = "http://www.geocaching.com/";
	static final String OAUTH_CALLBACK_URL = "x-locus://oauth.callback/callback/geocaching";

	static final String ERROR_SCRIPT_URL = "http://geocaching4locus.eu/sendcrash/send.php";

	static final Uri MANUAL_URI = Uri.parse("http://geocaching4locus.eu/manual/");
	static final Uri WEBSITE_URI = Uri.parse("http://geocaching4locus.eu/");
	static final Uri GEOCACHING_LIVE_URI = Uri.parse("http://www.geocaching.com/live");
	static final String DONATE_PAYPAL_URI = "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=arcao%%40arcao%%2ecom&lc=CZ&item_name=Geocaching4Locus&item_number=g4l&currency_code=%s&bn=PP%%2dDonationsBF%%3abtn_donateCC_LG%%2egif%%3aNonHosted";

	/** OAuth response parameter that contains error message - specific Groundspeak extension */
	static final String OAUTH_ERROR_MESSAGE_PARAMETER = "oauth_error_message";

	static final Version LOCUS_MIN_VERSION = Version.parseVersion("3.0.0");

	/** Login dialog visible state */
	static final String STATE_AUTHENTICATOR_ACTIVITY_VISIBLE = "AUTHENTICATOR_ACTIVITY_VISIBLE";

	/* Adaptive downloading configuration */
	static final int CACHES_PER_REQUEST = 10;
	static final int ADAPTIVE_DOWNLOADING_MIN_CACHES = CACHES_PER_REQUEST;
	static final int ADAPTIVE_DOWNLOADING_MAX_CACHES = 50;
	static final int ADAPTIVE_DOWNLOADING_STEP = 5;
	static final int ADAPTIVE_DOWNLOADING_MIN_TIME_MS = 3500;
	static final int ADAPTIVE_DOWNLOADING_MAX_TIME_MS = 10000;

	static final int SECONDS_PER_MINUTE = 60;

  /* Search nearest cache count configuration */
	static final int DOWNLOADING_COUNT_OF_CACHES_DEFAULT = 20;
	static final int DOWNLOADING_COUNT_OF_CACHES_MAX = 500;
	static final int DOWNLOADING_COUNT_OF_CACHES_MAX_LOW_MEMORY = 200;
	static final int DOWNLOADING_COUNT_OF_CACHES_STEP_DEFAULT = 10;

	static final long LOW_MEMORY_THRESHOLD = 16777216;

	static final String UPDATE_WITH_LOGS_COMPONENT = "com.arcao.geocaching4locus.UpdateWithLogsActivity";
	static final int LOGS_PER_REQUEST = 50;
	static final int LOGS_TO_UPDATE_MAX = 100;
}
