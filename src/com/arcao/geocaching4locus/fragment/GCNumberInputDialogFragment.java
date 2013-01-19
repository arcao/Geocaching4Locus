package com.arcao.geocaching4locus.fragment;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.arcao.geocaching.api.util.GeocachingUtils;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.util.EmptyDialogOnClickListener;


public class GCNumberInputDialogFragment extends AbstractDialogFragment {
	public static final String TAG = GCNumberInputDialogFragment.class.getName();
	
	protected static final String PARAM_INPUT = "INPUT";
	protected static final String PARAM_ERROR_MESSAGE = "ERROR_MESSAGE";
	
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
			outState.putCharSequence(PARAM_ERROR_MESSAGE, editText.getError());
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
		Context context = new ContextThemeWrapper(getActivity(), R.style.G4LTheme_Dialog);
		
		editText = new EditText(context);
		editText.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
		editText.addTextChangedListener(new TextWatcher() {
	    @Override
			public void afterTextChanged(Editable s) {}
	    @Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after){}
	    @Override
			public void onTextChanged(CharSequence s, int start, int before, int count){
	        if(s != null && s.length() > 0 && editText.getError() != null) {
	            editText.setError(null);
	        }
	    }
		}); 
		
		if (savedInstanceState != null && savedInstanceState.containsKey(PARAM_INPUT)) {
			editText.setText(savedInstanceState.getCharSequence(PARAM_INPUT));
			editText.setError(savedInstanceState.getCharSequence(PARAM_ERROR_MESSAGE));
		}
		
		return new AlertDialog.Builder(context)
			.setTitle(R.string.gc_number_input_title)
			.setView(editText)
			// Beware listener can't be null!
			.setPositiveButton(R.string.ok_button, new EmptyDialogOnClickListener())
			.setNegativeButton(R.string.cancel_button, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					fireOnInputFinished(null);
				}
			})
			.create();
	}
	
	@Override
	public void onResume() {
		super.onResume();

		// this is a little bit tricky to prevent auto dismiss
		// source: http://stackoverflow.com/questions/2620444/how-to-prevent-a-dialog-from-closing-when-a-button-is-clicked
		final AlertDialog alertDialog = (AlertDialog) getDialog();
		if (alertDialog == null)
			return;
		
		Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
		button.setOnClickListener(new View.OnClickListener() { 
			@Override
			public void onClick(View v)
			{
				if (validateInput(editText)) {
					fireOnInputFinished(editText.getText().toString());
					alertDialog.dismiss();
				}
			}
		});
	}
	
	protected boolean validateInput(EditText editText) {
		String value = editText.getText().toString();
		
		if (value.length() == 0) {
			editText.setError(getString(R.string.error_input_gc));
			return false;
		}
		
		try {
			if (GeocachingUtils.cacheCodeToCacheId(value) > 0) {
				return true;
			}
		} catch (IllegalArgumentException e) {}
		
		editText.setError(getString(R.string.error_input_gc));
		return false;
	}
	
	public interface OnInputFinishedListener {
		void onInputFinished(String input);
	}
}
