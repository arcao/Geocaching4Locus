package com.arcao.geocaching4locus.search_nearest.widget

import android.content.Context
import android.text.method.MovementMethod
import android.util.AttributeSet
import androidx.appcompat.R
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.ViewCompat
import com.arcao.geocaching4locus.base.util.ColorUtil

class SpinnerTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

    init {
        ViewCompat.setBackgroundTintList(this, ColorUtil.getDefaultColorStateList(context))
    }

    override fun getDefaultEditable(): Boolean {
        return false
    }

    override fun getDefaultMovementMethod(): MovementMethod? {
        return null
    }
}
