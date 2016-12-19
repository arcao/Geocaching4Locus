package com.arcao.geocaching4locus.base.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.NumberKeyListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.arcao.geocaching4locus.R;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SliderDialogFragment extends AbstractDialogFragment implements SeekBar.OnSeekBarChangeListener, TextWatcher {
	private static final String PARAM_TITLE = "TITLE";
	private static final String PARAM_MESSAGE = "MESSAGE";
	private static final String PARAM_DEFAULT_VALUE = "DEFAULT_VALUE";
	private static final String PARAM_MIN = "MIN_VALUE";
	private static final String PARAM_MAX = "MAX_VALUE";
	private static final String PARAM_STEP = "STEP";

	public interface DialogListener {
		void onDialogClosed(SliderDialogFragment fragment);
	}

	@BindView(R.id.seekbar) SeekBar mSeekBar;
	@BindView(R.id.input) EditText mValueText;
	@BindView(R.id.message) TextView mMessageText;

	private int mMin = 0;
	private int mMax = 100;
	private int mStep = 1;
	int mValue = mMin;
	int mNewValue = mValue;

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
			throw new ClassCastException(activity.toString() + " must implement DialogListeners");
		}
	}

	void fireDialogClose() {
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
				.positiveText(R.string.button_ok)
				.negativeText(R.string.button_cancel)
				.onAny(new MaterialDialog.SingleButtonCallback() {
					@Override
					public void onClick(@NonNull MaterialDialog materialDialog,
							@NonNull DialogAction dialogAction) {
						if (dialogAction == DialogAction.POSITIVE)
							mNewValue = mValue;

						fireDialogClose();
					}
				})
				.build();
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putInt(PARAM_DEFAULT_VALUE, mValue);
		super.onSaveInstanceState(outState);
	}

	private View prepareView() {
		@SuppressLint("InflateParams")
		View view = LayoutInflater.from(getActivity()).inflate(R.layout.view_slider, null, false);
		ButterKnife.bind(this, view);

		int message = getArguments().getInt(PARAM_MESSAGE);
		if (message != 0) {
			mMessageText.setVisibility(View.VISIBLE);
			mMessageText.setText(message);
		} else {
			mMessageText.setVisibility(View.GONE);
		}

		mSeekBar.setMax((mMax - mMin) / mStep);
		mSeekBar.setProgress((mValue - mMin) / mStep);
		mSeekBar.setOnSeekBarChangeListener(this);

		mValueText.setText(String.valueOf(mValue));
		mValueText.setSelection(mValueText.getText().length());
		mValueText.setFilters(new InputFilter[] {
						new InputTextFilter(mValueText, mMin, mMax, mStep)
		});
		mValueText.addTextChangedListener(this);

		return view;
	}

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

	private static class InputTextFilter extends NumberKeyListener {
		private static final char[] DIGIT_CHARACTERS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
		final EditText mEditText;
		@Nullable private final String[] mAvailableValues;
		private final int mMin;
		private final int mMax;

		InputTextFilter(EditText editText, int min, int max, int step) {
			mEditText = editText;
			mMin = min;
			mMax = max;

			if (step == 1) {
				mAvailableValues = null;
			} else {
				mAvailableValues = new String[((max - min) / step) + 1];

				final int length = mAvailableValues.length;
				for (int i = 0; i < length; i++) {
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
			if (ArrayUtils.isEmpty(mAvailableValues)) {
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
					return StringUtils.EMPTY;
				} else {
					return filtered;
				}
			} else {
				CharSequence filtered = String.valueOf(source.subSequence(start, end));
				if (TextUtils.isEmpty(filtered)) {
					return StringUtils.EMPTY;
				}
				String result = String.valueOf(dest.subSequence(0, dstart)) + filtered
								+ dest.subSequence(dend, dest.length());

				for (String val : mAvailableValues) {
					if (val.startsWith(result)) {
						postSetSelection(result.length(), val.length());
						return val.subSequence(dstart, val.length());
					}
				}
				return StringUtils.EMPTY;
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