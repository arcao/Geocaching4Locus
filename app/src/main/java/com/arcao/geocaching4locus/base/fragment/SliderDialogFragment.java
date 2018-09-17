package com.arcao.geocaching4locus.base.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
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

    @BindView(R.id.seekbar) SeekBar seekBarView;
    @BindView(R.id.input) EditText editTextView;
    @BindView(R.id.message) TextView messageView;

    private int min;
    private int max = 100;
    private int step = 1;
    int value = min;
    int newValue = value;

    private WeakReference<DialogListener> dialogListenerRef;

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
        return newValue;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            dialogListenerRef = new WeakReference<>((DialogListener) activity);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement DialogListeners");
        }
    }

    private void fireDialogClose() {
        DialogListener listener = dialogListenerRef.get();
        if (listener != null) {
            listener.onDialogClosed(this);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        min = getArguments().getInt(PARAM_MIN, min);
        max = getArguments().getInt(PARAM_MAX, max);
        step = getArguments().getInt(PARAM_STEP, step);
        value = getArguments().getInt(PARAM_DEFAULT_VALUE, value);

        if (value % step != 0) {
            value = (value / step) * step;
        }

        if (value < min) {
            value = min;
        }

        newValue = value;

        if (savedInstanceState != null) {
            value = savedInstanceState.getInt(PARAM_DEFAULT_VALUE, value);
        }

        return new MaterialDialog.Builder(getActivity())
                .title(getArguments().getInt(PARAM_TITLE))
                .customView(prepareView(), false)
                .positiveText(R.string.button_ok)
                .negativeText(R.string.button_cancel)
                .onAny((materialDialog, dialogAction) -> {
                    if (dialogAction == DialogAction.POSITIVE)
                        newValue = value;

                    fireDialogClose();
                })
                .build();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(PARAM_DEFAULT_VALUE, value);
        super.onSaveInstanceState(outState);
    }

    private View prepareView() {
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.view_slider, null, false);
        ButterKnife.bind(this, view);

        int message = getArguments().getInt(PARAM_MESSAGE);
        if (message != 0) {
            messageView.setVisibility(View.VISIBLE);
            messageView.setText(message);
        } else {
            messageView.setVisibility(View.GONE);
        }

        seekBarView.setMax((max - min) / step);
        seekBarView.setProgress((value - min) / step);
        seekBarView.setOnSeekBarChangeListener(this);

        editTextView.setText(String.valueOf(value));
        editTextView.setSelection(editTextView.getText().length());
        editTextView.setFilters(new InputFilter[]{
                new InputTextFilter(editTextView, min, max, step)
        });
        editTextView.addTextChangedListener(this);

        return view;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        value = progress * step + min;
        if (fromUser) {
            editTextView.setText(String.valueOf(value));
            editTextView.setSelection(editTextView.getText().length());
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
        value = min;
        try {
            value = Integer.parseInt(s.toString());
        } catch (NumberFormatException e) {
            // fall trough
        }

        seekBarView.setProgress((value - min) / step);
    }

    private static class InputTextFilter extends NumberKeyListener {
        private static final char[] DIGIT_CHARACTERS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        final EditText editTextView;
        @Nullable private final String[] availableValues;
        private final int min;
        private final int max;

        InputTextFilter(EditText editTextView, int min, int max, int step) {
            this.editTextView = editTextView;
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
            editTextView.post(() -> {
                int len = editTextView.getText().length();
                editTextView.setSelection(Math.min(start, len), Math.min(stop, len));
            });
        }
    }
}