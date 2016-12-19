package com.arcao.geocaching4locus.import_gc.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.arcao.geocaching.api.util.GeocachingUtils;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.base.fragment.AbstractDialogFragment;

import org.apache.commons.lang3.StringUtils;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;


public class GCNumberInputDialogFragment extends AbstractDialogFragment {
	public static final String FRAGMENT_TAG = GCNumberInputDialogFragment.class.getName();

	private static final String PARAM_INPUT = "INPUT";
	private static final String PARAM_ERROR_MESSAGE = "ERROR_MESSAGE";

	public interface DialogListener {
		void onInputFinished(String input);
	}

	@BindView(R.id.input) EditText mEditText;
	@BindView(R.id.layout) TextInputLayout mLayout;

	private WeakReference<DialogListener> mDialogListenerRef;

	public static GCNumberInputDialogFragment newInstance() {
		return new GCNumberInputDialogFragment();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mDialogListenerRef = new WeakReference<>((DialogListener) activity);
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement DialogListener");
		}
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mEditText != null && isShowing()) {
			outState.putCharSequence(PARAM_INPUT, mEditText.getText());
			outState.putCharSequence(PARAM_ERROR_MESSAGE, mLayout.getError());
		}
	}

	void fireOnInputFinished(String input) {
		DialogListener listener = mDialogListenerRef.get();
		if (listener != null) {
			listener.onInputFinished(input);
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
				.onPositive(new MaterialDialog.SingleButtonCallback() {
					@Override
					public void onClick(@NonNull MaterialDialog materialDialog,
							@NonNull DialogAction dialogAction) {
						if (validateInput(mEditText)) {
							fireOnInputFinished(mEditText.getText().toString());
							materialDialog.dismiss();
						} else {
							mLayout.setError(getText(R.string.error_gc_code_invalid));
						}
					}
				})
				.onNegative(new MaterialDialog.SingleButtonCallback() {
					@Override
					public void onClick(@NonNull MaterialDialog materialDialog,
							@NonNull DialogAction dialogAction) {
						fireOnInputFinished(null);
						materialDialog.dismiss();
					}
				}).build();

		final Window window = dialog.getWindow();
		if (window != null)
			window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

		final View customView = dialog.getCustomView();
		if (customView == null)
			throw new IllegalStateException("Custom view is null");

		ButterKnife.bind(this, customView);

		mEditText.setText(R.string.prefix_gc);
		mEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s != null && s.length() > 0 && mEditText.getError() != null) {
					mLayout.setError(null);
				}
			}
		});

		if (savedInstanceState != null && savedInstanceState.containsKey(PARAM_INPUT)) {
			mEditText.setText(savedInstanceState.getCharSequence(PARAM_INPUT));
			mLayout.setError(savedInstanceState.getCharSequence(PARAM_ERROR_MESSAGE));
		}

		// move caret on a last position
		mEditText.setSelection(mEditText.getText().length());

		return dialog;
	}

	static boolean validateInput(EditText editText) {
		String value = editText.getText().toString();

		if (StringUtils.isEmpty(value)) {
			return false;
		}

		try {
			return GeocachingUtils.cacheCodeToCacheId(value) > 0;
		} catch (IllegalArgumentException e) {
			Timber.e(e, e.getMessage());
			return false;
		}
	}
}
