package com.arcao.geocaching4locus.data.account

import com.github.scribejava.core.oauth.OAuth20Service
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.threeten.bp.Duration
import org.threeten.bp.Instant

abstract class AccountManager(
        private val oAuthService: OAuth20Service
) {
    companion object {
        val SAFE_OAUTH_TOKEN_REFRESH_DURATION: Duration = Duration.ofMinutes(2)
        const val TWO_YEARS_IN_SECONDS = 2L * 365L * 24L * 3600L
    }

    var account: GeocachingAccount? = null
        protected set

    val authorizationUrl: String
        get() {
            return oAuthService.authorizationUrl
        }

    protected abstract fun loadAccount() : GeocachingAccount?

    abstract fun saveAccount(account: GeocachingAccount?)

    suspend fun createAccount(code: String) : GeocachingAccount {
        val token = withContext(Dispatchers.IO) {
            oAuthService.getAccessToken(code)
        }

        return GeocachingAccount(
                accountManager = this,
                accessToken = token.accessToken,
                accessTokenExpiration = computeExpiration(token.expiresIn),
                refreshToken = token.refreshToken
        ).also {
            this.account = it
            saveAccount(it)
        }
    }

    fun deleteAccount() {
        account = null
        saveAccount(account)
    }

    suspend fun refreshAccount(account: GeocachingAccount) {
        account.apply {
            val token = withContext(Dispatchers.IO) {
                oAuthService.refreshAccessToken(refreshToken)
            }

            accessToken = token.accessToken
            accessTokenExpiration = computeExpiration(token.expiresIn)
            refreshToken = token.refreshToken
        }.also {
            saveAccount(it)
        }
    }

    private fun computeExpiration(expiresIn: Int?) =
            Instant.now()
                    .plusSeconds(expiresIn?.toLong() ?: TWO_YEARS_IN_SECONDS)
                    .minus(SAFE_OAUTH_TOKEN_REFRESH_DURATION)
}