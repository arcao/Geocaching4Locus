package com.arcao.geocaching4locus;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.support.annotation.XmlRes;
import android.view.MenuItem;
import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatPreferenceActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// hack / fix for Samsung phones - missing padding in header layout
		if (!onIsMultiPane() && !getIntent().hasExtra(EXTRA_SHOW_FRAGMENT)) {
			setPreferenceScreen(getPrefernceScreenFromHeader(R.xml.preference_header));
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

	public static Intent createIntent(Context context) {
		return new Intent(context, SettingsActivity.class);
	}

	public static Intent createIntent(Context context, String preferenceFragment) {
		return createIntent(context).putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT, preferenceFragment);
	}

	public static Intent createIntent(Context context, Class<?> preferenceFragment) {
		return createIntent(context, preferenceFragment.getName());
	}

	private PreferenceScreen getPrefernceScreenFromHeader(@XmlRes int headerRes) {
		List<Header> headers = new ArrayList<>();
		loadHeadersFromResource(headerRes, headers);

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
