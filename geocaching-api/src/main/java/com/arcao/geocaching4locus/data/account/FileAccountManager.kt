package com.arcao.geocaching4locus.data.account

import com.arcao.geocaching4locus.data.api.model.enum.MembershipType
import com.github.scribejava.core.oauth.OAuth20Service
import org.threeten.bp.Instant
import java.io.File
import java.util.regex.Pattern

class FileAccountManager(oAuthService: OAuth20Service, private val dataFile: File = File("account.dat")) : AccountManager(oAuthService) {
    init {
        account = loadAccount()
    }

    override fun loadAccount() : GeocachingAccount? {
        try {
            val (accessToken, expiration, refreshToken, userName, membership, avatarUrl, bannerUrl) =
                    dataFile.readText().split(NEW_LINE_PATTERN)

            return GeocachingAccount(
                    accountManager = this,
                    accessToken = accessToken,
                    accessTokenExpiration = Instant.ofEpochMilli(expiration.toLong()),
                    refreshToken = refreshToken,
                    userName = userName,
                    membership = MembershipType.valueOf(membership),
                    avatarUrl = avatarUrl,
                    bannerUrl = bannerUrl
            )
        } catch (e: Exception) {
            println(e)
            return null
        }
    }

    override fun saveAccount(account: GeocachingAccount) {
        dataFile.writeText(arrayOf(
                account.accessToken,
                account.accessTokenExpiration.toEpochMilli(),
                account.refreshToken,
                account.userName,
                account.membership.name,
                account.avatarUrl,
                account.bannerUrl
        ).joinToString(System.lineSeparator()))
    }


    private operator fun <E> List<E>.component6(): E? = this[5]
    private operator fun <E> List<E>.component7(): E? = this[6]

    companion object {
        private val NEW_LINE_PATTERN = Pattern.compile("(?:\r\n|\n|\r)")
    }
}