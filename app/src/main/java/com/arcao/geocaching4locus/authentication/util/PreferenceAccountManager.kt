package com.arcao.geocaching4locus.authentication.util

import android.app.Activity
import android.content.Context
import android.preference.PreferenceManager
import androidx.core.content.edit
import com.arcao.geocaching.api.data.User
import com.arcao.geocaching.api.data.coordinates.Coordinates
import com.arcao.geocaching.api.data.type.MemberType
import com.arcao.geocaching4locus.authentication.LoginActivity
import com.arcao.geocaching4locus.base.constants.PrefConstants
import com.github.scribejava.core.model.OAuth1RequestToken
import java.util.Date
import java.util.concurrent.TimeUnit

class PreferenceAccountManager(context: Context) : AccountManager {
    private val context: Context = context.applicationContext

    // Do not store username, password and hash in default shared preferences
    // PreferencesBackupAgent backup default shared preferences to Google Backup Service
    private val preferences =
        this.context.getSharedPreferences(PrefConstants.ACCOUNT_STORAGE_NAME, Context.MODE_PRIVATE)
    override val restrictions = AccountRestrictions(this.context)

    override var account: Account? = null
        private set

    private var lastAccountUpdateTime: Long = 0

    override val isPremium: Boolean
        get() = account?.premium ?: false

    override val isAccountUpdateRequired: Boolean
        get() = TimeUnit.MILLISECONDS.toDays(Date().time - lastAccountUpdateTime) > 0

    override var oAuthToken: String?
        get() = if (account == null) null else preferences.getString(PrefConstants.ACCOUNT_SESSION, null)
        set(authToken) {
            if (account == null)
                return

            preferences.edit {
                if (authToken != null) {
                    putString(PrefConstants.ACCOUNT_SESSION, authToken)
                } else {
                    remove(PrefConstants.ACCOUNT_SESSION)
                }
            }
        }

    override var oAuthRequestToken: OAuth1RequestToken?
        get() = OAuth1RequestToken(
            preferences.getString(PrefConstants.OAUTH_TOKEN, ""),
            preferences.getString(PrefConstants.OAUTH_TOKEN_SECRET, ""),
            preferences.getBoolean(PrefConstants.OAUTH_CALLBACK_CONFIRMED, false), null
        )
        set(token) {
            if (token == null || token.isEmpty)
                return

            preferences.edit()
                .putString(PrefConstants.OAUTH_TOKEN, token.token)
                .putString(PrefConstants.OAUTH_TOKEN_SECRET, token.tokenSecret)
                .putBoolean(PrefConstants.OAUTH_CALLBACK_CONFIRMED, token.isOauthCallbackConfirmed)
                .apply()
        }

    init {
        load()
    }

    private fun load() {
        upgradeStorage()

        lastAccountUpdateTime = preferences.getLong(PrefConstants.ACCOUNT_LAST_ACCOUNT_UPDATE_TIME, 0)
        val userName = preferences.getString(PrefConstants.ACCOUNT_USERNAME, null) ?: return

        account = Account(
            name = userName,
            premium = preferences.getBoolean(PrefConstants.ACCOUNT_PREMIUM, false),
            avatarUrl = preferences.getString(PrefConstants.ACCOUNT_AVATAR_URL, null),
            homeCoordinates =
            Coordinates.builder()
                .latitude(
                    preferences.getFloat(
                        PrefConstants.ACCOUNT_HOME_COORDINATES_LAT,
                        java.lang.Float.NaN
                    ).toDouble()
                )
                .longitude(
                    preferences.getFloat(
                        PrefConstants.ACCOUNT_HOME_COORDINATES_LON,
                        java.lang.Float.NaN
                    ).toDouble()
                )
                .build()
        )
    }

    private fun store() {
        if (account != null) {
            preferences.edit {
                putString(PrefConstants.ACCOUNT_USERNAME, account!!.name)
                putBoolean(PrefConstants.ACCOUNT_PREMIUM, account!!.premium)
                putString(PrefConstants.ACCOUNT_AVATAR_URL, account!!.avatarUrl)
                putFloat(PrefConstants.ACCOUNT_HOME_COORDINATES_LAT, java.lang.Float.NaN)
                putFloat(PrefConstants.ACCOUNT_HOME_COORDINATES_LON, java.lang.Float.NaN)
            }

            account!!.homeCoordinates?.let { coordinates ->
                preferences.edit {
                    putFloat(PrefConstants.ACCOUNT_HOME_COORDINATES_LAT, coordinates.latitude().toFloat())
                    putFloat(PrefConstants.ACCOUNT_HOME_COORDINATES_LON, coordinates.longitude().toFloat())
                }
            }
        } else {
            removeAccount()
        }
    }

    override fun createAccount(user: User): Account {
        return Account(
            name = user.userName(),
            premium = user.memberType() == MemberType.Premium || user.memberType() == MemberType.Charter,
            avatarUrl = user.avatarUrl(),
            homeCoordinates = user.homeCoordinates()
        )
    }

    override fun addAccount(account: Account) {
        if (this.account != null)
            removeAccount()

        this.account = account
        lastAccountUpdateTime = Date().time
        store()

        restrictions.applyRestrictions(account.premium)
    }

    override fun removeAccount() {
        account = null

        preferences.edit {
            remove(PrefConstants.ACCOUNT_USERNAME)
            remove(PrefConstants.ACCOUNT_SESSION)
            remove(PrefConstants.ACCOUNT_PREMIUM)
            remove(PrefConstants.ACCOUNT_AVATAR_URL)
            remove(PrefConstants.ACCOUNT_HOME_COORDINATES_LAT)
            remove(PrefConstants.ACCOUNT_HOME_COORDINATES_LON)
        }

        restrictions.remove()
    }

    override fun updateAccountNextTime() {
        lastAccountUpdateTime = 0
        store()
    }

    override fun updateAccount(account: Account) {
        this.account = account
        lastAccountUpdateTime = Date().time
        store()
    }

    override fun invalidateOAuthToken() {
        preferences.edit().remove(PrefConstants.ACCOUNT_SESSION).apply()
    }

    @Suppress("DEPRECATION")
    private fun upgradeStorage() {
        val defaultPref = PreferenceManager.getDefaultSharedPreferences(context)

        val prefVersion = preferences.getInt(PrefConstants.PREF_VERSION, 0)
        if (prefVersion < 1) {
            // remove user name, password and session from old storage

            defaultPref.edit {
                remove(PrefConstants.ACCOUNT_USERNAME)
                remove(PrefConstants.ACCOUNT_PASSWORD)
                remove(PrefConstants.ACCOUNT_SESSION)
            }
        }
        if (prefVersion < 3) {
            // remove old account with unset home coordinates and avatar
            removeAccount()
        }

        // update pref_version to latest one
        if (prefVersion != PrefConstants.CURRENT_PREF_VERSION)
            preferences.edit().putInt(PrefConstants.PREF_VERSION, PrefConstants.CURRENT_PREF_VERSION).apply()
    }

    override fun requestSignOn(activity: Activity, requestCode: Int): Boolean {
        if (account != null)
            return false

        activity.startActivityForResult(LoginActivity.createIntent(activity), requestCode)
        return true
    }

    override fun deleteOAuthRequestToken() {
        preferences.edit {
            remove(PrefConstants.OAUTH_TOKEN)
            remove(PrefConstants.OAUTH_TOKEN_SECRET)
            remove(PrefConstants.OAUTH_CALLBACK_CONFIRMED)
        }
    }
}
