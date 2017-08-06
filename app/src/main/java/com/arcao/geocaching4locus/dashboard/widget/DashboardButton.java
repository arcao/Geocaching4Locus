package com.arcao.geocaching4locus.dashboard.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ToggleButton;
import com.arcao.geocaching4locus.R;

public class DashboardButton extends ToggleButton {
	private static final float ALPHA_DISABLED = 0.38f;
	private static final float ALPHA_ENABLED = 1F;

	private final boolean mToggleable;

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public DashboardButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);

		final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DashboardButton, defStyleAttr, defStyleRes);
		mToggleable = a.getBoolean(R.styleable.DashboardButton_toggleable, false);
		a.recycle();
	}

	public DashboardButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DashboardButton, defStyleAttr, android.R.style.Widget_Button_Toggle);
		mToggleable = a.getBoolean(R.styleable.DashboardButton_toggleable, false);
		a.recycle();
	}

	public DashboardButton(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DashboardButton(Context context) {
		this(context, null);
	}

	@Override
	public void toggle() {
		if (mToggleable) {
			super.toggle();
		}
	}

	@Override
	public void setChecked(boolean checked) {
		if (!mToggleable)
			return;

		super.setChecked(checked);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		setAlpha(enabled ? ALPHA_ENABLED : ALPHA_DISABLED);
	}
}
