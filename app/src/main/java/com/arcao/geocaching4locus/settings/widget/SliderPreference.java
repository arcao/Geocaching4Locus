package com.arcao.geocaching4locus.settings.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import com.arcao.geocaching4locus.R;

import org.apache.commons.lang3.StringUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SliderPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener, TextWatcher {

    @BindView(R.id.seekbar) SeekBar seekBar;
    @BindView(R.id.input) EditText editText;
    @BindView(R.id.message) TextView messageView;

    private final Context context;

    private final CharSequence dialogMessage;
    private final int defaultValue;
    private int min, max, value, step;

    public SliderPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SliderPreference);

        dialogMessage = a.getText(R.styleable.SliderPreference_android_dialogMessage);
        defaultValue = a.getInt(R.styleable.SliderPreference_android_defaultValue, 0);
        min = a.getInt(R.styleable.SliderPreference_min, 0);
        max = a.getInt(R.styleable.SliderPreference_android_max, 100);
        step = a.getInt(R.styleable.SliderPreference_step, 1);

        value = getPersistedInt(defaultValue);

        a.recycle();
    }

    @Override
    protected View onCreateDialogView() {
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(context).inflate(R.layout.view_slider, null, false);
        ButterKnife.bind(this, view);

        if (shouldPersist())
            value = getPersistedInt(defaultValue);

        return view;
    }

    @Override
    protected void onBindDialogView(@NonNull View v) {
        super.onBindDialogView(v);

        if (value % step != 0) {
            value = (value / step) * step;
        }

        if (value < min) {
            value = min;
        }

        persistInt(value);

        if (!TextUtils.isEmpty(dialogMessage)) {
            messageView.setVisibility(View.VISIBLE);
            messageView.setText(dialogMessage);
        } else {
            messageView.setVisibility(View.GONE);
        }

        seekBar.setMax((max - min) / step);
        seekBar.setProgress((value - min) / step);
        seekBar.setOnSeekBarChangeListener(this);

        editText.setText(String.valueOf(value));
        editText.setSelection(editText.getText().length());
        editText.setFilters(new InputFilter[]{
                new InputTextFilter(editText, min, max, step)
        });
        editText.addTextChangedListener(this);
    }

    @Override
    protected void onSetInitialValue(boolean restore, Object defaultValue) {
        super.onSetInitialValue(restore, defaultValue);
        if (restore)
            value = shouldPersist() ? getPersistedInt(this.defaultValue) : 0;
        else
            value = (Integer) defaultValue;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        value = progress * step + min;
        if (fromUser) {
            editText.setText(String.valueOf(value));
            editText.setSelection(editText.getText().length());
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            if (shouldPersist())
                persistInt(value);
            callChangeListener(value);
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
        this.min = min;
    }

    public int getMin() {
        return min;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getMax() {
        return max;
    }

    public void setProgress(int progress) {
        if (progress == value)
            return;

        value = progress;

        persistInt(value);
        notifyDependencyChange(shouldDisableDependents());
        notifyChanged();

        if (seekBar != null)
            seekBar.setProgress(progress);
    }

    public int getProgress() {
        return value;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public int getStep() {
        return step;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        value = min;
        try {
            value = Integer.parseInt(s.toString());
        } catch (NumberFormatException e) {
            // fall trough
        }

        seekBar.setProgress((value - min) / step);
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
        myState.value = value;
        myState.min = min;
        myState.max = max;
        myState.step = step;
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
        value = myState.value;
        min = myState.min;
        max = myState.max;
        step = myState.step;
    }

    /**
     * SavedState, a subclass of {@link BaseSavedState}, will store the state
     * of MyPreference, a subclass of Preference.
     * <p>
     * It is important to always call through to super methods.
     */
    private static class SavedState extends BaseSavedState {
        int value, min, max, step;

        SavedState(Parcel source) {
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

        SavedState(Parcelable superState) {
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
        private static final char[] DIGIT_CHARACTERS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        final EditText editText;
        @Nullable private final String[] availableValues;
        private final int min;
        private final int max;

        InputTextFilter(EditText editText, int min, int max, int step) {
            this.editText = editText;
            this.min = min;
            this.max = max;

            if (step == 1) {
                availableValues = null;
            } else {
                availableValues = new String[((max - min) / step) + 1];

                final int length = availableValues.length;
                for (int i = 0; i < length; i++) {
                    availableValues[i] = String.valueOf(min + (i * step));
                }
            }
        }

        public int getInputType() {
            return InputType.TYPE_CLASS_TEXT;
        }

        @NonNull
        @Override
        protected char[] getAcceptedChars() {
            return DIGIT_CHARACTERS;
        }

        @Override
        public CharSequence filter(@NonNull CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            if (availableValues == null || availableValues.length == 0) {
                CharSequence filtered = super.filter(source, start, end, dest, dstart, dend);
                if (filtered == null) {
                    filtered = source.subSequence(start, end);
                }

                String result = String.valueOf(dest.subSequence(0, dstart)) + filtered
                        + dest.subSequence(dend, dest.length());

                if (TextUtils.isEmpty(result)) {
                    return result;
                }

                int val = min;
                try {
                    val = Integer.parseInt(result);
                } catch (NumberFormatException e) {
                    // do nothing
                }

                if (val > max) {
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

                for (String val : availableValues) {
                    if (val.startsWith(result)) {
                        postSetSelection(result.length(), val.length());
                        return val.subSequence(dstart, val.length());
                    }
                }
                return StringUtils.EMPTY;
            }
        }

        private void postSetSelection(final int start, final int stop) {
            editText.post(() -> {
                int len = editText.getText().length();
                editText.setSelection(Math.min(start, len), Math.min(stop, len));
            });
        }
    }
}
