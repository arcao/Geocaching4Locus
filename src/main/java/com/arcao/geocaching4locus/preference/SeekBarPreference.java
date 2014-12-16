package com.arcao.geocaching4locus.preference;

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

public class SeekBarPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener {

	private SeekBar mSeekBar;
	private EditText mValueText;
	private final Context mContext;

	private final CharSequence mDialogMessage;
	private final int mDefault;
	private int mMax, mValue = 0;

	public SeekBarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SeekBarPreference);

		mDialogMessage = a.getText(R.styleable.SeekBarPreference_android_dialogMessage);
		mDefault = a.getInt(R.styleable.SeekBarPreference_android_defaultValue, 0);
		mMax = a.getInt(R.styleable.SeekBarPreference_android_max, 100);

		mValue = getPersistedInt(mDefault);

		a.recycle();
	}

	@Override
	protected View onCreateDialogView() {
		View view = LayoutInflater.from(mContext).inflate(R.layout.seek_bar_dialog, null, false);

		TextView messageView = (TextView) view.findViewById(R.id.message);
		messageView.setText(mDialogMessage);

		mSeekBar = (SeekBar) view.findViewById(R.id.progress);
		mSeekBar.setOnSeekBarChangeListener(this);

		mValueText = (EditText) view.findViewById(R.id.progress_text);
		mValueText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				int value = 0;
				try {
					value = Integer.parseInt(s.toString());
				} catch (NumberFormatException e) {}

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
				} catch (NumberFormatException e) {}

				if (value > mMax) {
					value = mMax;
					mValueText.setText(String.valueOf(value));
					mValueText.setSelection(mValueText.getText().length());
				}

				mSeekBar.setProgress(value);
			}
		});

		if (shouldPersist())
			mValue = getPersistedInt(mDefault);

		return view;
	}

	@Override
	protected void onBindDialogView(@NonNull View v) {
		super.onBindDialogView(v);
		mSeekBar.setMax(mMax);
		mSeekBar.setProgress(mValue);
		mValueText.setText(String.valueOf(mValue));
		mValueText.setSelection(mValueText.getText().length());
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

		mValueText.setText(String.valueOf(value));
		mValueText.setSelection(mValueText.getText().length());
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
}
