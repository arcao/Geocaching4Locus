package com.arcao.geocaching4locus.authentication

import android.os.Build
import androidx.annotation.UiThread
import com.arcao.geocaching4locus.App
import com.arcao.geocaching4locus.authentication.usecase.CreateAccountUseCase
import com.arcao.geocaching4locus.authentication.usecase.RetrieveAuthorizationUrlUseCase
import com.arcao.geocaching4locus.base.BaseViewModel
import com.arcao.geocaching4locus.base.ProgressState
import com.arcao.geocaching4locus.base.constants.CrashlyticsConstants
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.util.AnalyticsManager
import com.arcao.geocaching4locus.base.util.Command
import com.arcao.geocaching4locus.base.util.invoke
import com.arcao.geocaching4locus.data.account.AccountManager
import com.arcao.geocaching4locus.error.handler.ExceptionHandler
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

    fun startLogin() {
        if (job?.isActive == true) {
            job?.cancel()
        }

        job = mainLaunch {
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

    fun finishLogin(input: String) {
        if (job?.isActive == true) {
            job?.cancel()
        }

        job = mainLaunch {
            if (input.isBlank()) {
                action(LoginAction.Cancel)
                return@mainLaunch
            }

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
                handleException(e)
            }
        }
    }

    private suspend fun handleException(e: Exception) = mainContext {
        accountManager.deleteAccount()
        analyticsManager.actionLogin(success = false, premiumMember = false)

        action(LoginAction.Error(exceptionHandler(e)))
    }

    fun isCompatLoginRequired() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

    fun showProgress() {
        progress(ProgressState.ShowProgress())
    }

    fun hideProgress() {
        progress(ProgressState.HideProgress)
    }

    @UiThread
    fun cancelLogin() {
        analyticsManager.actionLogin(success = false, premiumMember = false)

        job?.cancel()
        action(LoginAction.Cancel)
    }
}
