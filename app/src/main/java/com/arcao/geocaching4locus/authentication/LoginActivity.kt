package com.arcao.geocaching4locus.authentication

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContract
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.core.view.isVisible
import com.arcao.geocaching4locus.authentication.fragment.BasicMembershipWarningDialogFragment
import com.arcao.geocaching4locus.authentication.fragment.UnknownMembershipWarningDialogFragment
import com.arcao.geocaching4locus.base.AbstractActionBarActivity
import com.arcao.geocaching4locus.base.ProgressState
import com.arcao.geocaching4locus.base.util.bindText
import com.arcao.geocaching4locus.base.util.exhaustive
import com.arcao.geocaching4locus.base.util.showWebPage
import com.arcao.geocaching4locus.base.util.withObserve
import com.arcao.geocaching4locus.data.api.model.enums.MembershipType
import com.arcao.geocaching4locus.databinding.ActivityLoginBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class LoginActivity : AbstractActionBarActivity() {
    val viewModel by viewModel<LoginViewModel>()

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)

        setContentView(binding.root)

        setSupportActionBar(binding.toolbar.root)

        supportActionBar?.apply {
            title = this@LoginActivity.title
            setDisplayHomeAsUpEnabled(true)
        }

        binding.input.bindText(this, viewModel.code) { viewModel.code.value = it }
        binding.buttonContinue.setOnClickListener { viewModel.onContinueButtonClicked() }
        viewModel.continueButtonEnabled.observe(this) { binding.buttonContinue.isEnabled = it }
        viewModel.formVisible.observe(this) { binding.layoutInput.isVisible = it }

        Timber.i("source: login")

        if (!viewModel.handleIntent(intent)) {
            if (savedInstanceState == null) {
                viewModel.startLogin()
            }
        }

        viewModel.action.withObserve(this, ::handleAction)
        viewModel.progress.withObserve(this) { state ->
            when (state) {
                is ProgressState.ShowProgress -> binding.layoutProgress.isVisible = true
                ProgressState.HideProgress -> binding.layoutProgress.isVisible = false
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
            is LoginAction.Finish -> finishAction(action.membership)
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
                .setUrlBarHidingEnabled(true)
                .build().launchUrl(this, url.toUri())
        } catch (e: ActivityNotFoundException) {
            showWebPage(url.toUri())
        } catch (e: NoSuchMethodError) {
            // androidx.browser calls ActivityOptions.setShareIdentityEnabled on SDK 34+,
            // but some Android 14 builds don't have it
            showWebPage(url.toUri())
        }
    }

    private fun cancelAction() {
        setResult(RESULT_CANCELED)
        finish()
    }

    private fun finishAction(membership: MembershipType) {
        setResult(RESULT_OK)

        if (membership == MembershipType.BASIC) {
            BasicMembershipWarningDialogFragment.newInstance().show(supportFragmentManager)
            return
        }

        if (membership == MembershipType.UNKNOWN) {
            UnknownMembershipWarningDialogFragment.newInstance().show(supportFragmentManager)
            return
        }

        finish()
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, LoginActivity::class.java)
        }

        fun createResponseHandlingIntent(context: Context, responseUri: Uri?): Intent {
            val intent = createIntent(context)
            intent.data = responseUri
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            return intent
        }
    }

    object Contract : ActivityResultContract<Void?, Boolean>() {
        override fun createIntent(context: Context, input: Void?) =
            Intent(context, LoginActivity::class.java)

        override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
            return resultCode == RESULT_OK
        }
    }
}
