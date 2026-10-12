package com.arcao.geocaching4locus.base.util

import android.text.Spanned
import androidx.core.text.HtmlCompat

object HtmlUtil {
    fun fromHtml(source: String): CharSequence {
        return HtmlCompat.fromHtml(source, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    fun toHtml(source: CharSequence): String {
        return if (source is Spanned) HtmlCompat.toHtml(
            source,
            HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE
        ) else source.toString()
    }
}
