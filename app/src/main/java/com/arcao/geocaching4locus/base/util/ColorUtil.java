package com.arcao.geocaching4locus.base.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.appcompat.R;
import android.util.TypedValue;

public class ColorUtil {
    private static final ThreadLocal<TypedValue> TL_TYPED_VALUE = new ThreadLocal<>();

    private static final int[] DISABLED_STATE_SET = {-android.R.attr.state_enabled};
    private static final int[] FOCUSED_STATE_SET = {android.R.attr.state_focused};
    private static final int[] ACTIVATED_STATE_SET = {android.R.attr.state_activated};
    private static final int[] PRESSED_STATE_SET = {android.R.attr.state_pressed};
    private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};
    private static final int[] SELECTED_STATE_SET = {android.R.attr.state_selected};
    private static final int[] EMPTY_STATE_SET = new int[0];

    private static final int[] TEMP_ARRAY = new int[1];

    private static ColorStateList defaultColorStateList;

    public static ColorStateList getDefaultColorStateList(Context context) {
        if (defaultColorStateList == null) {
            /*
              Generate the default color state list which uses the colorControl attributes.
              Order is important here. The default enabled state needs to go at the bottom.
             */

            final int colorControlNormal = getThemeAttrColor(context, R.attr.colorControlNormal);
            final int colorControlActivated = getThemeAttrColor(context,
                    R.attr.colorControlActivated);

            final int[][] states = new int[7][];
            final int[] colors = new int[7];
            int i = 0;

            // Disabled state
            states[i] = DISABLED_STATE_SET;
            colors[i] = getDisabledThemeAttrColor(context, R.attr.colorControlNormal);
            i++;

            states[i] = FOCUSED_STATE_SET;
            colors[i] = colorControlActivated;
            i++;

            states[i] = ACTIVATED_STATE_SET;
            colors[i] = colorControlActivated;
            i++;

            states[i] = PRESSED_STATE_SET;
            colors[i] = colorControlActivated;
            i++;

            states[i] = CHECKED_STATE_SET;
            colors[i] = colorControlActivated;
            i++;
            states[i] = SELECTED_STATE_SET;
            colors[i] = colorControlActivated;
            i++;

            // Default enabled state
            states[i] = EMPTY_STATE_SET;
            colors[i] = colorControlNormal;

            defaultColorStateList = new ColorStateList(states, colors);
        }
        return defaultColorStateList;
    }

    private static int getDisabledThemeAttrColor(Context context, int attr) {
        final ColorStateList csl = getThemeAttrColorStateList(context, attr);
        if (csl != null && csl.isStateful()) {
            // If the CSL is stateful, we'll assume it has a disabled state and use it
            return csl.getColorForState(DISABLED_STATE_SET, csl.getDefaultColor());
        } else {
            // Else, we'll generate the color using disabledAlpha from the theme

            final TypedValue tv = getTypedValue();
            // Now retrieve the disabledAlpha value from the theme
            context.getTheme().resolveAttribute(android.R.attr.disabledAlpha, tv, true);
            final float disabledAlpha = tv.getFloat();

            return getThemeAttrColor(context, attr, disabledAlpha);
        }
    }

    private static ColorStateList getThemeAttrColorStateList(Context context, int attr) {
        TEMP_ARRAY[0] = attr;
        TypedArray a = context.obtainStyledAttributes(null, TEMP_ARRAY);
        try {
            return a.getColorStateList(0);
        } finally {
            a.recycle();
        }
    }

    private static int getThemeAttrColor(Context context, int attr, float alpha) {
        final int color = getThemeAttrColor(context, attr);
        final int originalAlpha = Color.alpha(color);
        return ColorUtils.setAlphaComponent(color, Math.round(originalAlpha * alpha));
    }

    private static int getThemeAttrColor(Context context, int attr) {
        TEMP_ARRAY[0] = attr;
        TypedArray a = context.obtainStyledAttributes(null, TEMP_ARRAY);
        try {
            return a.getColor(0, 0);
        } finally {
            a.recycle();
        }
    }

    private static TypedValue getTypedValue() {
        TypedValue typedValue = TL_TYPED_VALUE.get();
        if (typedValue == null) {
            typedValue = new TypedValue();
            TL_TYPED_VALUE.set(typedValue);
        }
        return typedValue;
    }

}
