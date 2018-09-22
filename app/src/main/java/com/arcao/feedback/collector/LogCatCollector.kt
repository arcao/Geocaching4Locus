package com.arcao.feedback.collector

import timber.log.Timber
import java.io.IOException

class LogCatCollector : Collector() {
    override val name: String
        get() = "LOGCAT"

    override fun collect(): String {
        val buffer = StringBuilder()

        try {
            val process = Runtime.getRuntime().exec(COMMAND_LINE)
            process.inputStream.bufferedReader().use { reader ->
                Timber.d("Retrieving logcat output...")
                // Dump stderr to null
                Thread {
                    try {
                        process.errorStream.use { stderr ->
                            val dummy = ByteArray(DEFAULT_BUFFER_SIZE_IN_BYTES)
                            while (stderr.read(dummy) >= 0); // discard all data
                        }
                    } catch (e: IOException) {
                        // fall trough
                    }
                }.start()

                return reader.readText()
            }
        } catch (t: Exception) {
            Timber.e(t, "LogCatCollector could not retrieve data.")
            return "Error: " + throwableToString(t)
        }
    }

    companion object {
        private const val DEFAULT_BUFFER_SIZE_IN_BYTES = 8192
        private val COMMAND_LINE = arrayOf("logcat", "-t", "10000", "-v", "time")
    }
}
