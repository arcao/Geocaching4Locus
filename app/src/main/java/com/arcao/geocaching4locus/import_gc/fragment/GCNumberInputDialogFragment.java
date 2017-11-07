package com.arcao.geocaching4locus.import_gc.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.MDButton;
import com.arcao.geocaching.api.util.GeocachingUtils;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.base.fragment.AbstractDialogFragment;

import org.apache.commons.lang3.StringUtils;

import java.lang.ref.WeakReference;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;


public class GCNumberInputDialogFragment extends AbstractDialogFragment {
    public static final String FRAGMENT_TAG = GCNumberInputDialogFragment.class.getName();

    private static final String PARAM_INPUT = "INPUT";
    private static final String PARAM_ERROR_MESSAGE = "ERROR_MESSAGE";
    private static final Pattern PATTERN_CACHE_ID_SEPARATOR = Pattern.compile("[\\W]+");

    public interface DialogListener {
        void onInputFinished(@NonNull String[] input);
    }

    @BindView(R.id.input) EditText editTextView;
    @BindView(R.id.layout) TextInputLayout textInputLayout;

    private WeakReference<DialogListener> dialogListenerRef;

    public static GCNumberInputDialogFragment newInstance() {
        return new GCNumberInputDialogFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            dialogListenerRef = new WeakReference<>((DialogListener) activity);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement DialogListener");
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (editTextView != null && isShowing()) {
            outState.putCharSequence(PARAM_INPUT, editTextView.getText());
            outState.putCharSequence(PARAM_ERROR_MESSAGE, textInputLayout.getError());
        }
    }

    void fireOnInputFinished(@Nullable String input) {
        DialogListener listener = dialogListenerRef.get();
        if (listener != null) {
            listener.onInputFinished(!TextUtils.isEmpty(input) ? PATTERN_CACHE_ID_SEPARATOR.split(input) : new String[0]);
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        fireOnInputFinished(null);
        super.onCancel(dialog);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.title_import_from_gc)
                .positiveText(R.string.button_ok)
                .negativeText(R.string.button_cancel)
                .customView(R.layout.dialog_gc_number_input, false)
                .autoDismiss(false)
                .onPositive((materialDialog, dialogAction) -> {
                    if (validateInput(editTextView)) {
                        fireOnInputFinished(editTextView.getText().toString());
                        materialDialog.dismiss();
                    } else {
                        textInputLayout.setError(getText(R.string.error_gc_code_invalid));
                    }
                })
                .onNegative((materialDialog, dialogAction) -> {
                    fireOnInputFinished(null);
                    materialDialog.dismiss();
                }).build();

        final Window window = dialog.getWindow();
        if (window != null)
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        final View customView = dialog.getCustomView();
        if (customView == null)
            throw new IllegalStateException("Custom view is null");

        ButterKnife.bind(this, customView);

        MDButton positiveButton = dialog.getActionButton(DialogAction.POSITIVE);

        editTextView.setNextFocusDownId(positiveButton.getId());
        editTextView.setText(R.string.prefix_gc);
        editTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null && s.length() > 0 && editTextView.getError() != null) {
                    textInputLayout.setError(null);
                }
            }
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(PARAM_INPUT)) {
            editTextView.setText(savedInstanceState.getCharSequence(PARAM_INPUT));
            textInputLayout.setError(savedInstanceState.getCharSequence(PARAM_ERROR_MESSAGE));
        }

        // move caret on a last position
        editTextView.setSelection(editTextView.getText().length());

        return dialog;
    }

    static boolean validateInput(EditText editText) {
        String value = editText.getText().toString();

        if (StringUtils.isEmpty(value)) {
            return false;
        }

        String[] cacheIds = PATTERN_CACHE_ID_SEPARATOR.split(value);

        for (String cacheId : cacheIds) {
            try {
                if (GeocachingUtils.cacheCodeToCacheId(cacheId) <= 0)
                    return false;
            } catch (IllegalArgumentException e) {
                Timber.e(e, e.getMessage());
                return false;
            }
        }

        return true;
    }
}
