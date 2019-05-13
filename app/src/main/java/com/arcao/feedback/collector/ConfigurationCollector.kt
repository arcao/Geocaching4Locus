package com.arcao.feedback.collector

import android.content.Context
import android.content.res.Configuration
import android.util.SparseArray
import timber.log.Timber
import java.lang.reflect.Field
import java.lang.reflect.Modifier

class ConfigurationCollector(private val context: Context) : Collector() {
    override val name: String
        get() = "CONFIGURATION"

    override suspend fun collect(): String {
        return try {
            val crashConf = context.resources.configuration
            ConfigurationCollector.toString(crashConf)
        } catch (e: RuntimeException) {
            Timber.w(e, "Couldn't retrieve ConfigurationCollector for : %s", context.packageName)
            "Couldn't retrieve crash config"
        }
    }

    companion object {
        private const val SUFFIX_MASK = "_MASK"
        private const val FIELD_SCREENLAYOUT = "screenLayout"
        private const val FIELD_UIMODE = "uiMode"
        private const val FIELD_MNC = "mnc"
        private const val FIELD_MCC = "mcc"
        private const val PREFIX_UI_MODE = "UI_MODE_"
        private const val PREFIX_TOUCHSCREEN = "TOUCHSCREEN_"
        private const val PREFIX_SCREENLAYOUT = "SCREENLAYOUT_"
        private const val PREFIX_ORIENTATION = "ORIENTATION_"
        private const val PREFIX_NAVIGATIONHIDDEN = "NAVIGATIONHIDDEN_"
        private const val PREFIX_NAVIGATION = "NAVIGATION_"
        private const val PREFIX_KEYBOARDHIDDEN = "KEYBOARDHIDDEN_"
        private const val PREFIX_KEYBOARD = "KEYBOARD_"
        private const val PREFIX_HARDKEYBOARDHIDDEN = "HARDKEYBOARDHIDDEN_"

        private val HARD_KEYBOARD_HIDDEN_VALUES = SparseArray<String>()
        private val KEYBOARD_VALUES = SparseArray<String>()
        private val KEYBOARD_HIDDEN_VALUES = SparseArray<String>()
        private val NAVIGATION_VALUES = SparseArray<String>()
        private val NAVIGATION_HIDDEN_VALUES = SparseArray<String>()
        private val ORIENTATION_VALUES = SparseArray<String>()
        private val SCREEN_LAYOUT_VALUES = SparseArray<String>()
        private val TOUCH_SCREEN_VALUES = SparseArray<String>()
        private val UI_MODE_VALUES = SparseArray<String>()

        private val VALUE_ARRAYS = HashMap<String, SparseArray<String>>()

        // Static init
        init {
            VALUE_ARRAYS[PREFIX_HARDKEYBOARDHIDDEN] = HARD_KEYBOARD_HIDDEN_VALUES
            VALUE_ARRAYS[PREFIX_KEYBOARD] = KEYBOARD_VALUES
            VALUE_ARRAYS[PREFIX_KEYBOARDHIDDEN] = KEYBOARD_HIDDEN_VALUES
            VALUE_ARRAYS[PREFIX_NAVIGATION] = NAVIGATION_VALUES
            VALUE_ARRAYS[PREFIX_NAVIGATIONHIDDEN] = NAVIGATION_HIDDEN_VALUES
            VALUE_ARRAYS[PREFIX_ORIENTATION] = ORIENTATION_VALUES
            VALUE_ARRAYS[PREFIX_SCREENLAYOUT] = SCREEN_LAYOUT_VALUES
            VALUE_ARRAYS[PREFIX_TOUCHSCREEN] = TOUCH_SCREEN_VALUES
            VALUE_ARRAYS[PREFIX_UI_MODE] = UI_MODE_VALUES

            for (f in Configuration::class.java.fields) {
                if (Modifier.isStatic(f.modifiers) && Modifier.isFinal(f.modifiers)) {
                    val fieldName = f.name
                    try {
                        when {
                            fieldName.startsWith(PREFIX_HARDKEYBOARDHIDDEN) -> HARD_KEYBOARD_HIDDEN_VALUES.put(
                                f.getInt(
                                    null
                                ), fieldName
                            )
                            fieldName.startsWith(PREFIX_KEYBOARD) -> KEYBOARD_VALUES.put(f.getInt(null), fieldName)
                            fieldName.startsWith(PREFIX_KEYBOARDHIDDEN) -> KEYBOARD_HIDDEN_VALUES.put(
                                f.getInt(null),
                                fieldName
                            )
                            fieldName.startsWith(PREFIX_NAVIGATION) -> NAVIGATION_VALUES.put(f.getInt(null), fieldName)
                            fieldName.startsWith(PREFIX_NAVIGATIONHIDDEN) -> NAVIGATION_HIDDEN_VALUES.put(
                                f.getInt(null),
                                fieldName
                            )
                            fieldName.startsWith(PREFIX_ORIENTATION) -> ORIENTATION_VALUES.put(
                                f.getInt(null),
                                fieldName
                            )
                            fieldName.startsWith(PREFIX_SCREENLAYOUT) -> SCREEN_LAYOUT_VALUES.put(
                                f.getInt(null),
                                fieldName
                            )
                            fieldName.startsWith(PREFIX_TOUCHSCREEN) -> TOUCH_SCREEN_VALUES.put(
                                f.getInt(null),
                                fieldName
                            )
                            fieldName.startsWith(PREFIX_UI_MODE) -> UI_MODE_VALUES.put(f.getInt(null), fieldName)
                        }
                    } catch (e: IllegalArgumentException) {
                        Timber.w(e, "Error while inspecting device configuration: ")
                    } catch (e: IllegalAccessException) {
                        Timber.w(e, "Error while inspecting device configuration: ")
                    }
                }
            }
        }

        /**
         * Use this method to generate a human readable String listing all values
         * from the provided Configuration instance.
         *
         * @param conf The Configuration to be described.
         * @return A String describing all the fields of the given Configuration,
         * with values replaced by constant names.
         */
        private fun toString(conf: Configuration): String {
            val result = StringBuilder()
            for (f in conf.javaClass.fields) {
                try {
                    if (!Modifier.isStatic(f.modifiers)) {
                        val fieldName = f.name
                        result.append(fieldName).append('=')
                        if (f.type == Int::class.javaPrimitiveType) {
                            result.append(getFieldValueName(conf, f))
                        } else if (f.get(conf) != null) {
                            result.append(f.get(conf).toString())
                        }
                        result.append('\n')
                    }
                } catch (e: IllegalArgumentException) {
                    Timber.e(e, "Error while inspecting device configuration: ")
                } catch (e: IllegalAccessException) {
                    Timber.e(e, "Error while inspecting device configuration: ")
                }
            }
            return result.toString()
        }

        /**
         * Retrieve the name of the constant defined in the [Configuration]
         * class which defines the value of a field in a [Configuration]
         * instance.
         *
         * @param conf The instance of [Configuration] where the value is
         * stored.
         * @param f The [Field] to be inspected in the [Configuration]
         * instance.
         * @return The value of the field f in instance conf translated to its
         * constant name.
         * @throws IllegalAccessException if the supplied field is inaccessible.
         */
        @Throws(IllegalAccessException::class)
        private fun getFieldValueName(conf: Configuration, f: Field): String? {
            val fieldName = f.name
            when (fieldName) {
                FIELD_MCC, FIELD_MNC -> return Integer.toString(f.getInt(conf))
                FIELD_UIMODE -> return activeFlags(VALUE_ARRAYS[PREFIX_UI_MODE]!!, f.getInt(conf))
                FIELD_SCREENLAYOUT -> return activeFlags(VALUE_ARRAYS[PREFIX_SCREENLAYOUT]!!, f.getInt(conf))
                else -> {
                    val values =
                        VALUE_ARRAYS[fieldName.toUpperCase() + '_'] // Unknown field, return the raw int as String
                            ?: return Integer.toString(f.getInt(conf))

                    return values.get(f.getInt(conf)) // Unknown value, return the raw int as String
                        ?: return Integer.toString(f.getInt(conf))
                }
            }
        }

        /**
         * Some fields contain multiple value types which can be isolated by
         * applying a bitmask. That method returns the concatenation of active
         * values.
         *
         * @param valueNames The array containing the different values and names for this
         * field. Must contain mask values too.
         * @param bitField The bit field to inspect.
         * @return The names of the different values contained in the bitField,
         * separated by '+'.
         */
        private fun activeFlags(valueNames: SparseArray<String>, bitField: Int): String {
            val result = StringBuilder()

            // Look for masks, apply it an retrieve the masked value
            for (i in 0 until valueNames.size()) {
                val maskValue = valueNames.keyAt(i)
                if (valueNames.get(maskValue).endsWith(SUFFIX_MASK)) {
                    val value = bitField and maskValue
                    if (value > 0) {
                        if (result.isNotEmpty()) {
                            result.append('+')
                        }
                        result.append(valueNames.get(value))
                    }
                }
            }
            return result.toString()
        }
    }
}
