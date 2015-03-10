package com.arcao.geocaching4locus.constants;

public interface PrefConstants {
	String ACCOUNT_STORAGE_NAME = "ACCOUNT";
	String RESTRICTION_STORAGE_NAME = "RESTRICTION";

	String PREF_VERSION = "pref_version";
	int CURRENT_PREF_VERSION = 1;

	String LAST_LATITUDE = "latitude";
	String LAST_LONGITUDE = "longitude";

	String USERNAME = "username";
	String PASSWORD = "password";
	String SESSION = "session";

	String OAUTH_TOKEN = "OAUTH_TOKEN";
	String OAUTH_TOKEN_SECRET = "OAUTH_TOKEN_SECRET";

	String FILTER_CACHE_TYPE = "cache_type_filter";
	String FILTER_CACHE_TYPE_PREFIX = "filter_";
	String FILTER_CONTAINER_TYPE = "container_type_filter";
	String FILTER_CONTAINER_TYPE_PREFIX = "container_filter_";
	String FILTER_DIFFICULTY = "difficulty_filter";
	String FILTER_DIFFICULTY_MIN = "difficulty_filter_min";
	String FILTER_DIFFICULTY_MAX = "difficulty_filter_max";
	String FILTER_TERRAIN = "terrain_filter";
	String FILTER_TERRAIN_MIN = "terrain_filter_min";
	String FILTER_TERRAIN_MAX = "terrain_filter_max";
	String FILTER_DISTANCE = "filter_distance";
	String FILTER_SHOW_FOUND = "filter_show_found";
	String FILTER_SHOW_OWN = "filter_show_own";
	String FILTER_SHOW_DISABLED = "filter_show_disabled";

	String LIVE_MAP = "live_map";
	String SHOW_LIVE_MAP_DISABLED_NOTIFICATION = "show_live_map_disabled_notification";
	String SHOW_LIVE_MAP_VISIBLE_ONLY_NOTIFICATION = "show_live_map_visible_only_notification";
	String LIVE_MAP_DOWNLOAD_HINTS = "live_map_download_hints";

	String DOWNLOADING_SIMPLE_CACHE_DATA = "simple_cache_data";
	String DOWNLOADING_FULL_CACHE_DATE_ON_SHOW = "full_cache_data_on_show";
	String DOWNLOADING_COUNT_OF_CACHES = "filter_count_of_caches";
	String DOWNLOADING_COUNT_OF_LOGS = "downloading_count_of_logs";
	String DOWNLOADING_COUNT_OF_CACHES_STEP = "downloading_count_of_caches_step";
	String DOWNLOAD_LOGS_UPDATE_CACHE = "download_logs_update_cache";

	String IMPERIAL_UNITS = "imperial_units";

	String ABOUT_VERSION = "about_version";
	String ABOUT_WEBSITE = "about_website";
	String ABOUT_FEEDBACK = "about_feedback";
	String ABOUT_DONATE_PAYPAL = "about_donate_paypal";
	String ACCOUNT_GEOCACHING_LIVE = "account_geocaching_live";

	String RESTRICTION__PREMIUM_MEMBER = "premium_account";
	String RESTRICTION__RENEW_FULL_GEOCACHE_LIMIT = "renew_full_geocache_limit";
	String RESTRICTION__FULL_GEOCACHE_LIMIT_PERIOD = "full_geocache_limit_period";
	String RESTRICTION__CURRENT_FULL_GEOCACHE_LIMIT = "current_full_geocache_limit";
	String RESTRICTION__MAX_FULL_GEOCACHE_LIMIT = "max_full_geocache_limit";


	String DOWNLOADING_FULL_CACHE_DATE_ON_SHOW__UPDATE_ONCE = "0";
	String DOWNLOADING_FULL_CACHE_DATE_ON_SHOW__UPDATE_EVERY = "1";
	String DOWNLOADING_FULL_CACHE_DATE_ON_SHOW__UPDATE_NEVER = "2";

	String UNIT_KM = "km";
	String UNIT_MILES = "mi";

	String[] shortCacheTypeName = {
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

	String[] shortContainerTypeName = {
					"?",
					"M",
					"S",
					"R",
					"L",
					"H",
					"O"
	};
}