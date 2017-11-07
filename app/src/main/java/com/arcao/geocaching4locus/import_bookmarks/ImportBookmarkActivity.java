package com.arcao.geocaching4locus.import_bookmarks;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.arcao.geocaching.api.data.bookmarks.Bookmark;
import com.arcao.geocaching.api.data.bookmarks.BookmarkList;
import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.authentication.util.AccountManager;
import com.arcao.geocaching4locus.base.util.AnalyticsUtil;
import com.arcao.geocaching4locus.base.util.LocusTesting;
import com.arcao.geocaching4locus.base.util.PermissionUtil;
import com.arcao.geocaching4locus.error.ErrorActivity;
import com.arcao.geocaching4locus.error.fragment.NoExternalStoragePermissionErrorDialogFragment;
import com.arcao.geocaching4locus.import_bookmarks.fragment.BookmarkCachesFragment;
import com.arcao.geocaching4locus.import_bookmarks.fragment.BookmarkImportDialogFragment;
import com.arcao.geocaching4locus.import_bookmarks.fragment.BookmarkListFragment;

import java.util.HashSet;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class ImportBookmarkActivity extends AppCompatActivity implements BookmarkListFragment.ListListener, BookmarkCachesFragment.ListListener, BookmarkImportDialogFragment.DialogListener {
    private static final int REQUEST_SIGN_ON = 1;

    @BindView(R.id.toolbar) Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!LocusTesting.isLocusInstalled(this)) {
            LocusTesting.showLocusMissingError(this);
            return;
        }

        AccountManager accountManager = App.get(this).getAccountManager();
        if (!accountManager.isPremium()) {
            startActivity(new ErrorActivity.IntentBuilder(this).message(R.string.error_premium_feature).build());
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
        if (accountManager.requestSignOn(this, REQUEST_SIGN_ON)) {
            return;
        }

        if (savedInstanceState != null)
            return;

        if (PermissionUtil.requestExternalStoragePermission(this))
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
            AnalyticsUtil.actionImportBookmarks(bookmarkList.itemCount(), true);
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

        for (Bookmark bookmark : bookmarksList)
            geocaches.add(bookmark.cacheCode());

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
                if (PermissionUtil.requestExternalStoragePermission(this))
                    showBookmarkList();
            } else {
                finish();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionUtil.REQUEST_EXTERNAL_STORAGE_PERMISSION) {
            if (PermissionUtil.verifyPermissions(grantResults)) {
                showBookmarkList();
            } else {
                NoExternalStoragePermissionErrorDialogFragment.newInstance(true).show(getFragmentManager(), NoExternalStoragePermissionErrorDialogFragment.FRAGMENT_TAG);
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
