package com.arcao.geocaching4locus.fragment.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.util.SpannedFix;

public class AbstractErrorDialogFragment extends AbstractDialogFragment {
	private static final String PARAM_TITLE = "TITLE";
	private static final String PARAM_ERROR_MESSAGE = "ERROR_MESSAGE";
	private static final String PARAM_ADDITIONAL_MESSAGE = "ADDITIONAL_MESSAGE";

	protected void prepareDialog(int resTitle, int resErrorMessage, String additionalMessage) {
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

	protected DialogInterface.OnClickListener getPositiveButtonOnClickListener() {
		return null;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		Bundle args = getArguments();
		String message = getString(args.getInt(PARAM_ERROR_MESSAGE), args.getString(PARAM_ADDITIONAL_MESSAGE));

		builder.setTitle(args.getInt(PARAM_TITLE));
		builder.setMessage(SpannedFix.fromHtml(message));
		builder.setPositiveButton(R.string.ok_button, getPositiveButtonOnClickListener());

		return builder.create();
	}
}
