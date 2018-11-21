package com.arcao.geocaching.api.oauth.services

import com.arcao.wherigoservice.api.WherigoApiFactory
import com.github.scribejava.core.services.TimestampServiceImpl
import timber.log.Timber
import java.util.Random

class ServerTimestampServiceImpl : TimestampServiceImpl() {
    private val rand = Random()
    private val ts: Long by lazy {
        val time = try {
            WherigoApiFactory.create().getTime().also {
                Timber.i("server time received (ms): %d", it)
            }
        } catch (e: Exception) {
            System.currentTimeMillis().also {
                Timber.e(e, "No server time received. Used system time (ms): %d", it)
            }
        }

        // timestamp in seconds
        time / 1000
    }

    override fun getTimestampInSeconds(): String {
        return ts.toString()
    }

    override fun getNonce(): String {
        return (ts + rand.nextInt()).toString()
    }
}
