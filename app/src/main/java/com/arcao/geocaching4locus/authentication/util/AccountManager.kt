package com.arcao.geocaching4locus.authentication.util

import android.app.Activity

import com.arcao.geocaching.api.data.User
import com.github.scribejava.core.model.OAuth1RequestToken

interface AccountManager {
    val account: Account?

    val isPremium: Boolean

    var oAuthToken: String?

    val isAccountUpdateRequired: Boolean

    val restrictions: AccountRestrictions

    var oAuthRequestToken: OAuth1RequestToken?

    fun createAccount(user: User): Account

    fun addAccount(account: Account)

    fun removeAccount()

    fun invalidateOAuthToken()

    fun updateAccountNextTime()

    fun updateAccount(account: Account)

    fun requestSignOn(activity: Activity, requestCode: Int): Boolean

    fun deleteOAuthRequestToken()
}