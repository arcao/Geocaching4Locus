package com.arcao.geocaching4locus.fragment;

import java.lang.ref.WeakReference;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.Html;
import android.view.ContextThemeWrapper;

import com.arcao.geocaching4locus.R;

public class ErrorDialogFragment extends AbstractDialogFragment {
	public static final String TAG = ErrorDialogFragment.class.getName();

	protected int resTitle;
	protected int resErrorMessage;
	protected String additionalMessage;
	protected WeakReference<OnClickListener> onClickListener;
	
	public static ErrorDialogFragment newInstance(int resTitle, int resErrorMessage, String additionalMessage, OnClickListener onClickListener) {
		ErrorDialogFragment fragment = new ErrorDialogFragment();
		
		fragment.resTitle = resTitle;
		fragment.resErrorMessage = resErrorMessage;
		fragment.additionalMessage = additionalMessage;
		fragment.onClickListener = new WeakReference<OnClickListener>(onClickListener);
		
		return fragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.G4LTheme_Dialog));
		
		String message = getString(resErrorMessage, additionalMessage);
		builder.setCancelable(false);
		builder.setTitle(R.string.error_title);
		builder.setMessage(Html.fromHtml(message));
		builder.setPositiveButton(R.string.ok_button, onClickListener.get());
		return builder.create();
	}
}
