package com.arcao.feedback.collector

import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.hardware.display.DisplayManager
import android.util.DisplayMetrics
import android.util.SparseArray
import android.view.Display
import android.view.Surface
import timber.log.Timber

class DisplayManagerCollector(private val context: Context) : Collector() {
    override val name: String
        get() = "DisplayManager"

    override suspend fun collect(): String {
        var displays: Array<Display>? = null
        val result = StringBuilder()

        // Since Android 4.2, we can fetch multiple displays with the
        // DisplayManager.
        try {
            val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
            displays = displayManager.displays
        } catch (e: IllegalArgumentException) {
            Timber.e(e, "Error while collecting DisplayManager data")
        }

        displays?.forEach { display ->
            result.append(collectDisplayData(display))
        }

        return result.toString()
    }

    @Suppress("DEPRECATION")
    private fun collectDisplayData(display: Display): Any {
        val metrics = DisplayMetrics()
        display.getMetrics(metrics)

        return collectCurrentSizeRange(display) +
            collectFlags(display) +
            display.displayId + ".height=" + display.height + '\n'.toString() +
            collectMetrics(display, "getMetrics") +
            collectName(display) +
            display.displayId + ".orientation=" + display.rotation + '\n'.toString() +
            collectMetrics(display, "getRealMetrics") +
            collectSize(display, "getRealSize") +
            collectRectSize(display) +
            display.displayId + ".refreshRate=" + display.refreshRate + '\n'.toString() +
            collectRotation(display) +
            collectSize(display, "getSize") +
            display.displayId + ".width=" + display.width + '\n'.toString() +
            collectIsValid(display)
    }

    private fun collectIsValid(display: Display): String {
        val result = StringBuilder()
        try {
            // since API v17
            val isValid = display.javaClass.getMethod("isValid")
            val value = isValid.invoke(display) as Boolean
            result.append(display.displayId).append(".isValid=").append(value).append('\n')
        } catch (e: Exception) {
            Timber.e(e)
        }

        return result.toString()
    }

    private fun collectRotation(display: Display): String {
        val result = StringBuilder()
        try {
            // since API v8
            when (val rotation = display.rotation) {
                Surface.ROTATION_0 -> result.append("ROTATION_0")
                Surface.ROTATION_90 -> result.append("ROTATION_90")
                Surface.ROTATION_180 -> result.append("ROTATION_180")
                Surface.ROTATION_270 -> result.append("ROTATION_270")
                else -> result.append(rotation)
            }
            result.append('\n')
        } catch (e: Exception) {
            Timber.e(e)
        }

        return result.toString()
    }

    private fun collectRectSize(display: Display): String {
        val result = StringBuilder()
        try {
            // since API v13
            val size = Rect()
            @Suppress("DEPRECATION")
            display.getRectSize(size)
            result.append(display.displayId).append(".rectSize=[").append(size.top).append(',').append(size.left)
                .append(',').append(size.width()).append(',').append(size.height()).append(']').append('\n')
        } catch (e: Exception) {
            Timber.e(e)
        }

        return result.toString()
    }

    private fun collectSize(display: Display, methodName: String): String {
        val result = StringBuilder()
        try {
            // since API v17
            val getRealSize = display.javaClass.getMethod(methodName, Point::class.java)
            val size = Point()
            getRealSize.invoke(display, size)
            result.append(display.displayId).append('.').append(methodName).append("=[").append(size.x)
                .append(',').append(size.y).append(']').append('\n')
        } catch (e: Exception) {
            Timber.e(e)
        }

        return result.toString()
    }

    private fun collectCurrentSizeRange(display: Display): String {
        val result = StringBuilder()
        try {
            // since API v16
            val getCurrentSizeRange =
                display.javaClass.getMethod("getCurrentSizeRange", Point::class.java, Point::class.java)
            val smallest = Point()
            val largest = Point()
            getCurrentSizeRange.invoke(display, smallest, largest)
            result.append(display.displayId).append(".currentSizeRange.smallest=[").append(smallest.x).append(',')
                .append(smallest.y).append(']').append('\n')
            result.append(display.displayId).append(".currentSizeRange.largest=[").append(largest.x).append(',')
                .append(largest.y).append(']').append('\n')
        } catch (e: Exception) {
            Timber.e(e)
        }

        return result.toString()
    }

    private fun collectFlags(display: Display): String {
        val result = StringBuilder()
        try {
            // since API v17
            val getFlags = display.javaClass.getMethod("getFlags")
            val flags = getFlags.invoke(display) as Int

            val flagsNames = SparseArray<String>()
            for (field in display.javaClass.fields) {
                if (field.name.startsWith("FLAG_")) {
                    flagsNames.put(field.getInt(null), field.name)
                }
            }

            result.append(display.displayId).append(".flags=").append(activeFlags(flagsNames, flags))
                .append('\n')
        } catch (e: Exception) {
            Timber.e(e)
        }

        return result.toString()
    }

    private fun collectName(display: Display): String {
        val result = StringBuilder()
        try {
            // since API v17
            val getName = display.javaClass.getMethod("getName")
            val name = getName.invoke(display) as String

            result.append(display.displayId).append(".name=").append(name).append('\n')
        } catch (e: Exception) {
            Timber.e(e)
        }

        return result.toString()
    }

    private fun collectMetrics(display: Display, methodName: String): String {
        val result = StringBuilder()
        try {
            val getMetrics = display.javaClass.getMethod(methodName)
            val metrics = getMetrics.invoke(display) as DisplayMetrics

            result.append(display.displayId).append('.').append(methodName).append(".density=")
                .append(metrics.density).append('\n')
            result.append(display.displayId).append('.').append(methodName).append(".densityDpi=")
                .append(metrics.javaClass.getField("densityDpi")).append('\n')
            result.append(display.displayId).append('.').append(methodName).append("scaledDensity=x")
                .append(metrics.scaledDensity).append('\n')
            result.append(display.displayId).append('.').append(methodName).append(".widthPixels=")
                .append(metrics.widthPixels).append('\n')
            result.append(display.displayId).append('.').append(methodName).append(".heightPixels=")
                .append(metrics.heightPixels).append('\n')
            result.append(display.displayId).append('.').append(methodName).append(".xdpi=").append(metrics.xdpi)
                .append('\n')
            result.append(display.displayId).append('.').append(methodName).append(".ydpi=").append(metrics.ydpi)
                .append('\n')
        } catch (e: Exception) {
            Timber.e(e)
        }

        return result.toString()
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
            val value = bitField and maskValue
            if (value > 0) {
                if (result.isNotEmpty()) {
                    result.append('+')
                }
                result.append(valueNames.get(value))
            }
        }
        return result.toString()
    }
}
