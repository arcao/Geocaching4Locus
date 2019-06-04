package com.arcao.feedback.collector

import com.arcao.geocaching4locus.App
import com.arcao.geocaching4locus.base.constants.PrefConstants
import com.arcao.geocaching4locus.data.account.AccountManager

class AccountInfoCollector(private val app: App, private val accountManager: AccountManager) : Collector() {
    override val name: String
        get() = "AccountInfo"

    override suspend fun collect(): String {
        val sb = StringBuilder()

        val account = accountManager.account
        if (account == null) {
            sb.append("No Account").append("\n")
        } else {
            sb.append("NAME=").append(account.userName).append("\n")

            sb.append("\n--- RESTRICTIONS ---\n")
            sb.append(SharedPreferencesCollector(app, PrefConstants.RESTRICTION_STORAGE_NAME).collect())
        }
        return sb.toString()
    }
}
