package com.arcao.geocaching4locus;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.arcao.geocaching.api.data.bookmarks.BookmarkList;
import com.arcao.geocaching4locus.fragment.BookmarkListFragment;
import com.arcao.geocaching4locus.util.LocusTesting;

public class ImportBookmarkActivity extends AppCompatActivity implements BookmarkListFragment.ListListener {
	private static final int REQUEST_LOGIN = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!LocusTesting.isLocusInstalled(this)) {
			LocusTesting.showLocusMissingError(this);
			return;
		}

		// test if user is logged in
		if (!App.get(this).getAuthenticatorHelper().isLoggedIn(this, REQUEST_LOGIN)) {
			return;
		}

		setContentView(R.layout.activity_import_bookmark);
		setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setTitle(getTitle());
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		showBookmarkList();
	}

	protected void showBookmarkList() {
		Fragment fragment = getFragmentManager().findFragmentById(R.id.fragment);
		if (!(fragment instanceof BookmarkListFragment)) {
			getFragmentManager().beginTransaction()
							.replace(R.id.fragment, BookmarkListFragment.newInstance())
							.commit();
		}
	}

	@Override
	public void onBookmarkSelected(BookmarkList bookmarkList) {

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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// restart update process after log in
		if (requestCode == REQUEST_LOGIN) {
			if (resultCode == RESULT_OK) {
				showBookmarkList();
			}
		}
	}
}
