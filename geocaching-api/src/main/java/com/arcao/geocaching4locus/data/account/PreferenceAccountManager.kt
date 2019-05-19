package com.arcao.geocaching4locus.data.account

import android.content.Context
import com.arcao.geocaching4locus.data.api.model.enum.MembershipType
import com.github.scribejava.core.oauth.OAuth20Service
import org.threeten.bp.Instant

class PreferenceAccountManager(
    context : Context,
    oAuthService: OAuth20Service
) : AccountManager(oAuthService) {
    private val prefs = context.getSharedPreferences("account", Context.MODE_PRIVATE)
    init {
        account = loadAccount()
    }

    override fun loadAccount(): GeocachingAccount? {
        val accessToken = prefs.getString("accessToken", null) ?: return null
        val expiration = prefs.getLong("expiration", 0)
        val refreshToken = prefs.getString("refreshToken", null) ?: return null
        val userName = prefs.getString("userName", null) ?: return null
        val membership = prefs.getString("membership", null) ?: return null
        val avatarUrl = prefs.getString("avatarUrl", null)
        val bannerUrl = prefs.getString("bannerUrl", null)

        return GeocachingAccount(
            accountManager = this,
            accessToken = accessToken,
            accessTokenExpiration = Instant.ofEpochMilli(expiration),
            refreshToken = refreshToken,
            userName = userName,
            membership = MembershipType.valueOf(membership),
            avatarUrl = avatarUrl,
            bannerUrl = bannerUrl
        )
    }

    override fun saveAccount(account: GeocachingAccount?) {
        if (account == null) {
            prefs.edit().clear().apply()
            return
        }

        prefs.edit().apply {
            putString("accessToken", account.accessToken)
            putLong("expiration", account.accessTokenExpiration.toEpochMilli())
            putString("refreshToken", account.refreshToken)
            putString("userName", account.userName)
            putString("membership", account.membership.value)
            putString("avatarUrl", account.avatarUrl)
            putString("bannerUrl", account.bannerUrl)
        }.apply()
    }
}