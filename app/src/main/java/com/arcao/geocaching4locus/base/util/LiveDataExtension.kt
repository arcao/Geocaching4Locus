package com.arcao.geocaching4locus.base.util

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

@MainThread
inline fun <T> LiveData<T>.withObserve(owner: LifecycleOwner, crossinline block: (T) -> Unit) =
    observe(owner, { block(it!!) })

@MainThread
operator fun MutableLiveData<Unit>.invoke() {
    value = Unit
}

@MainThread
inline operator fun <reified T> MutableLiveData<T>.invoke(t: T) {
    value = t
}

/**
 * One-shot command
 */
open class Command<T> : MutableLiveData<T>() {
    private val pending = AtomicBoolean(false)

    @MainThread
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        if (hasActiveObservers()) {
            Timber.e("${javaClass.simpleName}: Multiple observers registered but only one will be notified of changes.")
        }

        super.observe(owner) {
            if (pending.compareAndSet(true, false)) {
                observer.onChanged(it)
            }
        }
    }

    @MainThread
    override fun setValue(t: T) {
        pending.set(true)
        super.setValue(t)
    }

    override fun postValue(value: T) {
        pending.set(true)
        super.postValue(value)
    }
}
