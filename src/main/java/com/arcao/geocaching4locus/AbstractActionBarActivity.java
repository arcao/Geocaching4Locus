package com.arcao.geocaching4locus;

import android.content.res.TypedArray;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import com.example.android.cheatsheet.CheatSheet;

public abstract class AbstractActionBarActivity extends FragmentActivity {

	@Override
	public abstract boolean onCreateOptionsMenu(Menu menu);
	protected abstract boolean onOptionsItemSelected(int itemId);

	@Override
	public final boolean onOptionsItemSelected(MenuItem item) {
		return onOptionsItemSelected(item.getItemId()) || super.onOptionsItemSelected(item);

	}

	protected void applyMenuItemOnView(final int resMenuItem, int resView) {
		View v = findViewById(resView);
		if (v == null)
			return;

		v.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onOptionsItemSelected(resMenuItem);
			}
		});
		CheatSheet.setup(v);
	}

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
