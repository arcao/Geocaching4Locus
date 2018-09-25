package com.arcao.feedback.collector

import java.io.PrintWriter
import java.io.StringWriter

abstract class Collector {
    abstract val name: String

    abstract fun collect(): String

    override fun toString(): String {
        return "--- $name ---\n${collect()}\n------\n\n"
    }

    protected fun throwableToString(t: Throwable): String {
        val sw = StringWriter()
        PrintWriter(sw).use {
            t.printStackTrace(it)
        }
        return sw.toString()
    }
}
