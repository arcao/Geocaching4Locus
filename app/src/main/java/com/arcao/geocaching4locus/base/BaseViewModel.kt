package com.arcao.geocaching4locus.base

import androidx.lifecycle.ViewModel
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

abstract class BaseViewModel(
        val dispatcherProvider: CoroutinesDispatcherProvider
) : ViewModel(), CoroutineScope {
    val job = Job()

    override val coroutineContext: CoroutineContext
        get() = dispatcherProvider.computation + job

    override fun onCleared() {
        job.cancel()
        super.onCleared()
    }

    suspend inline fun <reified T : BaseViewModel, R> T.mainContext(crossinline block: suspend T.() -> R): R =
            withContext(dispatcherProvider.main) {
                this@mainContext.block()
            }
}
