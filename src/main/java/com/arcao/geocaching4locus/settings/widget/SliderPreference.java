package com.arcao.geocaching4locus.settings.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.arcao.geocaching4locus.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class SliderPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener, TextWatcher {

	@BindView(R.id.seekbar) SeekBar mSeekBar;
	@BindView(R.id.input) EditText mInputText;
	@BindView(R.id.message) TextView mMessageText;

	private final Context mContext;

	private final CharSequence mDialogMessage;
	private final int mDefault;
	private int mMax, mValue = 0;

	public SliderPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SliderPreference);

		mDialogMessage = a.getText(R.styleable.SliderPreference_android_dialogMessage);
		mDefault = a.getInt(R.styleable.SliderPreference_android_defaultValue, 0);
		mMax = a.getInt(R.styleable.SliderPreference_android_max, 100);

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

		mSeekBar.setMax(mMax);
		mSeekBar.setProgress(mValue);
		mSeekBar.setOnSeekBarChangeListener(this);

		mInputText.setText(String.valueOf(mValue));
		mInputText.setSelection(mInputText.getText().length());
		mInputText.addTextChangedListener(this);

		mMessageText.setText(mDialogMessage);
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
	public void onProgressChanged(SeekBar seek, int value, boolean fromTouch) {
		if (!fromTouch)
			return;

		mInputText.setText(String.valueOf(value));
		mInputText.setSelection(mInputText.getText().length());
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_POSITIVE) {
			mValue = mSeekBar.getProgress();

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

	public void setMax(int max) {
		mMax = max;
	}

	public int getMax() {
		return mMax;
	}

	public void setProgress(int progress) {
		mValue = progress;
		if (mSeekBar != null)
			mSeekBar.setProgress(progress);
	}

	public int getProgress() {
		return mValue;
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		int value = 0;
		try {
			value = Integer.parseInt(s.toString());
		} catch (NumberFormatException e) {
			Timber.e(e, e.getMessage());
		}

		if (value > mMax) {
			value = mMax;
		}

		mSeekBar.setProgress(value);
	}

	@Override
	public void afterTextChanged(Editable s) {
		int value = 0;
		try {
			value = Integer.parseInt(s.toString());
		} catch (NumberFormatException e) {
			// do nothing
		}

		if (value > mMax) {
			value = mMax;
			mInputText.setText(String.valueOf(value));
			mInputText.setSelection(mInputText.getText().length());
		}

		mSeekBar.setProgress(value);
	}
}
