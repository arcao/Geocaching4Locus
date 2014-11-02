package com.arcao.geocaching4locus;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import com.arcao.geocaching4locus.fragment.preference.AboutPreferenceFragment;
import com.arcao.geocaching4locus.fragment.preference.AccountsPreferenceFragment;
import com.arcao.geocaching4locus.fragment.preference.AdvancedPreferenceFragment;
import com.arcao.geocaching4locus.fragment.preference.DownloadingPreferenceFragment;
import com.arcao.geocaching4locus.fragment.preference.FilterPreferenceFragment;
import com.arcao.geocaching4locus.fragment.preference.HeaderPreferenceFragment;
import com.arcao.geocaching4locus.fragment.preference.LiveMapPreferenceFragment;

import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends ActionBarActivity {
	public static final String PARAM_FRAGMENT = "fragment";

	private static final FragmentCache FRAGMENT_CACHE = new FragmentCache();
	static {
		FRAGMENT_CACHE.add(new HeaderPreferenceFragment());
		FRAGMENT_CACHE.add(new AccountsPreferenceFragment());
		FRAGMENT_CACHE.add(new FilterPreferenceFragment());
		FRAGMENT_CACHE.add(new LiveMapPreferenceFragment());
		FRAGMENT_CACHE.add(new DownloadingPreferenceFragment());
		FRAGMENT_CACHE.add(new AdvancedPreferenceFragment());
		FRAGMENT_CACHE.add(new AboutPreferenceFragment());
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		String fragmentName = getIntent().getStringExtra(PARAM_FRAGMENT);
		if (fragmentName != null) {
			// Display the fragment as the main content.
			getSupportFragmentManager().beginTransaction().replace(android.R.id.content, FRAGMENT_CACHE.get(fragmentName)).commit();
			return;
		}

		getSupportFragmentManager().beginTransaction().replace(android.R.id.content, new HeaderPreferenceFragment()).commit();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			// Respond to the action bar's Up/Home button
			case android.R.id.home:
				finish();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public static Intent createIntent(Context context) {
		return new Intent(context, SettingsActivity.class);
	}

	public static Intent createIntent(Context context, String preferenceFragment) {
		return createIntent(context).putExtra(SettingsActivity.PARAM_FRAGMENT, preferenceFragment);
	}

	public static Intent createIntent(Context context, Class<?> preferenceFragment) {
		return createIntent(context, preferenceFragment.getName());
	}

	private static class FragmentCache {
		private Map<String, Fragment> cache = new HashMap<>();

		public void add(Fragment fragment) {
			cache.put(((Object)fragment).getClass().getName(), fragment);
		}

		public Fragment get(String key) {
			return cache.get(key);
		}
	}
}
