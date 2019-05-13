package com.arcao.geocaching4locus.base

import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.util.invoke
import com.arcao.geocaching4locus.base.util.runIfIsSuspended
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

abstract class BaseViewModel(
    val dispatcherProvider: CoroutinesDispatcherProvider
) : ViewModel(), CoroutineScope {
    val job = Job()
    val progress: MutableLiveData<ProgressState> = MutableLiveData()

    // Better to start on main context to minimize cost of switching between computation and main context
    // Also it is better to read code without switching
    // If you need more time to do work, switch to computation context
    override val coroutineContext: CoroutineContext
        get() = dispatcherProvider.main + job

    override fun onCleared() {
        job.cancel()
        super.onCleared()
    }

    fun <R> mainLaunch(block: suspend CoroutineScope.() -> R) = launch(dispatcherProvider.main) {
        block()
    }

    fun <R> computationLaunch(block: suspend CoroutineScope.() -> R) = launch(dispatcherProvider.computation) {
        block()
    }

    suspend fun <R> mainContext(block: suspend CoroutineScope.() -> R): R =
        withContext(dispatcherProvider.main) {
            block()
        }

    suspend fun <R> computationContext(block: suspend CoroutineScope.() -> R): R =
        withContext(dispatcherProvider.computation) {
            block()
        }

    suspend fun <R> showProgress(
        @StringRes message: Int = 0,
        messageArgs: Array<Any>? = null,
        progress: Int = 0,
        maxProgress: Int = 0,
        requestId: Int = 0,
        block: suspend CoroutineScope.() -> R
    ): R = coroutineScope {

        mainContext {
            progress(ProgressState.ShowProgress(requestId, message, messageArgs, progress, maxProgress))
        }

        try {
            return@coroutineScope block()
        } finally {
            mainContext {
                progress(ProgressState.HideProgress)
            }
        }
    }

    suspend fun updateProgress(
        requestId: Int = 0,
        @StringRes message: Int = 0,
        messageArgs: Array<Any>? = null,
        progress: Int = -1,
        maxProgress: Int = -1
    ) {
        this.progress.value.runIfIsSuspended(ProgressState.ShowProgress::class) {
            mainContext {
                progress(
                    ProgressState.ShowProgress(
                        if (requestId == 0) {
                            this@runIfIsSuspended.requestId
                        } else {
                            requestId
                        },
                        if (message == 0) {
                            this@runIfIsSuspended.message
                        } else {
                            message
                        },
                        messageArgs ?: this@runIfIsSuspended.messageArgs,
                        if (progress < 0) {
                            this@runIfIsSuspended.progress
                        } else {
                            progress
                        },
                        if (maxProgress < 0) {
                            this@runIfIsSuspended.maxProgress
                        } else {
                            maxProgress
                        }
                    )
                )
            }
        }
    }
}

sealed class ProgressState {
    class ShowProgress(
        val requestId: Int = 0,
        @StringRes val message: Int = 0,
        val messageArgs: Array<Any>? = null,
        val progress: Int = 0,
        val maxProgress: Int = 0
    ) : ProgressState()

    object HideProgress : ProgressState()
}
