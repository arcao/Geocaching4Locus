package com.arcao.geocaching4locus.fragment.dialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.ContextThemeWrapper;

import com.arcao.geocaching4locus.R;

public final class DownloadProgressDialogFragment extends AbstractDialogFragment {
	public static final String TAG = DownloadProgressDialogFragment.class.getName();

	protected static final String PARAM_MESSAGE_ID = "MESSAGE_ID";
	protected static final String PARAM_COUNT = "COUNT";
	protected static final String PARAM_CURRENT = "CURRENT";

	public static DownloadProgressDialogFragment newInstance(int messageId, int count, int current) {
		DownloadProgressDialogFragment fragment = new DownloadProgressDialogFragment();

		Bundle args = new Bundle();
		args.putInt(PARAM_MESSAGE_ID, messageId);
		args.putInt(PARAM_COUNT, count);
		args.putInt(PARAM_CURRENT, current);
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setCancelable(false);
	}

	public void setProgress(int progress) {
		ProgressDialog pd = (ProgressDialog) getDialog();
		if (pd != null)
			pd.setProgress(progress);
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Bundle arguments = getArguments();

		ProgressDialog pd = new ProgressDialog(new ContextThemeWrapper(getActivity(), R.style.G4LTheme_Dialog));

		pd.setButton(ProgressDialog.BUTTON_NEGATIVE, getText(R.string.cancel_button), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				callOnCancelListener(DownloadProgressDialogFragment.this);
			}
		});

		int count = arguments.getInt(PARAM_COUNT);
		int current = arguments.getInt(PARAM_CURRENT);

		if (count < 0) {
			pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		} else {
			pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			pd.setMax(count);
			pd.setProgress(current);
		}
		pd.setMessage(getText(arguments.getInt(PARAM_MESSAGE_ID)));

		Log.d(TAG, "Creating ProgressDialog; count:" + count + "; current:" + current);

		return pd;
	}
}
