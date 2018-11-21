package com.arcao.geocaching4locus.base

import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.util.invoke
import com.arcao.geocaching4locus.base.util.runIfIsSuspended
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

abstract class BaseViewModel(
        val dispatcherProvider: CoroutinesDispatcherProvider
) : ViewModel(), CoroutineScope {
    val job = Job()
    val progress: MutableLiveData<ProgressState> = MutableLiveData()

    override val coroutineContext: CoroutineContext
        get() = dispatcherProvider.computation + job

    override fun onCleared() {
        job.cancel()
        super.onCleared()
    }

    suspend fun <T : BaseViewModel, R> T.mainContext(block: suspend T.() -> R): R =
            withContext(dispatcherProvider.main) {
                this@mainContext.block()
            }

    suspend fun <T : BaseViewModel, R> T.showProgress(@StringRes message: Int = 0, messageArgs: Array<Any>? = null, progress: Int = 0, maxProgress: Int = 0, block: suspend T.() -> R) : R {
        mainContext {
            progress(ProgressState.ShowProgress(message, messageArgs, progress, maxProgress))
        }

        try {
            return this.block()
        } finally {
            mainContext {
                progress(ProgressState.HideProgress)
            }
        }
    }

    suspend fun <T : BaseViewModel> T.updateProgress(@StringRes message: Int = 0, messageArgs: Array<Any>? = null, progress: Int = 0) {
        this.progress.value.runIfIsSuspended(ProgressState.ShowProgress::class) {
            mainContext {
                this@updateProgress.progress(ProgressState.ShowProgress(
                        if (message == 0) {
                            this@runIfIsSuspended.message
                        } else {
                            message
                        },
                        messageArgs ?: this@runIfIsSuspended.messageArgs,
                        progress,
                        maxProgress
                ))
            }
        }
    }
}

sealed class ProgressState {
    class ShowProgress(@StringRes val message: Int, val messageArgs: Array<Any>?, val progress: Int, val maxProgress: Int) : ProgressState()
    object HideProgress : ProgressState()
}
