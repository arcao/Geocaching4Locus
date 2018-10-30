package com.arcao.geocaching4locus.dashboard.widget

import android.annotation.TargetApi
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.os.Build
import android.util.AttributeSet
import android.widget.ToggleButton
import androidx.annotation.StyleableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import com.arcao.geocaching4locus.R

class DashboardButton : ToggleButton {
    private val toggleable: Boolean

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.DashboardButton, defStyleAttr, defStyleRes)
        try {
            toggleable = a.getBoolean(R.styleable.DashboardButton_toggleable, false)

            applyCompoundDrawableTint(a)
            applyTextColorStateList(a)
        } finally {
            a.recycle()
        }
    }

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.DashboardButton, defStyleAttr, R.style.Widget_AppTheme_DashboardButton)
        try {
            toggleable = a.getBoolean(R.styleable.DashboardButton_toggleable, false)

            applyCompoundDrawableTint(a)
            applyTextColorStateList(a)
        } finally {
            a.recycle()
        }
    }

    private fun applyTextColorStateList(a: TypedArray) {
        // support for alpha attribute in ColorStateList
        getCompatColorStateList(context, a, R.styleable.DashboardButton_android_textColor)?.let {
            setTextColor(it)
        }
    }

    private fun applyCompoundDrawableTint(a: TypedArray) {
        // support for alpha attribute in ColorStateList
        getCompatColorStateList(context, a, R.styleable.DashboardButton_compoundDrawableTint)?.let { colorList ->
            val compoundDrawables = compoundDrawables
            for (i in 0..3) {
                if (compoundDrawables[i] == null)
                    continue

                compoundDrawables[i] = DrawableCompat.wrap(compoundDrawables[i])
                DrawableCompat.setTintList(compoundDrawables[i], colorList)
            }
            setCompoundDrawables(compoundDrawables[0], compoundDrawables[1], compoundDrawables[2], compoundDrawables[3])
        }
    }

    override fun toggle() {
        if (toggleable) super.toggle()
    }

    override fun setChecked(checked: Boolean) {
        if (!toggleable) return

        super.setChecked(checked)
    }

    private fun getCompatColorStateList(context: Context, a: TypedArray, @StyleableRes index: Int): ColorStateList? {
        val resourceId = a.getResourceId(index, 0)
        return if (resourceId == 0) null else AppCompatResources.getColorStateList(context, resourceId)
    }
}
