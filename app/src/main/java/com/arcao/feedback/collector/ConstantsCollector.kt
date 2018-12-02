package com.arcao.feedback.collector

class ConstantsCollector(private val source: Class<*>, private val prefix: String) : Collector() {

    override val name: String
        get() = "$prefix CONSTANTS"

    override suspend fun collect(): String {
        val result = StringBuilder()

        source.fields.forEach { field ->
            result.append(field.name).append("=")
            try {
                result.append(field.get(null).toString())
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
