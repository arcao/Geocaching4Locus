package com.arcao.feedback.collector

import java.util.Arrays

class ConstantsCollector(private val source: Class<*>, private val prefix: String) : Collector() {

    override val name: String
        get() = "$prefix CONSTANTS"

    override suspend fun collect(): String {
        val result = StringBuilder()

        source.fields.forEach { field ->
            result.append(field.name).append("=")
            try {
                result.append(
                    when (val value = field.get(null)) {
                        is Array<*> -> Arrays.toString(value)
                        else -> value.toString()
                    }
                )
            } catch (e: IllegalArgumentException) {
                result.append("N/A")
            } catch (e: IllegalAccessException) {
                result.append("N/A")
            }
            result.append("\n")
        }

        return result.toString()
    }
}
