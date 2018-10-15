package com.arcao.geocaching4locus.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.transaction
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.base.AbstractActionBarActivity
import com.arcao.geocaching4locus.base.fragment.AbstractPreferenceFragment
import com.arcao.geocaching4locus.settings.fragment.SettingsPreferenceFragment

class SettingsActivity : AbstractActionBarActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            if (intent.hasExtra(EXTRA_SHOW_FRAGMENT)) {
                showFragment(Fragment.instantiate(this, intent.getStringExtra(EXTRA_SHOW_FRAGMENT)))
            } else {
                showFragment(SettingsPreferenceFragment())
            }
        }
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.transaction {
            replace(R.id.fragment, fragment)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // Respond to the action bar's Up/Home button
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val EXTRA_SHOW_FRAGMENT = ":android:show_fragment"

        @JvmStatic
        fun createIntent(context: Context): Intent {
            return Intent(context, SettingsActivity::class.java)
        }

        @JvmStatic
        fun <F : AbstractPreferenceFragment> createIntent(context: Context, preferenceFragment: Class<F>): Intent {
            return createIntent(context).putExtra(EXTRA_SHOW_FRAGMENT, preferenceFragment.name)
        }
    }
}
