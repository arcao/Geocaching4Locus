package com.arcao.geocaching4locus.base.paging

import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState

fun CombinedLoadStates.handleErrors(callback: (Throwable) -> Unit) {
    val state = source.append as? LoadState.Error
        ?: source.prepend as? LoadState.Error
        ?: source.refresh as? LoadState.Error
        ?: append as? LoadState.Error
        ?: prepend as? LoadState.Error
        ?: refresh as? LoadState.Error

    if (state != null) {
        callback(state.error)
    }
}

