package com.arcao.geocaching4locus.constants;

public interface PrefConstants {
	static final String IMPORT_CACHES  = "import_caches";
	static final String LAST_LATITUDE  = "latitude";
	static final String LAST_LONGITUDE = "longitude";

	@Deprecated
	static final String USERNAME = "username";
	@Deprecated
	static final String PASSWORD = "password";
	@Deprecated
	static final String SESSION  = "session";

	static final String FILTER_CACHE_TYPE_PREFIX = "filter_";
	static final String FILTER_DIFFICULTY_MIN = "difficulty_filter_min";
	static final String FILTER_DIFFICULTY_MAX = "difficulty_filter_max";
	static final String FILTER_TERRAIN_MIN = "terrain_filter_min";
	static final String FILTER_TERRAIN_MAX = "terrain_filter_max";
	static final String FILTER_DISTANCE = "filter_distance";
	static final String FILTER_SHOW_FOUND = "filter_show_found";
	static final String FILTER_SHOW_OWN = "filter_show_own";
	static final String FILTER_SHOW_DISABLED = "filter_show_disabled";
	
	static final String LIVE_MAP = "live_map";
	static final String DOWNLOADING_SIMPLE_CACHE_DATA = "simple_cache_data";
	static final String DOWNLOADING_FULL_CACHE_DATE_ON_SHOW = "full_cache_data_on_show";
	static final String DOWNLOADING_COUNT_OF_CACHES = "filter_count_of_caches";
	static final String DOWNLOADING_COUNT_OF_LOGS = "downloading_count_of_logs";
	static final String DOWNLOADING_COUNT_OF_TRACKABLES = "downloading_count_of_trackables";

	static final String IMPERIAL_UNITS = "imperial_units";

	static final String ABOUT_VERSION = "about_version";
	static final String ABOUT_WEBSITE = "about_website";
	static final String ABOUT_DONATE_PAYPAL = "about_donate_paypal";



	static final String DOWNLOADING_FULL_CACHE_DATE_ON_SHOW__UPDATE_ONCE  = "0";
	static final String DOWNLOADING_FULL_CACHE_DATE_ON_SHOW__UPDATE_EVERY = "1";
	static final String DOWNLOADING_FULL_CACHE_DATE_ON_SHOW__UPDATE_NEVER = "2";
}
