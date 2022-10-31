package com.arcao.geocaching4locus.data.account

import com.arcao.geocaching4locus.data.api.model.User
import com.arcao.geocaching4locus.data.api.model.enums.MembershipType
import java.time.Instant

data class GeocachingAccount(
    private val accountManager: AccountManager,
    var accessToken: String,
    var accessTokenExpiration: Instant = Instant.now(),
    var refreshToken: String,
    var userName: String? = null,
    var membership: MembershipType = MembershipType.UNKNOWN,
    var avatarUrl: String? = null,
    var bannerUrl: String? = null,
    var lastUserInfoUpdate: Instant = Instant.now()
) {

    val accessTokenExpired: Boolean
        get() = Instant.now().isAfter(accessTokenExpiration)

    val isAccountUpdateInProgress: Boolean
        get() = accountManager.isAccountUpdateInProgress

    suspend fun refreshToken(): Boolean = accountManager.refreshAccount(this)

    fun updateUserInfo(user: User) {
        userName = user.username
        membership = fixMembership(user)
        avatarUrl = user.avatarUrl
        bannerUrl = user.bannerUrl
        lastUserInfoUpdate = Instant.now()

        accountManager.saveAccount(this)
    }

    private fun fixMembership(user: User): MembershipType {
        if (user.membership != MembershipType.UNKNOWN) {
            return user.membership
        }

        // if user restricts personal info, try to get Membership from limits
        if (user.geocacheLimits != null && user.geocacheLimits.fullCallsRemaining > 3) {
            return MembershipType.PREMIUM
        }

        return MembershipType.BASIC
    }

    fun isPremium(): Boolean = when (membership) {
        MembershipType.UNKNOWN -> false
        MembershipType.BASIC -> false
        MembershipType.CHARTER -> true
        MembershipType.PREMIUM -> true
    }
}
