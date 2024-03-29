package com.arcao.geocaching4locus.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.base.AbstractActionBarActivity
import com.arcao.geocaching4locus.base.fragment.AbstractPreferenceFragment
import com.arcao.geocaching4locus.settings.fragment.SettingsPreferenceFragment

class SettingsActivity : AbstractActionBarActivity(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback,
    PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            if (intent.hasExtra(EXTRA_SHOW_FRAGMENT)) {
                showFragment(
                    supportFragmentManager.fragmentFactory.instantiate(
                        classLoader,
                        requireNotNull(intent?.getStringExtra(EXTRA_SHOW_FRAGMENT))
                    )
                )
            } else {
                showFragment(SettingsPreferenceFragment())
            }
        }
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.commit {
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

    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat,
        pref: Preference
    ): Boolean {
        supportFragmentManager.commit {
            replace(
                R.id.fragment,
                supportFragmentManager.fragmentFactory.instantiate(
                    classLoader,
                    pref.fragment ?: return@commit
                )
            )
            addToBackStack(null)
        }

        return true
    }

    override fun onPreferenceDisplayDialog(
        caller: PreferenceFragmentCompat,
        pref: Preference
    ): Boolean {
        if (pref is PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback) {
            return pref.onPreferenceDisplayDialog(caller, pref)
        }

        return false
    }

    companion object {
        private const val EXTRA_SHOW_FRAGMENT = ":android:show_fragment"
    }

    object Contract : ActivityResultContract<Class<out AbstractPreferenceFragment>?, Boolean>() {
        override fun createIntent(context: Context, input: Class<out AbstractPreferenceFragment>?) =
            Intent(context, SettingsActivity::class.java).apply {
                if (input != null) {
                    putExtra(EXTRA_SHOW_FRAGMENT, input.name)
                }
            }

        override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
            return resultCode == Activity.RESULT_OK
        }
    }
}
