package com.arcao.geocaching4locus.authentication.helper;

import android.content.Context;
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
import java.util.concurrent.TimeUnit;

public class AccountRestrictions {
	private static final long DEFAULT_FULL_GEOCACHE_LIMIT_PERIOD = TimeUnit.DAYS.toSeconds(365);

	protected final SharedPreferences mPrefs;

	protected long maxFullGeocacheLimit;
	protected long currentFullGeocacheLimit;
	protected long fullGeocacheLimitPeriod;
	protected Date renewFullGeocacheLimit;
	protected boolean premiumMember;

	public AccountRestrictions(Context mContext) {
		mPrefs = mContext.getSharedPreferences("RESTRICTION", 0);
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

	protected void init() {
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
				premiumMember = false;
				break;
		}

		mPrefs.edit()
			.putBoolean(PrefConstants.RESTRICTION__PREMIUM_MEMBER, premiumMember)
			.apply();
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

	public void updateLimits(CacheLimits cacheLimits) {
		if (cacheLimits == null)
			return;

		maxFullGeocacheLimit = cacheLimits.getMaxCacheCount();

		Editor editor = mPrefs.edit();

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
