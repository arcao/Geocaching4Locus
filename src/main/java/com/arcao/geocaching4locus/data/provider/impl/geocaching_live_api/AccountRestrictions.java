package com.arcao.geocaching4locus.data.provider.impl.geocaching_live_api;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.arcao.geocaching.api.data.CacheLimits;
import com.arcao.geocaching.api.data.apilimits.ApiLimits;
import com.arcao.geocaching.api.data.apilimits.CacheLimit;
import com.arcao.geocaching.api.data.type.MemberType;
import com.arcao.geocaching4locus.constants.PrefConstants;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AccountRestrictions {
	private static final int DEFAULT_FULL_GEOCACHE_LIMIT_PERIOD = 365 * 24 * 3600;

	protected final SharedPreferences mPreferences;

	protected long maxFullGeocacheLimit;
	protected long currentFullGeocacheLimit;
	protected long fullGeocacheLimitPeriod;
	protected Date renewFullGeocacheLimit;
	protected boolean premiumMember;

	public AccountRestrictions(SharedPreferences preferences) {
		mPreferences = preferences;
		init();
	}

	protected void remove() {
		Editor editor = mPreferences.edit();
		editor.remove(PrefConstants.RESTRICTION__MAX_FULL_GEOCACHE_LIMIT);
		editor.remove(PrefConstants.RESTRICTION__CURRENT_FULL_GEOCACHE_LIMIT);
		editor.remove(PrefConstants.RESTRICTION__FULL_GEOCACHE_LIMIT_PERIOD);
		editor.remove(PrefConstants.RESTRICTION__RENEW_FULL_GEOCACHE_LIMIT);
		editor.remove(PrefConstants.RESTRICTION__PREMIUM_MEMBER);
		editor.commit();

		init();
	}

	protected void init() {
		maxFullGeocacheLimit = mPreferences.getLong(PrefConstants.RESTRICTION__MAX_FULL_GEOCACHE_LIMIT, Long.MAX_VALUE);
		currentFullGeocacheLimit = mPreferences.getLong(PrefConstants.RESTRICTION__CURRENT_FULL_GEOCACHE_LIMIT, 0);
		fullGeocacheLimitPeriod = mPreferences.getLong(PrefConstants.RESTRICTION__FULL_GEOCACHE_LIMIT_PERIOD, DEFAULT_FULL_GEOCACHE_LIMIT_PERIOD);
		renewFullGeocacheLimit = new Date(mPreferences.getLong(PrefConstants.RESTRICTION__RENEW_FULL_GEOCACHE_LIMIT, 0));
		premiumMember = mPreferences.getBoolean(PrefConstants.RESTRICTION__PREMIUM_MEMBER, false);
	}

	public void updateMemberType(MemberType memberType) {
		switch (memberType) {
			case Charter:
			case Premium:
				premiumMember = true;
				break;
			default:
				premiumMember = false;
				break;
		}

		Editor editor = mPreferences.edit();
		editor.putBoolean(PrefConstants.RESTRICTION__PREMIUM_MEMBER, premiumMember);
		editor.commit();
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

		Editor editor = mPreferences.edit();
		editor.putLong(PrefConstants.RESTRICTION__MAX_FULL_GEOCACHE_LIMIT, maxFullGeocacheLimit);
		editor.putLong(PrefConstants.RESTRICTION__FULL_GEOCACHE_LIMIT_PERIOD, fullGeocacheLimitPeriod);
		editor.commit();
	}

	public void updateLimits(CacheLimits cacheLimits) {
		if (cacheLimits == null)
			return;

		maxFullGeocacheLimit = cacheLimits.getMaxCacheCount();

		Editor editor = mPreferences.edit();

		// cache limit was renew
		if (currentFullGeocacheLimit > cacheLimits.getCurrentCacheCount()
				|| (currentFullGeocacheLimit == 0 && cacheLimits.getCurrentCacheCount() > 0)) {
			currentFullGeocacheLimit = cacheLimits.getCurrentCacheCount();

			Calendar c = Calendar.getInstance();
			c.add(Calendar.MINUTE, (int) fullGeocacheLimitPeriod);

			renewFullGeocacheLimit = c.getTime();

			// store it to preferences
			editor.putLong(PrefConstants.RESTRICTION__MAX_FULL_GEOCACHE_LIMIT, maxFullGeocacheLimit);
			editor.putLong(PrefConstants.RESTRICTION__CURRENT_FULL_GEOCACHE_LIMIT, currentFullGeocacheLimit);
			editor.putLong(PrefConstants.RESTRICTION__RENEW_FULL_GEOCACHE_LIMIT, renewFullGeocacheLimit.getTime());
		} else {
			currentFullGeocacheLimit = cacheLimits.getCurrentCacheCount();

			// store it to preferences
			editor.putLong(PrefConstants.RESTRICTION__MAX_FULL_GEOCACHE_LIMIT, maxFullGeocacheLimit);
			editor.putLong(PrefConstants.RESTRICTION__CURRENT_FULL_GEOCACHE_LIMIT, currentFullGeocacheLimit);
		}

		// this is some type of HACK, there is no other way how to detect change of Member type
		editor.putBoolean(PrefConstants.RESTRICTION__PREMIUM_MEMBER, maxFullGeocacheLimit > 1000);
		editor.commit();
	}

	public boolean isPremiumMember() {
		return premiumMember;
	}

	public long getMaxFullGeocacheLimit() {
		return maxFullGeocacheLimit;
	}

	public long getCurrentFullGeocacheLimit() {
		checkRenewPeriod();

		return currentFullGeocacheLimit;
	}

	public Date getRenewFullGeocacheLimit() {
		checkRenewPeriod();

		return renewFullGeocacheLimit;
	}

	public long getFullGeocacheLimitPeriod() {
		return fullGeocacheLimitPeriod;
	}

	public long getFullGeocacheLimitLeft() {
		checkRenewPeriod();

		return Math.max(0, maxFullGeocacheLimit - currentFullGeocacheLimit);
	}

	public boolean isFullGeocachesLimitWarningRequired() {
		return !premiumMember && getFullGeocacheLimitLeft() > 0;
	}

	protected void checkRenewPeriod() {
		if (renewFullGeocacheLimit.before(new Date())) {
			Calendar c = Calendar.getInstance();
			c.add(Calendar.MINUTE, (int) fullGeocacheLimitPeriod);
			renewFullGeocacheLimit = c.getTime();
			currentFullGeocacheLimit = 0;
		}
	}
}
