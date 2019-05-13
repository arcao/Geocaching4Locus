package com.arcao.geocaching4locus.authentication.usecase

import com.arcao.geocaching.api.exception.NetworkException
import com.arcao.geocaching4locus.authentication.util.AccountManager
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.github.scribejava.core.oauth.OAuth10aService
import kotlinx.coroutines.withContext
import java.io.IOException

class RetrieveAuthorizationUrlUseCase(
    private val accountManager: AccountManager,
    private val oAuthService: OAuth10aService,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) {

    suspend operator fun invoke(): String = withContext(dispatcherProvider.io) {
        try {
            val requestToken = oAuthService.requestToken

            accountManager.oAuthRequestToken = requestToken
            oAuthService.getAuthorizationUrl(requestToken)
        } catch (e: IOException) {
            throw NetworkException(e.message, e)
        }
    }
}