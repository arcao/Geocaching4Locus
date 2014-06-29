package com.arcao.fragment.number_chooser;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;

import com.arcao.geocaching4locus.R;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class NumberChooserDialogFragmentHoneycomb extends NumberChooserDialogFragment {
	@Override
	protected View prepareView() {
		View view = getActivity().getLayoutInflater().inflate(R.layout.number_picker_dialog, null);

		NumberPicker numberPicker = (NumberPicker) view.findViewById(R.id.number_picker_dialog_number_picker);
		numberPicker.setMinValue(getArguments().getInt(PARAM_MIN_VALUE, 0));
		numberPicker.setMaxValue(getArguments().getInt(PARAM_MAX_VALUE, 100));
		numberPicker.setValue(mValue);

		numberPicker.setOnValueChangedListener(new OnValueChangeListener() {
			@Override
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				mValue = newVal;
			}
		});

		return view;
	}
}
