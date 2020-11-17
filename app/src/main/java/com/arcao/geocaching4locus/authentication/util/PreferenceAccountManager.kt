package com.arcao.geocaching4locus.authentication.util

import android.app.Activity
import android.content.Context
import androidx.core.content.edit
import com.arcao.geocaching4locus.authentication.LoginActivity
import com.arcao.geocaching4locus.base.constants.PrefConstants
import com.arcao.geocaching4locus.data.account.AccountManager
import com.arcao.geocaching4locus.data.account.GeocachingAccount
import com.arcao.geocaching4locus.data.api.model.enums.MembershipType
import com.github.scribejava.core.oauth.OAuth20Service
import java.time.Instant
import java.time.temporal.ChronoUnit

class PreferenceAccountManager(context: Context, oAuthService: OAuth20Service) :
    AccountManager(oAuthService) {
    private val prefs =
        context.getSharedPreferences(PrefConstants.ACCOUNT_STORAGE_NAME, Context.MODE_PRIVATE)
    val restrictions = AccountRestrictions(context)

    init {
        upgradeStorage()
        account = loadAccount()
    }

    override fun loadAccount(): GeocachingAccount? {
        val accessToken = prefs.getString("accessToken", null) ?: return null
        val expiration = prefs.getLong("expiration", 0)
        val refreshToken = prefs.getString("refreshToken", null) ?: return null
        val userName = prefs.getString("userName", null) ?: return null
        val membership = prefs.getInt("membership", 0)
        val avatarUrl = prefs.getString("avatarUrl", null)
        val bannerUrl = prefs.getString("bannerUrl", null)
        val lastUserInfoUpdate = prefs.getLong("lastUserInfoUpdate", 0)

        return GeocachingAccount(
            accountManager = this,
            accessToken = accessToken,
            accessTokenExpiration = Instant.ofEpochMilli(expiration),
            refreshToken = refreshToken,
            userName = userName,
            membership = MembershipType.from(membership),
            avatarUrl = avatarUrl,
            bannerUrl = bannerUrl,
            lastUserInfoUpdate = Instant.ofEpochMilli(lastUserInfoUpdate)
        )
    }

    override fun saveAccount(account: GeocachingAccount?) {
        if (account == null) {
            prefs.edit { clear() }
            restrictions.remove()
            return
        }

        prefs.edit {
            putString("accessToken", account.accessToken)
            putLong("expiration", account.accessTokenExpiration.toEpochMilli())
            putString("refreshToken", account.refreshToken)
            putString("userName", account.userName)
            putInt("membership", account.membership.id)
            putString("avatarUrl", account.avatarUrl)
            putString("bannerUrl", account.bannerUrl)
            putLong("lastUserInfoUpdate", account.lastUserInfoUpdate.toEpochMilli())
        }
    }

    @Suppress("DEPRECATION")
    private fun upgradeStorage() {
        val prefVersion = prefs.getInt(PrefConstants.PREF_VERSION, 0)
        if (prefVersion < 4) {
            // remove old Geocaching API account
            deleteAccount()
        }

        // update pref_version to latest one
        if (prefVersion != PrefConstants.CURRENT_PREF_VERSION) {
            prefs.edit {
                putInt(PrefConstants.PREF_VERSION, PrefConstants.CURRENT_PREF_VERSION)
            }
        }
    }
}

fun AccountManager.requestSignOn(activity: Activity, requestCode: Int): Boolean {
    if (account != null)
        return false

    activity.startActivityForResult(LoginActivity.createIntent(activity), requestCode)
    return true
}

val AccountManager.isPremium: Boolean
    get() = account?.isPremium() ?: false

fun AccountManager.restrictions(): AccountRestrictions =
    (this as PreferenceAccountManager).restrictions

fun GeocachingAccount.isAccountUpdateRequired() =
    Instant.now().minus(1L, ChronoUnit.DAYS).isAfter(lastUserInfoUpdate)
