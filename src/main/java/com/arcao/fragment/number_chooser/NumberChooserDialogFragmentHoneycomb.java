package com.arcao.fragment.number_chooser;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;

import com.arcao.geocaching4locus.R;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class NumberChooserDialogFragmentHoneycomb extends NumberChooserDialogFragment {
	@Override
	protected View prepareView() {
		@SuppressLint("InflateParams")
		View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_number_picker, null);

		String[] values = getNumberPickerValues(mMinValue, mMaxValue, mStep);

		NumberPicker numberPicker = (NumberPicker) view.findViewById(R.id.number_picker_dialog_number_picker);
		numberPicker.setMinValue(0);
		numberPicker.setMaxValue(values.length - 1);
		numberPicker.setValue(Math.max(0, (mValue - mMinValue) / mStep));
		numberPicker.setDisplayedValues(values);

		numberPicker.setOnValueChangedListener(new OnValueChangeListener() {
			@Override
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				mValue = mMinValue + (newVal * mStep);
			}
		});

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();

		getDialog().getWindow().clearFlags(
						WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
	}

	protected String[] getNumberPickerValues(int min, int max, int step) {
		String[] values = new String[((max - min) / step) + 1];

		for (int i = 0; i < values.length; i++) {
			values[i] = String.valueOf(min + (i * step));
		}

		return values;
	}
}
