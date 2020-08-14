package com.arcao.geocaching4locus.data.account.oauth.client

import okhttp3.Call
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class OkHttp3Future<T>(private val call: Call) : Future<T> {
    private val latch = CountDownLatch(1)
    private var result: T? = null
    private var exception: Exception? = null

    override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        call.cancel()
        return call.isCanceled
    }

    override fun isCancelled(): Boolean {
        return call.isCanceled
    }

    override fun isDone(): Boolean {
        return call.isExecuted
    }

    fun setException(exception: Exception?) {
        this.exception = exception
    }

    @Throws(InterruptedException::class, ExecutionException::class)
    override fun get(): T? {
        latch.await()
        if (exception != null) {
            throw ExecutionException(exception)
        }
        return result
    }

    @Throws(
        InterruptedException::class,
        ExecutionException::class,
        TimeoutException::class
    )
    override fun get(timeout: Long, unit: TimeUnit): T? {
        if (latch.await(timeout, unit)) {
            if (exception != null) {
                throw ExecutionException(exception)
            }
            return result
        }
        throw TimeoutException()
    }

    fun finish() {
        latch.countDown()
    }

    fun setResult(result: T) {
        this.result = result
    }
}
