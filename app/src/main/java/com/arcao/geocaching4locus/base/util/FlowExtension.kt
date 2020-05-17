package com.arcao.geocaching4locus.base.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

suspend fun <T> Flow<T>.takeListVariable(initialCount: Int, action: suspend (List<T>) -> Int) {
    if (initialCount <= 0) {
        return
    }

    var nextCount = initialCount
    val list = mutableListOf<T>()

    collect { value ->
        list.add(value)

        if (list.size >= nextCount) {
            nextCount = action(list)
            list.clear()
        }
    }

    if (list.isNotEmpty()) {
        action(list)
    }
}
