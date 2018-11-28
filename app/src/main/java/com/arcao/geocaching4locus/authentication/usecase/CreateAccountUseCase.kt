package com.arcao.geocaching4locus.authentication.usecase

import com.arcao.geocaching.api.GeocachingApi
import com.arcao.geocaching.api.exception.InvalidResponseException
import com.arcao.geocaching.api.exception.NetworkException
import com.arcao.geocaching4locus.authentication.util.Account
import com.arcao.geocaching4locus.authentication.util.AccountManager
import com.arcao.geocaching4locus.authentication.util.DeviceInfoFactory
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.github.scribejava.core.oauth.OAuth10aService
import kotlinx.coroutines.withContext
import java.io.IOException

class CreateAccountUseCase(
    private val accountManager: AccountManager,
    private val oAuthService : OAuth10aService,
    private val geocachingApi: GeocachingApi,
    private val deviceInfoFactory: DeviceInfoFactory,
    private val dispatcherProvider: CoroutinesDispatcherProvider

) {
    suspend operator fun invoke(oAuthVerifier : String) : Account = withContext(dispatcherProvider.io) {
        try {
            val requestToken = accountManager.oAuthRequestToken
            val accessToken = oAuthService.getAccessToken(requestToken, oAuthVerifier)

            // open session
            val token = accessToken.token

            geocachingApi.openSession(token)

            // get account name
            val userProfile =
                geocachingApi.getYourUserProfile(
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    deviceInfoFactory()
                )

            val apiLimitsResponse = geocachingApi.apiLimits

            if (userProfile == null)
                throw InvalidResponseException("User profile is null")

            val account = accountManager.createAccount(userProfile.user())
            accountManager.addAccount(account)
            accountManager.oAuthToken = token
            accountManager.deleteOAuthRequestToken()

            // update restrictions
            accountManager.restrictions.updateLimits(apiLimitsResponse.apiLimits())

            account
        } catch (e : IOException) {
            throw NetworkException(e.message, e)
        }
    }
}