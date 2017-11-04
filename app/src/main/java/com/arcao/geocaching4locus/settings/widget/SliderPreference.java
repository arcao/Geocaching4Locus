package com.arcao.geocaching4locus.settings.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.NumberKeyListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.arcao.geocaching4locus.R;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class SliderPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener, TextWatcher {

	@BindView(R.id.seekbar) SeekBar mSeekBar;
	@BindView(R.id.input) EditText mValueText;
	@BindView(R.id.message) TextView mMessageText;

	private final Context mContext;

	private final CharSequence mDialogMessage;
	private final int mDefault;
	private int mMin, mMax, mValue, mStep;

	public SliderPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SliderPreference);

		mDialogMessage = a.getText(R.styleable.SliderPreference_android_dialogMessage);
		mDefault = a.getInt(R.styleable.SliderPreference_android_defaultValue, 0);
		mMin = a.getInt(R.styleable.SliderPreference_min, 0);
		mMax = a.getInt(R.styleable.SliderPreference_android_max, 100);
		mStep = a.getInt(R.styleable.SliderPreference_step, 1);

		mValue = getPersistedInt(mDefault);

		a.recycle();
	}

	@Override
	protected View onCreateDialogView() {
		@SuppressLint("InflateParams")
		View view = LayoutInflater.from(mContext).inflate(R.layout.view_slider, null, false);
		ButterKnife.bind(this, view);

		if (shouldPersist())
			mValue = getPersistedInt(mDefault);

		return view;
	}

	@Override
	protected void onBindDialogView(@NonNull View v) {
		super.onBindDialogView(v);

		if (mValue % mStep != 0) {
			mValue = (mValue / mStep) * mStep;
		}

		if (mValue < mMin) {
			mValue = mMin;
		}

		persistInt(mValue);

		if (!TextUtils.isEmpty(mDialogMessage)) {
			mMessageText.setVisibility(View.VISIBLE);
			mMessageText.setText(mDialogMessage);
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
	}

	@Override
	protected void onSetInitialValue(boolean restore, Object defaultValue) {
		super.onSetInitialValue(restore, defaultValue);
		if (restore)
			mValue = shouldPersist() ? getPersistedInt(mDefault) : 0;
		else
			mValue = (Integer) defaultValue;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		mValue = progress * mStep + mMin;
		if (fromUser) {
			mValueText.setText(String.valueOf(mValue));
			mValueText.setSelection(mValueText.getText().length());
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_POSITIVE) {
			if (shouldPersist())
				persistInt(mValue);
			callChangeListener(mValue);
		}
		super.onClick(dialog, which);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seek) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seek) {
	}

	public void setMin(int min) {
		mMin = min;
	}

	public int getMin() {
		return mMin;
	}

	public void setMax(int max) {
		mMax = max;
	}

	public int getMax() {
		return mMax;
	}

	public void setProgress(int progress) {
		if (progress == mValue)
			return;

		mValue = progress;

		persistInt(mValue);
		notifyDependencyChange(shouldDisableDependents());
		notifyChanged();

		if (mSeekBar != null)
			mSeekBar.setProgress(progress);
	}

	public int getProgress() {
		return mValue;
	}

	public void setStep(int step) {
		mStep = step;
	}

	public int getStep() {
		return mStep;
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

	@Override
	protected Parcelable onSaveInstanceState() {
        /*
         * Suppose a client uses this preference type without persisting. We
         * must save the instance state so it is able to, for example, survive
         * orientation changes.
         */

		final Parcelable superState = super.onSaveInstanceState();
		if (isPersistent()) {
			// No need to save instance state since it's persistent
			return superState;
		}

		// Save the instance state
		final SavedState myState = new SavedState(superState);
		myState.value = mValue;
		myState.min = mMin;
		myState.max = mMax;
		myState.step = mStep;
		return myState;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (!state.getClass().equals(SavedState.class)) {
			// Didn't save state for us in onSaveInstanceState
			super.onRestoreInstanceState(state);
			return;
		}

		// Restore the instance state
		SavedState myState = (SavedState) state;
		super.onRestoreInstanceState(myState.getSuperState());
		mValue = myState.value;
		mMin = myState.min;
		mMax = myState.max;
		mStep = myState.step;
	}

	/**
	 * SavedState, a subclass of {@link BaseSavedState}, will store the state
	 * of MyPreference, a subclass of Preference.
	 * <p>
	 * It is important to always call through to super methods.
	 */
	private static class SavedState extends BaseSavedState {
		int value, min, max, step;

		public SavedState(Parcel source) {
			super(source);

			// Restore the click counter
			value = source.readInt();
			min = source.readInt();
			max = source.readInt();
			step = source.readInt();
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);

			// Save the click counter
			dest.writeInt(value);
			dest.writeInt(min);
			dest.writeInt(max);
			dest.writeInt(step);
		}

		public SavedState(Parcelable superState) {
			super(superState);
		}

		@SuppressWarnings("unused")
		public static final Parcelable.Creator<SavedState> CREATOR =
				new Parcelable.Creator<SavedState>() {
					public SavedState createFromParcel(Parcel in) {
						return new SavedState(in);
					}

					public SavedState[] newArray(int size) {
						return new SavedState[size];
					}
				};
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
