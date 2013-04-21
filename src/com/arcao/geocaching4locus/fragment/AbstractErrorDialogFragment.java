package com.arcao.geocaching4locus.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.ContextThemeWrapper;

import com.arcao.geocaching4locus.R;

public class AbstractErrorDialogFragment extends AbstractDialogFragment {
	private static final String PARAM_TITLE = "TITLE";
	private static final String PARAM_ERROR_MESSAGE = "ERROR_MESSAGE";
	private static final String PARAM_ADDITIONAL_MESSAGE = "ADDITIONAL_MESSAGE";

	public void prepareDialog(int resTitle, int resErrorMessage, String additionalMessage) {
		Bundle args = new Bundle();
		args.putInt(PARAM_TITLE, resTitle);
		args.putInt(PARAM_ERROR_MESSAGE, resErrorMessage);
		args.putString(PARAM_ADDITIONAL_MESSAGE, additionalMessage);
		setArguments(args);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setCancelable(false);
	}

	public DialogInterface.OnClickListener getPositiveButtonOnClickListener() {
		return null;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.G4LTheme_Dialog));

		Bundle args = getArguments();
		String message = getString(args.getInt(PARAM_ERROR_MESSAGE), args.getString(PARAM_ADDITIONAL_MESSAGE));

		builder.setTitle(args.getInt(PARAM_TITLE));
		builder.setMessage(Html.fromHtml(message));
		builder.setPositiveButton(R.string.ok_button, getPositiveButtonOnClickListener());

		return builder.create();
	}
}
