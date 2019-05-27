package com.arcao.geocaching4locus.base.util

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.TypedValue
import androidx.appcompat.R
import androidx.core.graphics.ColorUtils
import kotlin.math.roundToInt

object ColorUtil {
    private val TL_TYPED_VALUE = ThreadLocal<TypedValue>()

    private val DISABLED_STATE_SET = intArrayOf(-android.R.attr.state_enabled)
    private val FOCUSED_STATE_SET = intArrayOf(android.R.attr.state_focused)
    private val ACTIVATED_STATE_SET = intArrayOf(android.R.attr.state_activated)
    private val PRESSED_STATE_SET = intArrayOf(android.R.attr.state_pressed)
    private val CHECKED_STATE_SET = intArrayOf(android.R.attr.state_checked)
    private val SELECTED_STATE_SET = intArrayOf(android.R.attr.state_selected)
    private val EMPTY_STATE_SET = IntArray(0)

    private val TEMP_ARRAY = IntArray(1)

    private var defaultColorStateList: ColorStateList? = null

    private val typedValue: TypedValue
        get() {
            var typedValue: TypedValue? = TL_TYPED_VALUE.get()
            if (typedValue == null) {
                typedValue = TypedValue()
                TL_TYPED_VALUE.set(typedValue)
            }
            return typedValue
        }

    fun getDefaultColorStateList(context: Context): ColorStateList {
        if (defaultColorStateList == null) {
            /*
              Generate the default color state list which uses the colorControl attributes.
              Order is important here. The default enabled state needs to go at the bottom.
             */

            val colorControlNormal = getThemeAttrColor(context, R.attr.colorControlNormal)
            val colorControlActivated = getThemeAttrColor(
                context,
                R.attr.colorControlActivated
            )

            val states = arrayOfNulls<IntArray>(7)
            val colors = IntArray(7)
            var i = 0

            // Disabled state
            states[i] = DISABLED_STATE_SET
            colors[i] = getDisabledThemeAttrColor(context, R.attr.colorControlNormal)
            i++

            states[i] = FOCUSED_STATE_SET
            colors[i] = colorControlActivated
            i++

            states[i] = ACTIVATED_STATE_SET
            colors[i] = colorControlActivated
            i++

            states[i] = PRESSED_STATE_SET
            colors[i] = colorControlActivated
            i++

            states[i] = CHECKED_STATE_SET
            colors[i] = colorControlActivated
            i++
            states[i] = SELECTED_STATE_SET
            colors[i] = colorControlActivated
            i++

            // Default enabled state
            states[i] = EMPTY_STATE_SET
            colors[i] = colorControlNormal

            defaultColorStateList = ColorStateList(states, colors)
        }
        return defaultColorStateList!!
    }

    private fun getDisabledThemeAttrColor(context: Context, attr: Int): Int {
        val csl = getThemeAttrColorStateList(context, attr)
        return if (csl != null && csl.isStateful) {
            // If the CSL is stateful, we'll assume it has a disabled state and use it
            csl.getColorForState(DISABLED_STATE_SET, csl.defaultColor)
        } else {
            // Else, we'll generate the color using disabledAlpha from the theme

            val tv = typedValue
            // Now retrieve the disabledAlpha value from the theme
            context.theme.resolveAttribute(android.R.attr.disabledAlpha, tv, true)
            val disabledAlpha = tv.float

            getThemeAttrColor(context, attr, disabledAlpha)
        }
    }

    private fun getThemeAttrColorStateList(context: Context, attr: Int): ColorStateList? {
        TEMP_ARRAY[0] = attr
        val a = context.obtainStyledAttributes(null, TEMP_ARRAY)

        try {
            return a.getColorStateList(0)
        } finally {
            a.recycle()
        }
    }

    private fun getThemeAttrColor(context: Context, attr: Int, alpha: Float): Int {
        val color = getThemeAttrColor(context, attr)
        val originalAlpha = Color.alpha(color)
        return ColorUtils.setAlphaComponent(color, (originalAlpha * alpha).roundToInt())
    }

    private fun getThemeAttrColor(context: Context, attr: Int): Int {
        TEMP_ARRAY[0] = attr
        val a = context.obtainStyledAttributes(null, TEMP_ARRAY)

        try {
            return a.getColor(0, 0)
        } finally {
            a.recycle()
        }
    }
}
