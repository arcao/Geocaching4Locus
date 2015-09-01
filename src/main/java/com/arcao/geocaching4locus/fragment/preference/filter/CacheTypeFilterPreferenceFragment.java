package com.arcao.geocaching4locus.fragment.preference.filter;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.arcao.geocaching.api.data.type.GeocacheType;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.fragment.preference.AbstractPreferenceFragment;

public class CacheTypeFilterPreferenceFragment extends AbstractPreferenceFragment {
	@Override
	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preference_category_filter_cache_type);
	}

	@Override
	protected void preparePreference() {
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.toolbar_select_deselect, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				// app icon in action bar clicked; go home
				getActivity().finish();
				return true;

			case R.id.selectAll:
				for (int i = 0; i < GeocacheType.values().length; i++)
					findPreference(FILTER_CACHE_TYPE_PREFIX + i, CheckBoxPreference.class).setChecked(true);
				return true;

			case R.id.deselectAll:
				for (int i = 0; i < GeocacheType.values().length; i++)
					findPreference(FILTER_CACHE_TYPE_PREFIX + i, CheckBoxPreference.class).setChecked(false);
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
