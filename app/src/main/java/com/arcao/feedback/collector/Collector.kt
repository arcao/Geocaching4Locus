package com.arcao.feedback.collector

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.PrintWriter
import java.io.StringWriter

abstract class Collector {
    abstract val name: String

    abstract suspend fun collect(): String

    suspend operator fun invoke(): String = withContext(Dispatchers.IO) {
        "--- $name ---\n${collect()}\n------\n\n"
    }

    protected fun throwableToString(t: Throwable): String {
        val sw = StringWriter()
        PrintWriter(sw).use {
            t.printStackTrace(it)
        }
        return sw.toString()
    }
}
