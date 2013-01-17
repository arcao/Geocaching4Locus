package com.arcao.geocaching4locus.fragment;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.widget.EditText;

import com.arcao.geocaching4locus.R;


public class GCNumberInputDialogFragment extends AbstractDialogFragment {
	public static final String TAG = GCNumberInputDialogFragment.class.getName();
	
	protected static final String PARAM_INPUT = "INPUT";
	
	protected WeakReference<OnInputFinishedListener> inputFinishedListenerRef;
	protected EditText editText;

	public static GCNumberInputDialogFragment newInstance() {
		return new GCNumberInputDialogFragment();	
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		try {
			inputFinishedListenerRef = new WeakReference<OnInputFinishedListener>((OnInputFinishedListener) activity);
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnInputFinishedListener");
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		if (editText != null && isShowing()) {
			outState.putCharSequence(PARAM_INPUT, editText.getText());
		}
	}
	
	protected void fireOnInputFinished(String input) {
		OnInputFinishedListener listener = inputFinishedListenerRef.get();
		if (listener != null) {
			listener.onInputFinished(input);
		}
	}
	
	@Override
	public void onCancel(DialogInterface dialog) {
		fireOnInputFinished(null);
		super.onCancel(dialog);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		editText = new EditText(getActivity());
		
		if (savedInstanceState != null && savedInstanceState.containsKey(PARAM_INPUT)) {
			editText.setText(savedInstanceState.getCharSequence(PARAM_INPUT));
		}
		
		return new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.G4LTheme_Dialog))
			.setTitle(R.string.gc_number_input_title)
			.setView(editText)
			.setPositiveButton(R.string.ok_button, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					fireOnInputFinished(editText.getText().toString());
				}
			})
			.setNegativeButton(R.string.cancel_button, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					fireOnInputFinished(null);
				}
			})
			.create();
	}
	
	public interface OnInputFinishedListener {
		void onInputFinished(String input);
	}
}
