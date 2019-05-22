package com.arcao.geocaching4locus.authentication.usecase

import com.arcao.geocaching4locus.authentication.util.restrictions
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.data.account.AccountManager
import com.arcao.geocaching4locus.data.api.GeocachingApiRepository
import kotlinx.coroutines.withContext

class CreateAccountUseCase(
    private val accountManager: AccountManager,
    private val api: GeocachingApiRepository,
    private val dispatcherProvider: CoroutinesDispatcherProvider

) {
    suspend operator fun invoke(code: String) = withContext(dispatcherProvider.io) {
        val account = accountManager.createAccount(code)
        val user = api.user()
        account.updateUserInfo(user)

        // update restrictions
        accountManager.restrictions().apply {
            updateLimits(user)
            applyRestrictions(user)
        }

        account
    }
}