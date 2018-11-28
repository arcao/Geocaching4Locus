package com.arcao.geocaching4locus.base.usecase

import androidx.annotation.WorkerThread
import com.arcao.geocaching.api.GeocachingApi
import com.arcao.geocaching.api.exception.GeocachingApiException
import com.arcao.geocaching.api.exception.InvalidCredentialsException
import com.arcao.geocaching.api.exception.InvalidResponseException
import com.arcao.geocaching4locus.App
import com.arcao.geocaching4locus.authentication.util.AccountManager
import com.arcao.geocaching4locus.authentication.util.DeviceInfoFactory
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import kotlinx.coroutines.withContext

class GeocachingApiLoginUseCase(
    private val app: App,
    private val accountManager: AccountManager,
    private val deviceInfoFactory: DeviceInfoFactory,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) {

    @WorkerThread
    @Throws(GeocachingApiException::class)
    suspend operator fun invoke(geocachingApi: GeocachingApi) = withContext(dispatcherProvider.io) {
        val token = accountManager.oAuthToken
        if (token == null) {
            accountManager.removeAccount()
            throw InvalidCredentialsException("Account not found.")
        }

        geocachingApi.openSession(token)

        if (accountManager.isAccountUpdateRequired) {
            val userProfile = geocachingApi.getYourUserProfile(
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    deviceInfoFactory()
            ) ?: throw InvalidResponseException("User profile is null")

            accountManager.updateAccount(accountManager.createAccount(userProfile.user()))
        }
    }
}