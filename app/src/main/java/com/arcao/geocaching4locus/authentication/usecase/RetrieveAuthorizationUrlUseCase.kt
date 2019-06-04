package com.arcao.geocaching4locus.authentication.usecase

import com.arcao.geocaching4locus.data.account.AccountManager

class RetrieveAuthorizationUrlUseCase(
    private val accountManager: AccountManager
) {
    operator fun invoke(): String = accountManager.authorizationUrl
}