package com.arcao.geocaching4locus.data.provider;

import android.content.Context;
import android.support.v4.preference.PreferenceFragment;

public interface Provider {
	public String getId();
	public int getName();
	public Class<PreferenceFragment> getFilterPreference();
	public boolean canHandleGcCode(String gcCode);
	public ProviderService createService(Context context);
}
