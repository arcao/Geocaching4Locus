package com.arcao.geocaching4locus.settings.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.preference.Preference
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import androidx.core.content.withStyledAttributes
import com.arcao.geocaching4locus.R

class IconPreference @JvmOverloads constructor(context: Context, attrs: AttributeSet, defStyle: Int = 0) : Preference(context, attrs, defStyle) {
    private var iconDrawable: Drawable? = null

    init {
        layoutResource = R.layout.preference_icon

        context.withStyledAttributes(attrs, R.styleable.IconPreference, defStyle, 0) {
            iconDrawable = getDrawable(R.styleable.IconPreference_icon)
        }
    }

    public override fun onBindView(view: View) {
        super.onBindView(view)

        view.findViewById<ImageView>(R.id.icon)?.apply {
            if (iconDrawable != null) {
                setImageDrawable(iconDrawable)
            }
        }
    }

    override fun setIcon(icon: Drawable?) {
        if (icon == null && iconDrawable != null || icon != null && icon != iconDrawable) {
            iconDrawable = icon
            notifyChanged()
        }
    }

    override fun getIcon(): Drawable? {
        return iconDrawable
    }
}