package com.arcao.geocaching4locus;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import java.util.List;

public class SettingsActivity extends PreferenceActivity {
	private Toolbar mToolBar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		prepareLayout();
	}

	@Override
	protected void onTitleChanged(CharSequence title, int color) {
		super.onTitleChanged(title, color);
		if (mToolBar != null) {
			mToolBar.setTitle(title);
		}
	}

	private void prepareLayout() {
		LinearLayout root = (LinearLayout)findViewById(android.R.id.list).getParent().getParent().getParent();
		mToolBar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.toolbar, root, false);
		root.addView(mToolBar, 0); // insert at top
		mToolBar.setTitle(getTitle());
		mToolBar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
		mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	public Toolbar getToolBar() {
		return mToolBar;
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
		loadHeadersFromResource(R.xml.preference_header, target);
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
		return createIntent(context).putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT, preferenceFragment);
	}

	public static Intent createIntent(Context context, Class<?> preferenceFragment) {
		return createIntent(context, preferenceFragment.getName());
	}
}
