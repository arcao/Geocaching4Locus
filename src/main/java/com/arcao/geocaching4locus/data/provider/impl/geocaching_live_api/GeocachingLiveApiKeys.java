package com.arcao.geocaching4locus.data.provider.impl.geocaching_live_api;

public interface GeocachingLiveApiKeys {
	/* Preference keys */
	static final String PREF_USERNAME = "username";
	static final String PREF_TOKEN = "token";

	static final String PREF_DOWNLOADING_COUNT_OF_LOGS = "downloading_count_of_logs";

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
	static final int DOWNLOADING_COUNT_OF_CACHES_MIN = 10;
	static final int DOWNLOADING_COUNT_OF_CACHES_MAX = 500;
	static final int DOWNLOADING_COUNT_OF_CACHES_MAX_LOW_MEMORY = 200;
	static final int DOWNLOADING_COUNT_OF_CACHES_STEP_DEFAULT = 10;
}
