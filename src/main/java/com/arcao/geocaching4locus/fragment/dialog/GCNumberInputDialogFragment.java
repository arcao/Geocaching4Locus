package com.arcao.geocaching4locus.fragment.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import com.afollestad.materialdialogs.MaterialDialog;
import com.arcao.geocaching.api.util.GeocachingUtils;
import com.arcao.geocaching4locus.R;
import timber.log.Timber;

import java.lang.ref.WeakReference;


public class GCNumberInputDialogFragment extends AbstractDialogFragment {
	public static final String FRAGMENT_TAG = GCNumberInputDialogFragment.class.getName();

	private static final String PARAM_INPUT = "INPUT";
	private static final String PARAM_ERROR_MESSAGE = "ERROR_MESSAGE";

	public interface DialogListener {
		void onInputFinished(String input);
	}

	private WeakReference<DialogListener> mDialogListenerRef;
	private EditText mEditText;
	private CharSequence mErrorMessage = null;

	public static GCNumberInputDialogFragment newInstance() {
		return new GCNumberInputDialogFragment();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mDialogListenerRef = new WeakReference<>((DialogListener) activity);
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnInputFinishedListener");
		}
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mEditText != null && isShowing()) {
			outState.putCharSequence(PARAM_INPUT, mEditText.getText());
			outState.putCharSequence(PARAM_ERROR_MESSAGE, mEditText.getError());
		}
	}

	private void fireOnInputFinished(String input) {
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
						.title(R.string.dialog_gc_number_input_title)
						.positiveText(R.string.ok_button)
						.negativeText(R.string.cancel_button)
						.customView(R.layout.dialog_gc_number_input, false)
						.autoDismiss(false)
						.callback(new MaterialDialog.ButtonCallback() {
							@Override
							public void onPositive(MaterialDialog dialog) {
								if (validateInput(mEditText)) {
									fireOnInputFinished(mEditText.getText().toString());
									dialog.dismiss();
								}
							}

							@Override
							public void onNegative(MaterialDialog dialog) {
								fireOnInputFinished(null);
								dialog.dismiss();
							}
						})
						.build();

		View view = dialog.getCustomView();

		mEditText = (EditText) view.findViewById(R.id.gc_code_input_edit_text);
		mEditText.setText("GC");
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
					mEditText.setError(null);
				}
			}
		});

		if (savedInstanceState != null && savedInstanceState.containsKey(PARAM_INPUT)) {
			mEditText.setText(savedInstanceState.getCharSequence(PARAM_INPUT));
			mErrorMessage = savedInstanceState.getCharSequence(PARAM_ERROR_MESSAGE);
		}

		// move caret on a last position
		mEditText.setSelection(mEditText.getText().length());

		return dialog;
	}

	@Override
	public void onStart() {
		super.onStart();

//		getDialog().getWindow().clearFlags(
//			WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

		// setError can't be called from onCreateDialog - cause NPE in StaticLayout.<init>
		// More here: http://code.google.com/p/android/issues/detail?id=19173
		if (mErrorMessage != null) {
			mEditText.setError(mErrorMessage);
			mErrorMessage = null;
		}
	}
	
	private boolean validateInput(EditText editText) {
		String value = editText.getText().toString();

		if (value.length() == 0) {
			editText.setError(getString(R.string.error_input_gc));
			return false;
		}

		try {
			if (GeocachingUtils.cacheCodeToCacheId(value) > 0) {
				return true;
			}
		} catch (IllegalArgumentException e) {
			Timber.e(e, e.getMessage());
		}

		editText.setError(getString(R.string.error_input_gc));
		return false;
	}
}
