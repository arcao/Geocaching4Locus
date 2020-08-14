package com.arcao.geocaching4locus.authentication

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.addCallback
import androidx.appcompat.widget.Toolbar
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import com.arcao.geocaching4locus.authentication.fragment.BasicMembershipWarningDialogFragment
import com.arcao.geocaching4locus.base.AbstractActionBarActivity
import com.arcao.geocaching4locus.base.ProgressState
import com.arcao.geocaching4locus.base.util.exhaustive
import com.arcao.geocaching4locus.base.util.showWebPage
import com.arcao.geocaching4locus.base.util.withObserve
import com.arcao.geocaching4locus.databinding.ActivityLoginBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class LoginActivity : AbstractActionBarActivity() {
    val viewModel by viewModel<LoginViewModel>()

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        binding.vm = viewModel

        setContentView(binding.root)
        @Suppress("USELESS_CAST")
        setSupportActionBar(binding.toolbar as Toolbar)

        supportActionBar?.apply {
            title = this@LoginActivity.title
            setDisplayHomeAsUpEnabled(true)
        }

        Timber.i("source: login")

        if (savedInstanceState == null) {
            if (!viewModel.handleIntent(intent)) {
                viewModel.startLogin()
            }
        }

        viewModel.action.withObserve(this, ::handleAction)
        viewModel.progress.withObserve(this) { state ->
            when (state) {
                is ProgressState.ShowProgress -> binding.layoutProgress.visibility = View.VISIBLE
                ProgressState.HideProgress -> binding.layoutProgress.visibility = View.GONE
            }.exhaustive
        }

        onBackPressedDispatcher.addCallback {
            viewModel.onCancelClicked()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        viewModel.handleIntent(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // Respond to the action bar's Up/Home button
            android.R.id.home -> {
                viewModel.onCancelClicked()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun handleAction(action: LoginAction) {
        when (action) {
            is LoginAction.LoginUrlAvailable -> onLoginUrlAvailable(action.url)
            is LoginAction.Finish -> {
                finishAction(action.showBasicMembershipWarning)
            }
            is LoginAction.Error -> {
                startActivity(action.intent)
                if (viewModel.fromIntent) {
                    cancelAction()
                }
                Unit
            }
            LoginAction.Cancel -> cancelAction()
        }.exhaustive
    }

    private fun onLoginUrlAvailable(url: String) {
        try {
            CustomTabsIntent.Builder()
                .setInstantAppsEnabled(true)
                .enableUrlBarHiding()
                .build().launchUrl(this, url.toUri())
        } catch (e: ActivityNotFoundException) {
            showWebPage(url.toUri())
        }
    }

    private fun cancelAction() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    private fun finishAction(showBasicMembershipWarning: Boolean) {
        setResult(Activity.RESULT_OK)

        if (showBasicMembershipWarning) {
            BasicMembershipWarningDialogFragment.newInstance().show(supportFragmentManager)
            return
        }

        finish()
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, LoginActivity::class.java)
        }
    }
}
