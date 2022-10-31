package com.arcao.feedback.collector

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import timber.log.Timber
import java.io.IOException

class LogCatCollector : Collector() {
    override val name: String
        get() = "LOGCAT"

    @Suppress("BlockingMethodInNonBlockingContext", "ControlFlowWithEmptyBody")
    override suspend fun collect(): String = coroutineScope {
        try {
            val process = Runtime.getRuntime().exec(COMMAND_LINE)
            val errors = async {
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE_IN_BYTES)

                try {
                    process.errorStream.use { stderr ->
                        while (stderr.read(buffer) >= 0); // discard all data
                    }
                } catch (ignored: IOException) {
                    // fall trough
                }
            }
            val logcat = process.inputStream.bufferedReader().use { reader ->
                Timber.d("Retrieving logcat output...")
                reader.readText()
            }
            errors.await()
            logcat
        } catch (t: Exception) {
            Timber.e(t, "LogCatCollector could not retrieve data.")
            "Error: " + throwableToString(t)
        }
    }

    companion object {
        private const val DEFAULT_BUFFER_SIZE_IN_BYTES = 8192
        private val COMMAND_LINE = arrayOf("logcat", "-t", "10000", "-v", "time")
    }
}
