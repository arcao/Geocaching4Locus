package com.arcao.geocaching4locus.data

import com.arcao.geocaching4locus.data.account.FileAccountManager
import com.arcao.geocaching4locus.data.account.oauth.GeocachingOAuthServiceFactory
import com.arcao.geocaching4locus.data.api.GeocachingApiRepository
import com.arcao.geocaching4locus.data.api.endpoint.GeocachingApiEndpointFactory
import com.arcao.geocaching4locus.data.api.internal.moshi.MoshiFactory
import com.arcao.geocaching4locus.data.api.internal.okhttp.OkHttpClientFactory
import kotlinx.coroutines.runBlocking
import org.threeten.bp.zone.TzdbZoneRulesProvider
import org.threeten.bp.zone.ZoneRulesInitializer
import org.threeten.bp.zone.ZoneRulesProvider
import timber.log.Timber
import java.io.File

fun main() {
    println("Hello")

    // load TZDB for ThreeTenABP
    ZoneRulesInitializer.setInitializer(object : ZoneRulesInitializer() {
        override fun initializeProviders() {
            val stream = File("geocaching-api/src/main/resources/TZDB.dat").inputStream()
            stream.use {
                ZoneRulesProvider.registerProvider(TzdbZoneRulesProvider(it))
            }
        }
    })

    // Init timber
    Timber.plant(object : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            println(message + (if (t != null) " $t" else ""))
        }
    })

    val okHttpClient = OkHttpClientFactory(true).create()
    val moshi = MoshiFactory.create()

    println("World!")

    val oAuthService = GeocachingOAuthServiceFactory(okHttpClient).create()
    val manager = FileAccountManager(oAuthService)
    val endpoint = GeocachingApiEndpointFactory(manager, okHttpClient, moshi).create()
    val api = GeocachingApiRepository(endpoint)

    if (manager.account == null) {
        print("Authorization url: ")
        println(manager.authorizationUrl)

        print("Enter code: ")
        val code = readLine()

        runBlocking {
            val account = manager.createAccount(code!!)
            account.updateUserInfo(api.user())
        }
    }

    runBlocking {
//        println(api.user())
//        println(api.search(
//                listOf(LocationFilter(50.0, 14.0), GeocacheTypeFilter(GeocacheType.TRADITIONAL)),
//                lite = false,
//                take = 30,
//                logsCount = 10
//        ))
//
//        println(api.geocacheLogs("GC12345"))
//        println(api.geocacheImages("GC12345"))

        println(api.userLists())
    }
}