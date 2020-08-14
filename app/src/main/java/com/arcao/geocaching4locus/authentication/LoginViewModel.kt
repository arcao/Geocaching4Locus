package com.arcao.geocaching4locus.authentication

import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.arcao.geocaching4locus.App
import com.arcao.geocaching4locus.authentication.usecase.CreateAccountUseCase
import com.arcao.geocaching4locus.authentication.usecase.RetrieveAuthorizationUrlUseCase
import com.arcao.geocaching4locus.base.BaseViewModel
import com.arcao.geocaching4locus.base.constants.CrashlyticsConstants
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.util.AnalyticsManager
import com.arcao.geocaching4locus.base.util.Command
import com.arcao.geocaching4locus.base.util.invoke
import com.arcao.geocaching4locus.data.account.AccountManager
import com.arcao.geocaching4locus.error.handler.ExceptionHandler
import com.github.scribejava.core.model.OAuthConstants
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.Job

class LoginViewModel(
    private val app: App,
    private val retrieveAuthorizationUrl: RetrieveAuthorizationUrlUseCase,
    private val createAccount: CreateAccountUseCase,
    private val exceptionHandler: ExceptionHandler,
    private val accountManager: AccountManager,
    private val analyticsManager: AnalyticsManager,
    dispatcherProvider: CoroutinesDispatcherProvider
) : BaseViewModel(dispatcherProvider) {
    val action = Command<LoginAction>()
    private var job: Job? = null

    val code = MutableLiveData<String>("")
    val continueButtonEnabled = Transformations.map(code, String::isNotBlank)
    val formVisible = MutableLiveData(true)
    var fromIntent = false
        private set

    fun startLogin() {
        if (job?.isActive == true) {
            job?.cancel()
        }

        job = mainImmediateLaunch {
            try {
                showProgress {
                    app.clearGeocachingCookies()

                    // retrieve authorization url
                    val url = retrieveAuthorizationUrl()

                    action(LoginAction.LoginUrlAvailable(url))
                }
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    private fun finishLogin(input: String) {
        if (job?.isActive == true) {
            job?.cancel()
        }

        job = mainLaunch {
            if (input.isBlank()) {
                action(LoginAction.Cancel)
                return@mainLaunch
            }

            formVisible(false)

            try {
                showProgress {
                    // create account
                    val account = createAccount(input)

                    val premium = account.isPremium()

                    // handle analytics and crashlytics
                    FirebaseCrashlytics.getInstance()
                        .setCustomKey(CrashlyticsConstants.PREMIUM_MEMBER, premium)
                    analyticsManager.setPremiumMember(premium)
                    analyticsManager.actionLogin(true, premium)

                    action(LoginAction.Finish(!premium))
                }
            } catch (e: Exception) {
                if (!fromIntent) {
                    formVisible(true)
                }

                handleException(e)
            }
        }
    }

    private suspend fun handleException(e: Exception) = mainContext {
        accountManager.deleteAccount()
        analyticsManager.actionLogin(success = false, premiumMember = false)

        action(LoginAction.Error(exceptionHandler(e)))
    }

    fun onCancelClicked() {
        analyticsManager.actionLogin(success = false, premiumMember = false)

        job?.cancel()
        action(LoginAction.Cancel)
    }

    fun onContinueButtonClicked() {
        val input = code.value ?: return
        fromIntent = false
        finishLogin(input)
    }

    fun handleIntent(intent: Intent): Boolean {
        val data = intent.data
        val action = intent.action

        if (Intent.ACTION_VIEW == action && data != null && data.getQueryParameter(OAuthConstants.CODE) != null) {
            val code = requireNotNull(data.getQueryParameter(OAuthConstants.CODE))
            fromIntent = true
            finishLogin(code)
            return true
        }

        return false
    }
}
