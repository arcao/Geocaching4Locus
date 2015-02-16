package com.arcao.geocaching4locus.data.provider;

import android.content.Context;
import android.preference.PreferenceFragment;
import android.support.annotation.StringRes;

public interface Provider {
	public String getId();
	@StringRes
	public int getName();
	public Class<? extends PreferenceFragment> getFilterPreference();
	public boolean canHandleCacheCode(String cacheCode);
	public ProviderService createService(Context context);
}
