package com.arcao.geocaching4locus.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.support.annotation.XmlRes;
import android.view.MenuItem;

import com.arcao.geocaching4locus.base.AppCompatPreferenceActivity;
import com.arcao.geocaching4locus.R;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatPreferenceActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// hack / fix for Samsung phones - missing padding in header layout
		if (!onIsMultiPane() && !getIntent().hasExtra(EXTRA_SHOW_FRAGMENT)) {
			//noinspection deprecation
			setPreferenceScreen(getPreferenceScreenFromHeader(R.xml.preference_header));
		}
	}

	@Override
	public boolean onIsMultiPane() {
		return getResources().getBoolean(R.bool.preferences_prefer_dual_pane);
	}

	@Override
	protected boolean isValidFragment(String fragmentName) {
		return true;
	}

	/**
	 * Populate the activity with the top-level headers.
	 */
	@Override
	public void onBuildHeaders(List<Header> target) {
		if (onIsMultiPane()) {
			loadHeadersFromResource(R.xml.preference_header, target);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			// Respond to the action bar's Up/Home button
			case android.R.id.home:
				onBackPressed();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@NonNull
	public static Intent createIntent(@NonNull Context context) {
		return new Intent(context, SettingsActivity.class);
	}

	@NonNull
	public static Intent createIntent(@NonNull Context context, @NonNull String preferenceFragment) {
		return createIntent(context).putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT, preferenceFragment);
	}

	@NonNull
	public static Intent createIntent(@NonNull Context context, @NonNull Class<?> preferenceFragment) {
		return createIntent(context, preferenceFragment.getName());
	}

	private PreferenceScreen getPreferenceScreenFromHeader(@XmlRes int headerRes) {
		List<Header> headers = new ArrayList<>();
		loadHeadersFromResource(headerRes, headers);

		@SuppressWarnings("deprecation")
		PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(this);
		for (Header header : headers) {
			Preference p = new Preference(this);
			p.setTitle(header.titleRes);
			// HACK with setFragment does not work click
			p.setIntent(onBuildStartFragmentIntent(header.fragment, null, header.titleRes, 0));
			preferenceScreen.addPreference(p);
		}

		return preferenceScreen;
	}
}
