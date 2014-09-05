package com.arcao.geocaching4locus.constants;

import oauth.signpost.OAuth;

public interface PrefConstants {
	static final String PREF_VERSION = "pref_version";
	static final int CURRENT_PREF_VERSION = 1;

	static final String LAST_LATITUDE = "latitude";
	static final String LAST_LONGITUDE = "longitude";

	static final String USERNAME = "username";
	static final String PASSWORD = "password";
	static final String SESSION = "session";

	static final String OAUTH_TOKEN = OAuth.OAUTH_TOKEN;
	static final String OAUTH_TOKEN_SECRET = OAuth.OAUTH_TOKEN_SECRET;

	static final String FILTER_CACHE_TYPE_PREFIX = "filter_";
	static final String FILTER_CONTAINER_TYPE_PREFIX = "container_filter_";
	static final String FILTER_DIFFICULTY = "difficulty_filter";
	static final String FILTER_DIFFICULTY_MIN = "difficulty_filter_min";
	static final String FILTER_DIFFICULTY_MAX = "difficulty_filter_max";
	static final String FILTER_TERRAIN = "terrain_filter";
	static final String FILTER_TERRAIN_MIN = "terrain_filter_min";
	static final String FILTER_TERRAIN_MAX = "terrain_filter_max";
	static final String FILTER_DISTANCE = "filter_distance";
	static final String FILTER_SHOW_FOUND = "filter_show_found";
	static final String FILTER_SHOW_OWN = "filter_show_own";
	static final String FILTER_SHOW_DISABLED = "filter_show_disabled";

	static final String LIVE_MAP = "live_map";
	static final String SHOW_LIVE_MAP_DISABLED_NOTIFICATION = "show_live_map_disabled_notification";
	static final String SHOW_LIVE_MAP_VISIBLE_ONLY_NOTIFICATION = "show_live_map_visible_only_notification";
	static final String DOWNLOADING_SIMPLE_CACHE_DATA = "simple_cache_data";
	static final String DOWNLOADING_FULL_CACHE_DATE_ON_SHOW = "full_cache_data_on_show";
	static final String DOWNLOADING_COUNT_OF_CACHES = "filter_count_of_caches";
	static final String DOWNLOADING_COUNT_OF_LOGS = "downloading_count_of_logs";
	static final String DOWNLOAD_LOGS_UPDATE_CACHE = "download_logs_update_cache";

	static final String IMPERIAL_UNITS = "imperial_units";

	static final String ABOUT_VERSION = "about_version";
	static final String ABOUT_WEBSITE = "about_website";
	static final String ABOUT_DONATE_PAYPAL = "about_donate_paypal";
	static final String ACCOUNT_GEOCACHING_LIVE = "account_geocaching_live";

	static final String RESTRICTION__PREMIUM_MEMBER = "premium_account";
	static final String RESTRICTION__RENEW_FULL_GEOCACHE_LIMIT = "renew_full_geocache_limit";
	static final String RESTRICTION__FULL_GEOCACHE_LIMIT_PERIOD = "full_geocache_limit_period";
	static final String RESTRICTION__CURRENT_FULL_GEOCACHE_LIMIT = "current_full_geocache_limit";
	static final String RESTRICTION__MAX_FULL_GEOCACHE_LIMIT = "max_full_geocache_limit";


	static final String DOWNLOADING_FULL_CACHE_DATE_ON_SHOW__UPDATE_ONCE = "0";
	static final String DOWNLOADING_FULL_CACHE_DATE_ON_SHOW__UPDATE_EVERY = "1";
	static final String DOWNLOADING_FULL_CACHE_DATE_ON_SHOW__UPDATE_NEVER = "2";

	static final String[] shortCacheTypeName = {
					"Tradi",
					"Multi",
					"Mystery",
					"Virtual",
					"Earth",
					"APE",
					"Letter",
					"Wherigo",
					"Event",
					"M-Event",
					"CITO",
					"Advent",
					"Webcam",
					"Loc-less",
					"L&F",
					"GS HQ",
					"GS L&F",
					"GS Party",
					"G-Event"
	};

	static final String[] shortContainerTypeName = {
					"?",
					"M",
					"S",
					"R",
					"L",
					"H",
					"O"
	};
}