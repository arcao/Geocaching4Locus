package com.arcao.geocaching4locus.authentication.usecase

import com.arcao.geocaching4locus.authentication.util.restrictions
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.data.account.AccountManager
import com.arcao.geocaching4locus.data.account.GeocachingAccount
import com.arcao.geocaching4locus.data.api.GeocachingApiRepository
import kotlinx.coroutines.withContext

class UpdateAccountUseCase(
    private val accountManager: AccountManager,
    private val api: GeocachingApiRepository,
    private val dispatcherProvider: CoroutinesDispatcherProvider

) {
    suspend operator fun invoke(): GeocachingAccount = withContext(dispatcherProvider.io) {
        val account = requireNotNull(accountManager.account)
        val user = api.user()
        val wasPremium = account.isPremium()
        account.updateUserInfo(user)

        // update restrictions, the Premium defaults are applied only if the membership has changed
        accountManager.restrictions().apply {
            updateLimits(user)
            applyRestrictions(user, applyPremiumDefaults = !wasPremium)
        }

        account
    }
}