package com.arcao.geocaching4locus.authentication

import android.os.Build
import androidx.annotation.UiThread
import com.arcao.geocaching4locus.App
import com.arcao.geocaching4locus.authentication.usecase.CreateAccountUseCase
import com.arcao.geocaching4locus.authentication.usecase.RetrieveAuthorizationUrlUseCase
import com.arcao.geocaching4locus.authentication.util.AccountManager
import com.arcao.geocaching4locus.base.BaseViewModel
import com.arcao.geocaching4locus.base.ProgressState
import com.arcao.geocaching4locus.base.constants.CrashlyticsConstants
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.util.AnalyticsUtil
import com.arcao.geocaching4locus.base.util.Command
import com.arcao.geocaching4locus.base.util.invoke
import com.arcao.geocaching4locus.error.handler.ExceptionHandler
import com.crashlytics.android.Crashlytics
import kotlinx.coroutines.launch

class LoginViewModel(
    private val app: App,
    private val retrieveAuthorizationUrlUseCase: RetrieveAuthorizationUrlUseCase,
    private val createAccountUseCase: CreateAccountUseCase,
    private val exceptionHandler: ExceptionHandler,
    private val accountManager: AccountManager,
    dispatcherProvider: CoroutinesDispatcherProvider
) : BaseViewModel(dispatcherProvider) {

    val action = Command<LoginAction>()

    fun startLogin() {
        if (job.isActive) job.cancel()

        launch {
            try {
                showProgress {
                    app.clearGeocachingCookies()

                    // retrieve authorization url
                    val url = retrieveAuthorizationUrlUseCase()

                    mainContext {
                        action(LoginAction.LoginUrlAvailable(url))
                    }
                }
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    fun finishLogin(input: String) {
        if (input.isBlank()) {
            action(LoginAction.Cancel)
            return
        }

        launch {
            try {
                showProgress {
                    // create account
                    val account = createAccountUseCase(input)

                    val premium = account.premium

                    // handle analytics and crashlytics
                    Crashlytics.setBool(CrashlyticsConstants.PREMIUM_MEMBER, premium)
                    AnalyticsUtil.setPremiumUser(app, premium)
                    AnalyticsUtil.actionLogin(true, premium)


                    mainContext {
                        action(LoginAction.Finish(premium))
                    }
                }
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    private suspend fun handleException(e: Exception) {
        accountManager.removeAccount()

        AnalyticsUtil.actionLogin(false, false)

        mainContext {
            action(LoginAction.Error(exceptionHandler(e)))
        }
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
        AnalyticsUtil.actionLogin(false, false)

        job.cancel()
        action(LoginAction.Cancel)
    }
}