package com.arcao.geocaching4locus.import_bookmarks

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContract
import androidx.fragment.app.commit
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.authentication.LoginActivity
import com.arcao.geocaching4locus.base.AbstractActionBarActivity
import com.arcao.geocaching4locus.base.util.exhaustive
import com.arcao.geocaching4locus.base.util.showLocusMissingError
import com.arcao.geocaching4locus.base.util.withObserve
import com.arcao.geocaching4locus.error.ErrorActivity
import com.arcao.geocaching4locus.import_bookmarks.fragment.BaseBookmarkFragment
import com.arcao.geocaching4locus.import_bookmarks.fragment.BookmarkFragment
import com.arcao.geocaching4locus.import_bookmarks.fragment.BookmarkListFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class ImportBookmarkActivity : AbstractActionBarActivity() {
    private val viewModel by viewModel<ImportBookmarkViewModel>()

    private val loginActivity = registerForActivityResult(LoginActivity.Contract) { success ->
        if (success) {
            viewModel.init()
        } else {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_import_bookmark)

        setSupportActionBar(findViewById(R.id.toolbar))

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }

        @Suppress("IMPLICIT_CAST_TO_ANY")
        viewModel.action.withObserve(this) { action ->
            when (action) {
                ImportBookmarkAction.LocusMapNotInstalled -> {
                    showLocusMissingError()
                }
                ImportBookmarkAction.ShowList -> {
                    supportFragmentManager.commit {
                        replace(R.id.fragment, BookmarkListFragment.newInstance())
                    }
                }
                is ImportBookmarkAction.ChooseBookmark -> {
                    supportFragmentManager.commit {
                        replace(R.id.fragment, BookmarkFragment.newInstance(action.geocacheList))
                        addToBackStack(null)
                    }
                }
                ImportBookmarkAction.PremiumMembershipRequired -> {
                    startActivity(
                        ErrorActivity.IntentBuilder(this).message(R.string.error_premium_feature)
                            .build()
                    )
                    finish()
                }
                is ImportBookmarkAction.SignIn -> loginActivity.launch(null)
            }.exhaustive
        }

        viewModel.progress.withObserve(this, ::handleProgress)

        if (savedInstanceState == null) {
            viewModel.init()
        }
    }

    override fun onProgressCancel(requestId: Int) {
        (supportFragmentManager.findFragmentById(R.id.fragment) as? BaseBookmarkFragment)?.onProgressCancel(
            requestId
        )
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            onBackPressed()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    object Contract : ActivityResultContract<Void?, Boolean>() {
        override fun createIntent(context: Context, input: Void?) =
            Intent(context, ImportBookmarkActivity::class.java)

        override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
            return resultCode == Activity.RESULT_OK
        }
    }
}
