package com.arcao.geocaching4locus.base.usecase

import androidx.annotation.WorkerThread
import com.arcao.geocaching4locus.authentication.util.isAccountUpdateRequired
import com.arcao.geocaching4locus.base.AccountNotFoundException
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.data.account.AccountManager
import com.arcao.geocaching4locus.data.api.GeocachingApiRepository
import kotlinx.coroutines.withContext

class GeocachingApiLoginUseCase(
    private val repository: GeocachingApiRepository,
    private val accountManager: AccountManager,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) {

    @WorkerThread
    @Suppress("BlockingMethodInNonBlockingContext")
    suspend operator fun invoke() = withContext(dispatcherProvider.io) {
        val account = accountManager.account ?: throw AccountNotFoundException("Account not found.")

        if (account.isAccountUpdateRequired()) {
            account.updateUserInfo(repository.user())
        }
    }
}
