package com.arcao.geocaching4locus.data.provider;

import android.content.Context;
import android.preference.PreferenceFragment;
import android.support.annotation.StringRes;

public interface Provider {
	String getId();
	@StringRes
	int getName();
	Class<? extends PreferenceFragment> getFilterPreference();
	boolean canHandleCacheCode(String cacheCode);
	ProviderService createService(Context context);
}
