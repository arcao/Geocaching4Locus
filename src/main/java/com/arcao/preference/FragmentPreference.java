package com.arcao.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import com.arcao.geocaching4locus.R;

public class FragmentPreference extends Preference {
	private String fragmentName;

	public FragmentPreference(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public FragmentPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		TypedArray a = context.obtainStyledAttributes(attrs,
						R.styleable.FragmentPreference, defStyle, 0);
		fragmentName = a.getString(R.styleable.FragmentPreference_fragmentName);
		a.recycle();
	}

	public String getFragmentName() {
		return fragmentName;
	}
}
