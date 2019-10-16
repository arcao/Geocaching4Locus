package com.arcao.geocaching4locus.settings.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import androidx.core.content.res.TypedArrayUtils
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import androidx.preference.R

class UnlimitedIconSizePreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = TypedArrayUtils.getAttr(
        context,
        R.attr.preferenceStyle,
        android.R.attr.preferenceStyle
    )
) : Preference(context, attrs, defStyleAttr) {
    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        (holder.findViewById(android.R.id.icon) as? ImageView)?.let { imageView ->
            imageView.maxWidth = 0
            imageView.maxHeight = 0
        }
    }
}
