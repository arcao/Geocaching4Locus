package com.arcao.geocaching4locus.base

import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.util.runIfIsSuspended
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BaseViewModel(
    val dispatcherProvider: CoroutinesDispatcherProvider
) : ViewModel() {
    val progress: MutableLiveData<ProgressState> = MutableLiveData()

    inline fun <R> mainLaunch(crossinline block: suspend CoroutineScope.() -> R) =
        viewModelScope.launch(dispatcherProvider.main) {
            block()
        }

    inline fun <R> mainImmediateLaunch(crossinline block: suspend CoroutineScope.() -> R) =
        viewModelScope.launch(dispatcherProvider.main.immediate) {
            block()
        }

    inline fun <R> computationLaunch(crossinline block: suspend CoroutineScope.() -> R) =
        viewModelScope.launch(dispatcherProvider.computation) {
            block()
        }

    suspend inline fun <R> mainContext(crossinline block: suspend CoroutineScope.() -> R): R =
        withContext(dispatcherProvider.main) {
            block()
        }

    suspend inline fun <R> computationContext(crossinline block: suspend CoroutineScope.() -> R): R =
        withContext(dispatcherProvider.computation) {
            block()
        }

    suspend inline fun <R> showProgress(
        @StringRes message: Int = 0,
        messageArgs: Array<Any>? = null,
        progress: Int = 0,
        maxProgress: Int = 0,
        requestId: Int = 0,
        crossinline block: suspend CoroutineScope.() -> R
    ): R = coroutineScope {

        this@BaseViewModel.progress.postValue(
            ProgressState.ShowProgress(
                requestId,
                message,
                messageArgs,
                progress,
                maxProgress
            )
        )

        try {
            return@coroutineScope block()
        } finally {
            this@BaseViewModel.progress.postValue(ProgressState.HideProgress)
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
            this@BaseViewModel.progress.postValue(
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
