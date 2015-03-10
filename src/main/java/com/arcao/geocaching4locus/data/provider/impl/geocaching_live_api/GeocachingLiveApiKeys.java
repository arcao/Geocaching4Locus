package com.arcao.geocaching4locus.data.provider.impl.geocaching_live_api;

public interface GeocachingLiveApiKeys {
	/* Preference keys */
	String PREF_USERNAME = "username";
	String PREF_TOKEN = "token";

	String PREF_DOWNLOADING_COUNT_OF_LOGS = "downloading_count_of_logs";

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
	int DOWNLOADING_COUNT_OF_CACHES_MIN = 10;
	int DOWNLOADING_COUNT_OF_CACHES_MAX = 500;
	int DOWNLOADING_COUNT_OF_CACHES_MAX_LOW_MEMORY = 200;
	int DOWNLOADING_COUNT_OF_CACHES_STEP_DEFAULT = 10;
}
