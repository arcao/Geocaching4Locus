package com.arcao.geocaching4locus.util;

import android.content.Context;
import android.content.res.ColorStateList;

import static android.support.v7.internal.widget.ThemeUtils.getDisabledThemeAttrColor;
import static android.support.v7.internal.widget.ThemeUtils.getThemeAttrColor;

public class ColorUtil {
	private static final int[] DISABLED_STATE_SET = new int[]{-android.R.attr.state_enabled};
	private static final int[] FOCUSED_STATE_SET = new int[]{android.R.attr.state_focused};
	private static final int[] ACTIVATED_STATE_SET = new int[]{android.R.attr.state_activated};
	private static final int[] PRESSED_STATE_SET = new int[]{android.R.attr.state_pressed};
	private static final int[] CHECKED_STATE_SET = new int[]{android.R.attr.state_checked};
	private static final int[] SELECTED_STATE_SET = new int[]{android.R.attr.state_selected};
	private static final int[] EMPTY_STATE_SET = new int[0];

	private static ColorStateList mDefaultColorStateList = null;

	public static ColorStateList getDefaultColorStateList(Context context) {
		if (mDefaultColorStateList == null) {
			/**
			 * Generate the default color state list which uses the colorControl attributes.
			 * Order is important here. The default enabled state needs to go at the bottom.
			 */

			final int colorControlNormal = getThemeAttrColor(context, android.support.v7.appcompat.R.attr.colorControlNormal);
			final int colorControlActivated = getThemeAttrColor(context,
							android.support.v7.appcompat.R.attr.colorControlActivated);

			final int[][] states = new int[7][];
			final int[] colors = new int[7];
			int i = 0;

			// Disabled state
			states[i] = DISABLED_STATE_SET;
			colors[i] = getDisabledThemeAttrColor(context, android.support.v7.appcompat.R.attr.colorControlNormal);
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

			mDefaultColorStateList = new ColorStateList(states, colors);
		}
		return mDefaultColorStateList;
	}
}
