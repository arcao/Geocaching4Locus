package com.arcao.geocaching4locus.data.account

import com.github.scribejava.core.oauth.OAuth20Service
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.Instant

abstract class AccountManager(
    private val oAuthService: OAuth20Service
) {
    var account: GeocachingAccount? = null
        protected set

    val authorizationUrl: String
        get() {
            return oAuthService.authorizationUrl
        }

    private val refreshAccountMutex = Mutex()

    var isAccountUpdateInProgress: Boolean = false
        private set

    protected abstract fun loadAccount(): GeocachingAccount?

    abstract fun saveAccount(account: GeocachingAccount?)

    suspend fun createAccount(code: String): GeocachingAccount {
        val token = withContext(Dispatchers.IO) {
            oAuthService.getAccessToken(code)
        }

        val newAccount = GeocachingAccount(
            accountManager = this,
            accessToken = token.accessToken,
            accessTokenExpiration = computeExpiration(token.expiresIn),
            refreshToken = token.refreshToken
        )

        saveAccount(newAccount)
        this.account = newAccount

        return newAccount
    }

    fun deleteAccount() {
        account = null
        saveAccount(account)
    }

    suspend fun refreshAccount(account: GeocachingAccount): Boolean {
        refreshAccountMutex.withLock {
            if (!account.accessTokenExpired) {
                return false
            }

            try {
                isAccountUpdateInProgress = true
                account.apply {
                    val token = withContext(Dispatchers.IO) {
                        oAuthService.refreshAccessToken(refreshToken)
                    }

                    accessToken = token.accessToken
                    accessTokenExpiration = computeExpiration(token.expiresIn)
                    refreshToken = token.refreshToken
                }
                saveAccount(account)

                return true
            } finally {
                isAccountUpdateInProgress = false
            }
        }
    }

    private fun computeExpiration(expiresIn: Int?) = Instant.now()
        .plusSeconds(expiresIn?.toLong() ?: TWO_YEARS_IN_SECONDS)
        .minus(SAFE_OAUTH_TOKEN_REFRESH_DURATION)

    companion object {
        val SAFE_OAUTH_TOKEN_REFRESH_DURATION: Duration = Duration.ofMinutes(2)
        const val TWO_YEARS_IN_SECONDS = 2L * 365L * 24L * 3600L
    }
}
