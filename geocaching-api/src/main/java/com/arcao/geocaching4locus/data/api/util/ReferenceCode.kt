package com.arcao.geocaching4locus.data.api.util

import java.util.Locale

/**
 * Helper functions to convert string reference code to numeric reference id and vice versa.
 */
object ReferenceCode {
    private const val BASE_31_CHARS = "0123456789ABCDEFGHJKMNPQRTVWXYZ"

    // = (16 * 31 * 31 * 31) - (16 * 16 * 16 * 16)
    private const val REFERENCE_CODE_BASE31_MAGIC_NUMBER: Long = 411120
    const val GEOCACHE_PREFIX = "GC"
    private const val REFERENCE_CODE_BASE16_MAX: Long = 0xFFFF
    private const val BASE_31 = 31
    private const val BASE_16 = 16

    /**
     * Convert a base 31 number containing chars 0123456789ABCDEFGHJKMNPQRTVWXYZ
     * to numeric value.
     *
     * @param input base 31 number
     * @return numeric value
     * @throws IllegalArgumentException If input contains illegal chars
     */
    fun base31Decode(input: String): Long {
        var result: Long = 0

        for (ch in input.toCharArray()) {
            result *= BASE_31

            val index = BASE_31_CHARS.indexOf(ch, ignoreCase = true)
            if (index == -1) {
                throw IllegalArgumentException("Only chars $BASE_31_CHARS are supported.")
            }

            result += index.toLong()
        }
        return result
    }

    /**
     * Convert a numeric value to base 31 number using chars
     * 0123456789ABCDEFGHJKMNPQRTVWXYZ.
     *
     * @param input numeric value
     * @return base 31 number
     */
    fun base31Encode(input: Long): String {
        var temp = input
        val sb = StringBuilder()

        while (temp != 0L) {
            sb.append(BASE_31_CHARS[(temp % BASE_31).toInt()])
            temp /= BASE_31
        }

        return sb.reverse().toString()
    }

    /**
     * Convert reference code `ppXXX` to numeric reference id.
     *
     * The algorithm respects following rules used for reference code:
     *
     *  * `pp0 - ppFFFF` - value after `pp` prefix is a hexadecimal number
     *  * `ppG000 - ...` = value after `pp` is a base 31 number minus magic constant
     *    `411120 = (16 * 31 * 31 * 31 - 16 * 16 * 16 * 16)`
     *
     * @param referenceCode cache code including GC prefix
     * @return reference id
     * @throws IllegalArgumentException reference code contains invalid characters
     */
    fun toId(referenceCode: String): Long {
        val referenceCodeNorm = referenceCode.uppercase(Locale.US)

        if (referenceCodeNorm.length < 3) {
            throw IllegalArgumentException("Reference code is too short.")
        }

        // remove prefix
        val code = referenceCodeNorm.substring(2)

        // 0 - FFFF = base16; G000 - ... = base 31
        return if (code.length <= 4 && code[0] < 'G') {
            try {
                code.toLong(BASE_16)
            } catch (e: NumberFormatException) {
                throw IllegalArgumentException("Only chars $BASE_31_CHARS are supported.")
            }
        } else {
            base31Decode(code) - REFERENCE_CODE_BASE31_MAGIC_NUMBER
        }
    }

    /**
     * Convert a numeric id to reference code `ppXXX`. The algorithm respects
     * rules for generating reference code.
     *
     * @param prefix the reference code prefix `pp`
     * @param id reference id
     * @return reference code including prefix
     * @see .toId
     */
    fun toReferenceCode(prefix: String = GEOCACHE_PREFIX, id: Long): String {
        val sb = StringBuilder()

        // append GC prefix
        sb.append(prefix)

        if (id <= REFERENCE_CODE_BASE16_MAX) { // 0 - FFFF
            sb.append(id.toString(BASE_16).uppercase(Locale.US))
        } else { // G000 - ...
            sb.append(base31Encode(id + REFERENCE_CODE_BASE31_MAGIC_NUMBER))
        }

        return sb.toString()
    }

    /**
     * Returns true if the reference code is valid. The algorithm respects
     * rules for reference code.
     *
     * @param referenceCode reference code
     * @return true if reference code is valid, otherwise false
     * @see .toId
     */
    fun isReferenceCodeValid(referenceCode: String, prefix: String? = null): Boolean {
        try {
            if (prefix != null && !referenceCode.startsWith(prefix, ignoreCase = true))
                return false

            return toId(referenceCode) >= 0
        } catch (e: IllegalArgumentException) {
            return false
        }
    }
}
