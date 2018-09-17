package com.arcao.geocaching4locus.search_nearest.widget;

import android.content.Context;
import androidx.core.view.ViewCompat;
import androidx.appcompat.R;
import androidx.appcompat.widget.AppCompatEditText;
import android.text.method.MovementMethod;
import android.util.AttributeSet;

import com.arcao.geocaching4locus.base.util.ColorUtil;

public class SpinnerTextView extends AppCompatEditText {
    public SpinnerTextView(Context context) {
        this(context, null);
    }

    public SpinnerTextView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.editTextStyle);
    }

    public SpinnerTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        ViewCompat.setBackgroundTintList(this, ColorUtil.getDefaultColorStateList(context));
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
