package com.arcao.geocaching4locus;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.arcao.geocaching.api.data.bookmarks.Bookmark;
import com.arcao.geocaching.api.data.bookmarks.BookmarkList;
import com.arcao.geocaching4locus.fragment.BookmarkCachesFragment;
import com.arcao.geocaching4locus.fragment.BookmarkListFragment;
import com.arcao.geocaching4locus.fragment.dialog.BookmarkImportDialogFragment;
import com.arcao.geocaching4locus.util.AnalyticsUtil;
import com.arcao.geocaching4locus.util.LocusTesting;
import timber.log.Timber;

import java.util.HashSet;
import java.util.Set;

public class ImportBookmarkActivity extends AppCompatActivity implements BookmarkListFragment.ListListener, BookmarkCachesFragment.ListListener, BookmarkImportDialogFragment.DialogListener {
  private static final int REQUEST_SIGN_ON = 1;

  @Bind(R.id.toolbar) Toolbar toolbar;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (!LocusTesting.isLocusInstalled(this)) {
      LocusTesting.showLocusMissingError(this);
      return;
    }

    if (!App.get(this).getAuthenticatorHelper().getRestrictions().isPremiumMember()) {
      startActivity(new ErrorActivity.IntentBuilder(this).setMessage(R.string.premium_feature).build());
      finish();
      return;
    }

    setContentView(R.layout.activity_import_bookmark);
    ButterKnife.bind(this);
    setSupportActionBar(toolbar);

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setTitle(getTitle());
      actionBar.setDisplayHomeAsUpEnabled(true);
    }

    // test if user is logged in
    if (App.get(this).getAuthenticatorHelper().requestSignOn(this, REQUEST_SIGN_ON)) {
      return;
    }

    if (savedInstanceState == null)
      showBookmarkList();
  }

  protected void showBookmarkList() {
    getFragmentManager().beginTransaction()
        .replace(R.id.fragment, BookmarkListFragment.newInstance())
        .commit();
  }

  protected void showBookmarkCaches(BookmarkList bookmarkList) {
    getFragmentManager().beginTransaction().addToBackStack(null)
        .replace(R.id.fragment, BookmarkCachesFragment.newInstance(bookmarkList))
        .commit();
  }

  @Override
  public void onBookmarkListSelected(BookmarkList bookmarkList, boolean selectAll) {
    if (selectAll) {
      AnalyticsUtil.actionImportBookmarks(bookmarkList.getItemCount(), true);
      BookmarkImportDialogFragment.newInstance(bookmarkList).show(getFragmentManager(), BookmarkImportDialogFragment.FRAGMENT_TAG);
    } else {
      showBookmarkCaches(bookmarkList);
    }
  }

  @Override
  public void onTitleChanged(String title) {
    toolbar.setSubtitle(title);
  }

  @Override
  public void onBookmarksSelected(Bookmark[] bookmarksList) {
    Set<String> geocaches = new HashSet<>(bookmarksList.length);

    AnalyticsUtil.actionImportBookmarks(bookmarksList.length, false);

    for (Bookmark bookmark : bookmarksList) geocaches.add(bookmark.getCacheCode());

    BookmarkImportDialogFragment.newInstance(geocaches.toArray(new String[geocaches.size()]))
        .show(getFragmentManager(), BookmarkImportDialogFragment.FRAGMENT_TAG);
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

  @Override
  public void onBackPressed() {
    if (getFragmentManager().getBackStackEntryCount() > 0) {
      getFragmentManager().popBackStack();
    } else {
      super.onBackPressed();
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    // restart update process after log in
    if (requestCode == REQUEST_SIGN_ON) {
      if (resultCode == RESULT_OK) {
        showBookmarkList();
      } else {
        finish();
      }
    }
  }

  @Override
  public void onImportFinished(Intent errorIntent) {
    Timber.d("onImportFinished result: " + errorIntent);

    if (errorIntent == null) {
      setResult(RESULT_OK);
      finish();
    } else {
      startActivity(errorIntent);
    }
  }
}
