package com.arcao.geocaching4locus;

import android.content.res.TypedArray;
import android.support.v7.app.ActionBarActivity;

public abstract class AbstractActionBarActivity extends ActionBarActivity {
	public boolean isFloatingWindow() {
		TypedArray typedArray = getTheme().obtainStyledAttributes(new int [] { android.R.attr.windowIsFloating });

		if (typedArray == null)
			return false;

		try {
			return typedArray.getBoolean(0, false);
		} finally {
			typedArray.recycle();
		}
	}
}
