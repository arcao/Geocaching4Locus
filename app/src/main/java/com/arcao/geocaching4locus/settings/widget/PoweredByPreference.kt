package com.arcao.geocaching4locus.settings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.TypedArrayUtils
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import androidx.preference.R

class PoweredByPreference @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = TypedArrayUtils.getAttr(
                context,
                R.attr.preferenceStyle,
                android.R.attr.preferenceStyle
        )
) : Preference(context, attrs, defStyleAttr) {
    init {
        widgetLayoutResource = com.arcao.geocaching4locus.R.layout.preference_powered_by_widget
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        (holder.findViewById(android.R.id.icon) as? ImageView)?.let { imageView ->
            imageView.maxWidth = Integer.MAX_VALUE
            imageView.maxHeight = Integer.MAX_VALUE
        }

        (holder.findViewById(android.R.id.summary) as? TextView)?.let { textView ->
            textView.maxHeight = Integer.MAX_VALUE
        }

        holder.findViewById(android.R.id.title)?.let { view ->
            view.doOnLayout {
                holder.findViewById(android.R.id.widget_frame)?.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    topMargin = view.height
                }
            }
        }
    }
}
