package com.arcao.geocaching4locus.authentication.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import com.arcao.geocaching.api.data.GeocacheLimits;
import com.arcao.geocaching.api.data.apilimits.ApiLimits;
import com.arcao.geocaching.api.data.apilimits.CacheLimit;
import com.arcao.geocaching.api.data.type.ContainerType;
import com.arcao.geocaching.api.data.type.GeocacheType;
import com.arcao.geocaching.api.data.type.MemberType;
import com.arcao.geocaching4locus.constants.PrefConstants;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AccountRestrictions {
	private static final long DEFAULT_FULL_GEOCACHE_LIMIT_PERIOD = 1440; // 24 hours in minutes

	private final SharedPreferences mPrefs;
	private final Context mContext;

	private long maxFullGeocacheLimit;
	private long currentFullGeocacheLimit;
	private long fullGeocacheLimitPeriod;
	private Date renewFullGeocacheLimit;
	private boolean premiumMember;

	public AccountRestrictions(Context context) {
		mContext = context.getApplicationContext();
		mPrefs = mContext.getSharedPreferences(PrefConstants.RESTRICTION_STORAGE_NAME, Context.MODE_PRIVATE);
		init();
	}

	protected void remove() {
		mPrefs.edit()
			.remove(PrefConstants.RESTRICTION__MAX_FULL_GEOCACHE_LIMIT)
			.remove(PrefConstants.RESTRICTION__CURRENT_FULL_GEOCACHE_LIMIT)
			.remove(PrefConstants.RESTRICTION__FULL_GEOCACHE_LIMIT_PERIOD)
			.remove(PrefConstants.RESTRICTION__RENEW_FULL_GEOCACHE_LIMIT)
			.remove(PrefConstants.RESTRICTION__PREMIUM_MEMBER)
			.apply();

		init();
	}

	private void init() {
		maxFullGeocacheLimit = mPrefs.getLong(PrefConstants.RESTRICTION__MAX_FULL_GEOCACHE_LIMIT, Long.MAX_VALUE);
		currentFullGeocacheLimit = mPrefs.getLong(PrefConstants.RESTRICTION__CURRENT_FULL_GEOCACHE_LIMIT, 0);
		fullGeocacheLimitPeriod = mPrefs.getLong(PrefConstants.RESTRICTION__FULL_GEOCACHE_LIMIT_PERIOD, DEFAULT_FULL_GEOCACHE_LIMIT_PERIOD);
		renewFullGeocacheLimit = new Date(mPrefs.getLong(PrefConstants.RESTRICTION__RENEW_FULL_GEOCACHE_LIMIT, 0));
		premiumMember = mPrefs.getBoolean(PrefConstants.RESTRICTION__PREMIUM_MEMBER, false);
	}

	public void updateMemberType(MemberType memberType) {
		switch (memberType) {
			case Charter:
			case Premium:
				premiumMember = true;
				break;
			default:
				presetBasicMembershipConfiguration();
				premiumMember = false;
				break;
		}

		mPrefs.edit()
			.putBoolean(PrefConstants.RESTRICTION__PREMIUM_MEMBER, premiumMember)
			.apply();
	}

	private void presetBasicMembershipConfiguration() {
		SharedPreferences defaultPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

		Editor editor = defaultPreferences.edit()
				// DOWNLOADING
				.putBoolean(PrefConstants.DOWNLOADING_SIMPLE_CACHE_DATA, true)
				.putString(PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW, PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW__UPDATE_NEVER)
				.putInt(PrefConstants.DOWNLOADING_COUNT_OF_LOGS, 0)
				// LIVE MAP
				.putBoolean(PrefConstants.LIVE_MAP_DOWNLOAD_HINTS, false)
				// FILTERS
				.putString(PrefConstants.FILTER_DIFFICULTY_MIN, "1")
				.putString(PrefConstants.FILTER_DIFFICULTY_MAX, "5")
				.putString(PrefConstants.FILTER_TERRAIN_MIN, "1")
				.putString(PrefConstants.FILTER_TERRAIN_MAX, "5");

		// multi-select filters (select all)
		for (int i = 0; i < GeocacheType.values().length; i++)
			editor.putBoolean(PrefConstants.FILTER_CACHE_TYPE_PREFIX + i, true);
		for (int i = 0; i < ContainerType.values().length; i++)
			editor.putBoolean(PrefConstants.FILTER_CONTAINER_TYPE_PREFIX + i, true);

		editor.apply();
	}

	public void updateLimits(ApiLimits apiLimits) {
		if (apiLimits == null)
			return;

		List<CacheLimit> limits = apiLimits.getCacheLimits();
		if (limits.isEmpty())
			return;

		CacheLimit limit = limits.get(0);

		maxFullGeocacheLimit = limit.getLimit();
		fullGeocacheLimitPeriod = limit.getPeriod();

		mPrefs.edit()
			.putLong(PrefConstants.RESTRICTION__MAX_FULL_GEOCACHE_LIMIT, maxFullGeocacheLimit)
			.putLong(PrefConstants.RESTRICTION__FULL_GEOCACHE_LIMIT_PERIOD, fullGeocacheLimitPeriod)
			.apply();
	}

	public void updateLimits(GeocacheLimits cacheLimits) {
		if (cacheLimits == null)
			return;

		maxFullGeocacheLimit = cacheLimits.getMaxGeocacheCount();

		Editor editor = mPrefs.edit();

		// cache limit was renew
		if (currentFullGeocacheLimit > cacheLimits.getCurrentGeocacheCount()
				|| (currentFullGeocacheLimit == 0 && cacheLimits.getCurrentGeocacheCount() > 0)) {
			currentFullGeocacheLimit = cacheLimits.getCurrentGeocacheCount();

			Calendar c = Calendar.getInstance();
			c.add(Calendar.MINUTE, (int) fullGeocacheLimitPeriod);

			renewFullGeocacheLimit = c.getTime();

			// store it to preferences
			editor.putLong(PrefConstants.RESTRICTION__MAX_FULL_GEOCACHE_LIMIT, maxFullGeocacheLimit);
			editor.putLong(PrefConstants.RESTRICTION__CURRENT_FULL_GEOCACHE_LIMIT, currentFullGeocacheLimit);
			editor.putLong(PrefConstants.RESTRICTION__RENEW_FULL_GEOCACHE_LIMIT, renewFullGeocacheLimit.getTime());
		} else {
			currentFullGeocacheLimit = cacheLimits.getCurrentGeocacheCount();

			// store it to preferences
			editor.putLong(PrefConstants.RESTRICTION__MAX_FULL_GEOCACHE_LIMIT, maxFullGeocacheLimit);
			editor.putLong(PrefConstants.RESTRICTION__CURRENT_FULL_GEOCACHE_LIMIT, currentFullGeocacheLimit);
		}

		// this is some type of HACK, there is no other way how to detect change of Member type
		//editor.putBoolean(PrefConstants.RESTRICTION__PREMIUM_MEMBER, maxFullGeocacheLimit == 6000);
		editor.apply();
	}

	public boolean isPremiumMember() {
		return premiumMember;
	}

	public long getMaxFullGeocacheLimit() {
		return maxFullGeocacheLimit;
	}

	public Date getRenewFullGeocacheLimit() {
		checkRenewPeriod();

		return renewFullGeocacheLimit;
	}

	public long getFullGeocacheLimitPeriod() {
		return fullGeocacheLimitPeriod;
	}

	private void checkRenewPeriod() {
		if (renewFullGeocacheLimit.before(new Date())) {
			Calendar c = Calendar.getInstance();
			c.add(Calendar.MINUTE, (int) fullGeocacheLimitPeriod);
			renewFullGeocacheLimit = c.getTime();
			currentFullGeocacheLimit = 0;
		}
	}
}
