package com.arcao.geocaching4locus.base.coroutine

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

data class CoroutinesDispatcherProvider(
        val main: CoroutineDispatcher = Dispatchers.Main,
        val computation: CoroutineDispatcher = Dispatchers.Default,
        val io: CoroutineDispatcher = Dispatchers.IO
)