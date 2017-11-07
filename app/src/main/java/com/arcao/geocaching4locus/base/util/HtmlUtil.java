package com.arcao.geocaching4locus.base.util;

import android.os.Build;
import android.support.annotation.NonNull;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.MetricAffectingSpan;

public class HtmlUtil {
    public static CharSequence fromHtml(@NonNull String source) {
        return applyFix(Html.fromHtml(source));
    }

    public static String toHtml(@NonNull CharSequence source) {
        return source instanceof Spanned ? Html.toHtml((Spanned) source) : source.toString();
    }

    public static CharSequence applyFix(@NonNull CharSequence source) {
        if (source instanceof Spanned)
            return applyFix((Spanned) source);

        return source;
    }

    private static Spanned applyFix(@NonNull Spanned spanned) {
        // this will fix crash only on API v16, other API versions are safe
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.JELLY_BEAN)
            return spanned;

        // Idea from: http://code.google.com/p/android/issues/detail?id=35466#c25
        // 3.  Find all the MetricAffectingSpans and detect whether each span has a space/tab/newline before
        // and after them.  If not, then insert a space before and/or after each span.
        SpannableStringBuilder builder = new SpannableStringBuilder(spanned);

        MetricAffectingSpan[] spans = builder.getSpans(0, builder.length(), MetricAffectingSpan.class);
        for (MetricAffectingSpan span : spans) {
            int spanStart = builder.getSpanStart(span);
            if (spanStart > 0 && isNotSpace(builder, spanStart - 1)) {
                builder.insert(spanStart, " ");
            }

            int spanEnd = builder.getSpanEnd(span);
            if (spanEnd >= builder.length() || isNotSpace(builder, spanEnd)) {
                builder.insert(spanEnd, " ");
            }
        }

        return builder;
    }

    private static boolean isNotSpace(@NonNull CharSequence text, int where) {
        char ch = text.charAt(where);
        return ch != ' ' && ch != '\n' && ch != '\t';
    }
}
