package com.arcao.geocaching4locus.authentication

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.authentication.fragment.OAuthLoginCompatFragment
import com.arcao.geocaching4locus.authentication.fragment.OAuthLoginFragment
import com.arcao.geocaching4locus.base.AbstractActionBarActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class LoginActivity : AbstractActionBarActivity() {
    val viewModel by viewModel<LoginViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)
        setSupportActionBar(findViewById(R.id.toolbar))

        supportActionBar?.apply {
            title = this@LoginActivity.title
            setDisplayHomeAsUpEnabled(true)
        }

        Timber.i("source: login")

        if (savedInstanceState == null) {
            showLoginFragment()
        }
    }

    override fun onProgressCancel(requestId: Int) {
        viewModel.cancelLogin()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // Respond to the action bar's Up/Home button
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // fis for crash in WebView: https://issuetracker.google.com/issues/141132133#comment6
    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        if (Build.VERSION.SDK_INT in 21..25 && (resources.configuration.uiMode == applicationContext.resources.configuration.uiMode)) {
            return
        }
        super.applyOverrideConfiguration(overrideConfiguration)
    }

    private fun showLoginFragment() {
        if (viewModel.isCompatLoginRequired()) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment, OAuthLoginFragment.newInstance())
                .commit()
        } else {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment, OAuthLoginCompatFragment.newInstance())
                .commit()
        }
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, LoginActivity::class.java)
        }
    }
}
