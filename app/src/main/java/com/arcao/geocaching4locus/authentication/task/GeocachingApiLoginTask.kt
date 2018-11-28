package com.arcao.geocaching4locus.authentication.task

import android.content.Context
import com.arcao.geocaching.api.GeocachingApi
import com.arcao.geocaching.api.exception.GeocachingApiException
import com.arcao.geocaching.api.exception.InvalidCredentialsException
import com.arcao.geocaching.api.exception.InvalidResponseException
import com.arcao.geocaching4locus.App
import com.arcao.geocaching4locus.authentication.util.Account
import com.arcao.geocaching4locus.authentication.util.DeviceInfoFactory

@Deprecated("Use GeocachingApiLoginUseCase")
class GeocachingApiLoginTask private constructor(context: Context, private val api: GeocachingApi) {
    private val context: Context = context.applicationContext

    @Throws(GeocachingApiException::class)
    fun perform() {
        val accountManager = App[context].accountManager

        var account: Account? = accountManager.account ?: throw InvalidCredentialsException("Account not found.")

        val token = accountManager.oAuthToken
        if (token == null) {
            accountManager.removeAccount()
            throw InvalidCredentialsException("Account not found.")
        }

        api.openSession(token)

        if (accountManager.isAccountUpdateRequired) {
            val userProfile = api.getYourUserProfile(
                false,
                false,
                false,
                false,
                false,
                false,
                DeviceInfoFactory(App.get(context))()
            ) ?: throw InvalidResponseException("User profile is null")

            account = accountManager.createAccount(userProfile.user())
            accountManager.updateAccount(account)
        }
    }

    companion object {
        @JvmStatic
        fun create(context: Context, api: GeocachingApi): GeocachingApiLoginTask {
            return GeocachingApiLoginTask(context, api)
        }
    }
}
