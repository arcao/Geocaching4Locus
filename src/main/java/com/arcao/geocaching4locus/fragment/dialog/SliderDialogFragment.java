package com.arcao.geocaching4locus.fragment.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.text.*;
import android.text.method.NumberKeyListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.arcao.geocaching4locus.R;

import java.lang.ref.WeakReference;

public class SliderDialogFragment extends AbstractDialogFragment {
	private static final String PARAM_TITLE = "TITLE";
	private static final String PARAM_MESSAGE = "MESSAGE";
	private static final String PARAM_DEFAULT_VALUE = "DEFAULT_VALUE";
	private static final String PARAM_MIN = "MIN_VALUE";
	private static final String PARAM_MAX = "MAX_VALUE";
	private static final String PARAM_STEP = "STEP";

	public interface DialogListener {
		void onDialogClosed(SliderDialogFragment fragment);
	}

	private SeekBar mSeekBar;
	private EditText mValueText;

	private int mMin = 0;
	private int mMax = 100;
	private int mStep = 1;
	private int mValue = mMin;
	private int mNewValue = mValue;

	private WeakReference<DialogListener> mDialogListenerRef;

	public static SliderDialogFragment newInstance(@StringRes int title, @StringRes int message, int min, int max, int defaultValue) {
		return newInstance(title, message, min, max, defaultValue, 1);
	}

	public static SliderDialogFragment newInstance(@StringRes int title, @StringRes int message, int min, int max, int defaultValue, int step) {
		SliderDialogFragment fragment = new SliderDialogFragment();

		Bundle args = new Bundle();
		args.putInt(PARAM_TITLE, title);
		args.putInt(PARAM_MESSAGE, message);
		args.putInt(PARAM_MIN, min);
		args.putInt(PARAM_MAX, max);
		args.putInt(PARAM_DEFAULT_VALUE, defaultValue);
		args.putInt(PARAM_STEP, step);

		fragment.setArguments(args);
		fragment.setCancelable(false);

		return fragment;
	}

	public int getValue() {
		return mNewValue;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mDialogListenerRef = new WeakReference<>((DialogListener) activity);
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement SeekBarDialogListener");
		}
	}

	private void fireDialogClose() {
		DialogListener listener = mDialogListenerRef.get();
		if (listener != null) {
			listener.onDialogClosed(this);
		}
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		mMin = getArguments().getInt(PARAM_MIN, mMin);
		mMax = getArguments().getInt(PARAM_MAX, mMax);
		mStep = getArguments().getInt(PARAM_STEP, mStep);
		mValue = getArguments().getInt(PARAM_DEFAULT_VALUE, mValue);

		if (mValue % mStep != 0) {
			mValue = (mValue / mStep) * mStep;
		}

		if (mValue < mMin) {
			mValue = mMin;
		}

		mNewValue = mValue;

		if (savedInstanceState != null ) {
			mValue = savedInstanceState.getInt(PARAM_DEFAULT_VALUE, mValue);
		}

		return new MaterialDialog.Builder(getActivity())
						.title(getArguments().getInt(PARAM_TITLE))
						.customView(prepareView(), false)
						.positiveText(R.string.ok_button)
						.negativeText(R.string.cancel_button)
						.callback(new MaterialDialog.ButtonCallback() {
							@Override
							public void onPositive(MaterialDialog dialog) {
								mNewValue = mValue;
								fireDialogClose();
							}

							@Override
							public void onNegative(MaterialDialog dialog) {
								fireDialogClose();
							}
						}).build();
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putInt(PARAM_DEFAULT_VALUE, mValue);
		super.onSaveInstanceState(outState);
	}

	private View prepareView() {
		@SuppressLint("InflateParams")
		View view = LayoutInflater.from(getActivity()).inflate(R.layout.seek_bar_dialog, null, false);

		TextView messageView = (TextView) view.findViewById(R.id.message);
		int message = getArguments().getInt(PARAM_MESSAGE);
		if (message != 0) {
			messageView.setVisibility(View.VISIBLE);
			messageView.setText(message);
		} else {
			messageView.setVisibility(View.GONE);
		}

		mSeekBar = (SeekBar) view.findViewById(R.id.progress);
		mSeekBar.setMax((mMax - mMin) / mStep);
		mSeekBar.setProgress((mValue - mMin) / mStep);
		mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				mValue = progress * mStep + mMin;
				if (fromUser) {
					mValueText.setText(String.valueOf(mValue));
					mValueText.setSelection(mValueText.getText().length());
				}
			}
		});

		mValueText = (EditText) view.findViewById(R.id.progress_text);
		mValueText.setText(String.valueOf(mValue));
		mValueText.setSelection(mValueText.getText().length());
		mValueText.setFilters(new InputFilter[] {
						new InputTextFilter(mValueText, mMin, mMax, mStep)
		});
		mValueText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				mValue = mMin;
				try {
					mValue = Integer.parseInt(s.toString());
				} catch (NumberFormatException e) {
					// fall trough
				}

				mSeekBar.setProgress((mValue - mMin) / mStep);
			}
		});

		return view;
	}

	private static class InputTextFilter extends NumberKeyListener {
		private static final char[] DIGIT_CHARACTERS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
		private final EditText mEditText;
		private final String[] mAvailableValues;
		private final int mMin;
		private final int mMax;

		private InputTextFilter(EditText editText, int min, int max, int step) {
			mEditText = editText;
			mMin = min;
			mMax = max;

			if (step == 1) {
				mAvailableValues = null;
			} else {
				mAvailableValues = new String[((max - min) / step) + 1];

				for (int i = 0; i < mAvailableValues.length; i++) {
					mAvailableValues[i] = String.valueOf(min + (i * step));
				}
			}
		}

		public int getInputType() {
			return InputType.TYPE_CLASS_TEXT;
		}

		@Override
		protected char[] getAcceptedChars() {
			return DIGIT_CHARACTERS;
		}

		@Override
		public CharSequence filter(@NonNull CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
			if (mAvailableValues == null) {
				CharSequence filtered = super.filter(source, start, end, dest, dstart, dend);
				if (filtered == null) {
					filtered = source.subSequence(start, end);
				}

				String result = String.valueOf(dest.subSequence(0, dstart)) + filtered
								+ dest.subSequence(dend, dest.length());

				if (TextUtils.isEmpty(result)) {
					return result;
				}

				int val = mMin;
				try {
					val = Integer.parseInt(result);
				} catch (NumberFormatException e) {
					// do nothing
				}

				if (val > mMax) {
					return "";
				} else {
					return filtered;
				}
			} else {
				CharSequence filtered = String.valueOf(source.subSequence(start, end));
				if (TextUtils.isEmpty(filtered)) {
					return "";
				}
				String result = String.valueOf(dest.subSequence(0, dstart)) + filtered
								+ dest.subSequence(dend, dest.length());

				for (String val : mAvailableValues) {
					if (val.startsWith(result)) {
						postSetSelection(result.length(), val.length());
						return val.subSequence(dstart, val.length());
					}
				}
				return "";
			}
		}

		private void postSetSelection(final int start, final int stop) {
			mEditText.post(new Runnable() {
				@Override
				public void run() {
					int len = mEditText.getText().length();
					mEditText.setSelection(Math.min(start, len), Math.min(stop, len));
				}
			});
		}
	}
}