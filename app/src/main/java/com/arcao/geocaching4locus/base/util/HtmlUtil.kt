package com.arcao.geocaching4locus.base.util

import android.os.Build
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.MetricAffectingSpan
import androidx.annotation.NonNull
import androidx.core.text.HtmlCompat

object HtmlUtil {
    @JvmStatic
    fun fromHtml(@NonNull source: String): CharSequence {
        return applyFix(HtmlCompat.fromHtml(source, HtmlCompat.FROM_HTML_MODE_LEGACY))
    }

    @JvmStatic
    fun toHtml(@NonNull source: CharSequence): String {
        return if (source is Spanned) HtmlCompat.toHtml(
            source,
            HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE
        ) else source.toString()
    }

    @JvmStatic
    fun applyFix(@NonNull source: CharSequence): CharSequence {
        return if (source is Spanned) applyFix(source) else source
    }

    private fun applyFix(@NonNull spanned: Spanned): Spanned {
        // this will fix crash only on API v16, other API versions are safe
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.JELLY_BEAN)
            return spanned

        // Idea from: https://issuetracker.google.com/issues/36952022#comment24, point 3.
        // Find all the MetricAffectingSpans and detect whether each span has a space/tab/newline before
        // and after them.  If not, then insert a space before and/or after each span.
        val builder = SpannableStringBuilder(spanned)

        val spans = builder.getSpans(0, builder.length, MetricAffectingSpan::class.java)
        for (span in spans) {
            val spanStart = builder.getSpanStart(span)
            if (spanStart > 0 && isNotSpace(builder, spanStart - 1)) {
                builder.insert(spanStart, " ")
            }

            val spanEnd = builder.getSpanEnd(span)
            if (spanEnd >= builder.length || isNotSpace(builder, spanEnd)) {
                builder.insert(spanEnd, " ")
            }
        }

        return builder
    }

    private fun isNotSpace(@NonNull text: CharSequence, where: Int): Boolean {
        val ch = text[where]
        return ch != ' ' && ch != '\n' && ch != '\t'
    }
}
