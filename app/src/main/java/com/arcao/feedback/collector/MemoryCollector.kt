package com.arcao.feedback.collector

import android.os.Process
import timber.log.Timber
import java.io.IOException

class MemoryCollector : Collector() {
    override val name: String
        get() = "MEMORY"

    override fun collect(): String {
        try {
            val commandLine = arrayOf("dumpsys", "meminfo", Process.myPid().toString())
            val process = Runtime.getRuntime().exec(commandLine)
            process.inputStream.bufferedReader().use { reader ->
                return reader.readText()
            }
        } catch (e: IOException) {
            Timber.e(e, "MemoryCollector could not retrieve data")
            return "Error: " + throwableToString(e)
        }
    }
}
