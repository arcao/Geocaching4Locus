package com.arcao.geocaching4locus.dashboard.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.StyleableRes;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.content.res.AppCompatResources;
import android.util.AttributeSet;
import android.widget.ToggleButton;

import com.arcao.geocaching4locus.R;

public class DashboardButton extends ToggleButton {
    private final boolean toggleable;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DashboardButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        final TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DashboardButton, defStyleAttr, defStyleRes);
        toggleable = a.getBoolean(R.styleable.DashboardButton_toggleable, false);

        applyCompoundDrawableTint(a);
        applyTextColorStateList(a);

        a.recycle();
    }

    public DashboardButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DashboardButton, defStyleAttr, R.style.Widget_AppTheme_DashboardButton);
        toggleable = a.getBoolean(R.styleable.DashboardButton_toggleable, false);

        applyCompoundDrawableTint(a);
        applyTextColorStateList(a);

        a.recycle();
    }

    public DashboardButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DashboardButton(Context context) {
        this(context, null);
    }

    private void applyTextColorStateList(TypedArray a) {
        // support for alpha attribute in ColorStateList
        ColorStateList textColorStateList = getCompatColorStateList(getContext(), a, R.styleable.DashboardButton_android_textColor);
        if (textColorStateList != null) setTextColor(textColorStateList);
    }

    private void applyCompoundDrawableTint(TypedArray a) {
        // support for alpha attribute in ColorStateList
        ColorStateList compoundDrawableTint = getCompatColorStateList(getContext(), a, R.styleable.DashboardButton_compoundDrawableTint);
        if (compoundDrawableTint != null) {
            Drawable[] compoundDrawables = getCompoundDrawables();
            for (int i = 0; i < 4; i++) {
                if (compoundDrawables[i] == null)
                    continue;

                compoundDrawables[i] = DrawableCompat.wrap(compoundDrawables[i]);
                DrawableCompat.setTintList(compoundDrawables[i], compoundDrawableTint);
            }
            setCompoundDrawables(compoundDrawables[0], compoundDrawables[1], compoundDrawables[2], compoundDrawables[3]);
        }
    }

    @Override
    public void toggle() {
        if (toggleable) {
            super.toggle();
        }
    }

    @Override
    public void setChecked(boolean checked) {
        if (!toggleable)
            return;

        super.setChecked(checked);
    }

    private static ColorStateList getCompatColorStateList(Context context, TypedArray a, @StyleableRes int index) {
        int resourceId = a.getResourceId(index, 0);
        if (resourceId == 0) return null;

        return AppCompatResources.getColorStateList(context, resourceId);
    }
}
