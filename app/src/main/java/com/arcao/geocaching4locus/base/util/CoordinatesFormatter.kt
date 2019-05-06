package com.arcao.geocaching4locus.base.util

import kotlin.math.pow

object CoordinatesFormatter {
    private const val MIN_PER_DEG = 60.0
    private const val SEC_PER_DEG = 3600.0

    private val COMMA_REGEX = Regex.fromLiteral(",")

    fun convertDoubleToDeg(source: Double, isLon: Boolean, precision: Int = 3): CharSequence {
        var value = source
        if (value.isNaN())
            return ""

        val sb = StringBuilder()
        if (value < 0) {
            sb.append(if (!isLon) 'S' else 'W')
            value = -value
        } else {
            sb.append(if (!isLon) 'N' else 'E')
        }
        sb.append(' ')

        var deg = value.toInt()

        // FIX for rounding errors
        var min = roundDouble((value - deg) * MIN_PER_DEG, precision)
        if (min == MIN_PER_DEG) {
            deg++
            min = 0.0
        }

        sb.append(deg)
        sb.append("\u00B0 ")
        sb.append(round(min, precision))

        return sb.toString()
    }

    fun convertDegToDouble(source: CharSequence): Double {
        val tmp = source.trim { it <= ' ' }.replace(COMMA_REGEX, ".")

        var index = 0
        var end: Int

        val ch: Char

        val deg: Double
        var min = 0.0
        var sec = 0.0

        var direction = 1.0

        try {
            ch = tmp[index].toUpperCase()
            if (ch == 'S' || ch == 'W' || ch == '-') {
                direction = -1.0
                index++
            }
            if (ch == 'N' || ch == 'E' || ch == '+')
                index++

            while (!tmp[index].isDigit()) index++
            end = getDoubleNumberEnd(tmp, index)
            deg = tmp.substring(index, end).toDouble()
            index = end

            while (index < tmp.length && !tmp[index].isDigit()) index++
            if (index < tmp.length) {
                end = getDoubleNumberEnd(tmp, index)
                min = tmp.substring(index, end).toDouble()
                index = end

                while (index < tmp.length && !tmp[index].isDigit()) index++
                if (index < tmp.length) {
                    end = getDoubleNumberEnd(tmp, index)
                    sec = tmp.substring(index, end).toDouble()
                }
            }

            return direction * (deg + min / MIN_PER_DEG + sec / SEC_PER_DEG)
        } catch (e: Exception) {
            return Double.NaN
        }
    }

    private fun getDoubleNumberEnd(source: CharSequence, start: Int): Int {
        for (i in start until source.length) {
            if (!source[i].isDigit() && source[i] != '.') {
                return i
            }
        }
        return source.length
    }

    private fun roundDouble(source: Double, decimalPlaces: Int): Double {
        val multiplicationFactor = 10.0.pow(decimalPlaces.toDouble())
        val sourceMultiplied = source * multiplicationFactor
        return Math.round(sourceMultiplied) / multiplicationFactor
    }

    private fun round(source: Double, decimals: Int): String {
        if (decimals < 0)
            throw IllegalArgumentException("decimals must be great or equal to zero")

        if (decimals == 0) {
            return source.toLong().toString()
        }

        val rounded = roundDouble(source, decimals)

        val value = rounded.toString()
        val dot = value.indexOf('.')
        if (dot == -1) {
            val sb = StringBuilder(value)
            sb.append('.')
            for (i in 0 until decimals)
                sb.append('0')
            return sb.toString()
        } else {
            if (value.length - (dot + decimals) > 0) {
                return value.substring(0, dot + decimals + 1)
            }
            val sb = StringBuilder(value)
            for (i in value.length..dot + decimals)
                sb.append('0')
            return sb.toString()
        }
    }
}
