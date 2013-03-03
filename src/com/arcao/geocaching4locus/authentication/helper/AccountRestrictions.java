package com.arcao.geocaching4locus.authentication.helper;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.arcao.geocaching.api.data.CacheLimits;
import com.arcao.geocaching.api.data.apilimits.ApiLimits;
import com.arcao.geocaching.api.data.apilimits.CacheLimit;
import com.arcao.geocaching.api.data.type.MemberType;
import com.arcao.geocaching4locus.constants.PrefConstants;

public class AccountRestrictions {
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
	
	protected void init() {
		maxFullGeocacheLimit = mPrefs.getLong(PrefConstants.RESTRICTION__MAX_FULL_GEOCACHE_LIMIT, Long.MAX_VALUE);
		currentFullGeocacheLimit = mPrefs.getInt(PrefConstants.RESTRICTION__CURRENT_FULL_GEOCACHE_LIMIT, 0);
		fullGeocacheLimitPeriod = mPrefs.getLong(PrefConstants.RESTRICTION__FULL_GEOCACHE_LIMIT_PERIOD, 365*24*3600);
		renewFullGeocacheLimit = new Date(mPrefs.getLong(PrefConstants.RESTRICTION__RENEW_FULL_GEOCACHE_LIMIT, 0));
		premiumMember = mPrefs.getBoolean(PrefConstants.RESTRICTION__PREMIUM_MEMBER, false);
	}
	
	public void updateMemberType(MemberType memberType) {
		switch (memberType) {
			case Charter:
			case Premium:
				premiumMember = true;
			default:
				premiumMember = false;
				break;
		}
		
		Editor editor = mPrefs.edit();
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
		
		Editor editor = mPrefs.edit();
		editor.putLong(PrefConstants.RESTRICTION__MAX_FULL_GEOCACHE_LIMIT, maxFullGeocacheLimit);
		editor.putLong(PrefConstants.RESTRICTION__FULL_GEOCACHE_LIMIT_PERIOD, fullGeocacheLimitPeriod);
		editor.commit();
	}
	
	public void updateLimits(CacheLimits cacheLimits) {
		if (cacheLimits == null)
			return;
		
		maxFullGeocacheLimit = cacheLimits.getMaxCacheCount();
		
		// cache limit was renew
		if (currentFullGeocacheLimit > cacheLimits.getCurrentCacheCount()
				|| (currentFullGeocacheLimit == 0 && cacheLimits.getCurrentCacheCount() > 0)) {
			currentFullGeocacheLimit = cacheLimits.getCurrentCacheCount();
			
			Calendar c = Calendar.getInstance();
			c.add(Calendar.SECOND, (int) fullGeocacheLimitPeriod);
			
			renewFullGeocacheLimit = c.getTime();
			
			// store it to preferences
			Editor editor = mPrefs.edit();
			editor.putLong(PrefConstants.RESTRICTION__MAX_FULL_GEOCACHE_LIMIT, maxFullGeocacheLimit);
			editor.putLong(PrefConstants.RESTRICTION__CURRENT_FULL_GEOCACHE_LIMIT, currentFullGeocacheLimit);
			editor.putLong(PrefConstants.RESTRICTION__RENEW_FULL_GEOCACHE_LIMIT, renewFullGeocacheLimit.getTime());
			editor.commit();
		} else {
			currentFullGeocacheLimit = cacheLimits.getCurrentCacheCount();
			
			// store it to preferences
			Editor editor = mPrefs.edit();
			editor.putLong(PrefConstants.RESTRICTION__MAX_FULL_GEOCACHE_LIMIT, maxFullGeocacheLimit);
			editor.putLong(PrefConstants.RESTRICTION__CURRENT_FULL_GEOCACHE_LIMIT, currentFullGeocacheLimit);
			editor.commit();
		}
	}
	
	public boolean isPremiumMember() {
		return premiumMember;
	}
	
	public long getMaxFullGeocacheLimit() {
		return maxFullGeocacheLimit;
	}
	
	public long getCurrentFullGeocacheLimit() {
		return currentFullGeocacheLimit;
	}
	
	public Date getRenewFullGeocacheLimit() {
		
		if (renewFullGeocacheLimit.before(new Date())) {
			Calendar c = Calendar.getInstance();
			c.add(Calendar.SECOND, (int) fullGeocacheLimitPeriod);
			renewFullGeocacheLimit = c.getTime();

			// store it to preferences
			Editor editor = mPrefs.edit();
			editor.putLong(PrefConstants.RESTRICTION__RENEW_FULL_GEOCACHE_LIMIT, renewFullGeocacheLimit.getTime());
			editor.commit();
		}
		
		return renewFullGeocacheLimit;
	}

}
