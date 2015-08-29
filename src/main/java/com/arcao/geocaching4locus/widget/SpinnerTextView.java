package com.arcao.geocaching4locus.widget;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.text.method.MovementMethod;
import android.util.AttributeSet;
import com.arcao.geocaching4locus.util.ColorUtil;

public class SpinnerTextView extends AppCompatEditText {
	public SpinnerTextView(Context context) {
		this(context, null);
	}

	public SpinnerTextView(Context context, AttributeSet attrs) {
		this(context, attrs, android.support.v7.appcompat.R.attr.editTextStyle);
	}

	public SpinnerTextView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		setSupportBackgroundTintList(ColorUtil.getDefaultColorStateList(context));
	}

	@Override
	protected boolean getDefaultEditable() {
		return false;
	}

	@Override
	protected MovementMethod getDefaultMovementMethod() {
		return null;
	}
}
