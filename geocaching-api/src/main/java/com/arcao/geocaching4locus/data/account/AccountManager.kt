package com.arcao.geocaching4locus.data.account

import com.github.scribejava.core.oauth.AccessTokenRequestParams
import com.github.scribejava.core.oauth.OAuth20Service
import com.github.scribejava.core.pkce.PKCEService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.Instant
import kotlin.random.Random

abstract class AccountManager(
    private val oAuthService: OAuth20Service
) {
    private val pkceService: PKCEService by lazy {
        PKCEService.defaultInstance()
    }

    var account: GeocachingAccount? = null
        protected set

    val authorizationUrl: String
        get() {
            val randomBytes = Random.nextBytes(32)
            savePkceRandom(randomBytes)

            return oAuthService.createAuthorizationUrlBuilder().pkce(
                PKCEService.defaultInstance().generatePKCE(randomBytes)
            ).build()
        }

    private val refreshAccountMutex = Mutex()

    var isAccountUpdateInProgress: Boolean = false
        private set

    open fun saveAccount(account: GeocachingAccount?) {
        this.account = account
    }

    abstract fun savePkceRandom(randomBytes: ByteArray)

    abstract fun loadPkceRandom(): ByteArray

    suspend fun createAccount(code: String): GeocachingAccount {
        val token = withContext(Dispatchers.IO) {
            val pkce = pkceService.generatePKCE(loadPkceRandom())

            oAuthService.getAccessToken(
                AccessTokenRequestParams.create(code).pkceCodeVerifier(pkce.codeVerifier)
            )
        }

        val newAccount = GeocachingAccount(
            accountManager = this,
            accessToken = token.accessToken,
            accessTokenExpiration = computeExpiration(token.expiresIn),
            refreshToken = token.refreshToken
        )

        saveAccount(newAccount)

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
