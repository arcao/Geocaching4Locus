package com.arcao.geocaching4locus.fragment.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.afollestad.materialdialogs.MaterialDialog;
import com.arcao.geocaching4locus.R;

import timber.log.Timber;

public final class DownloadProgressDialogFragment extends AbstractDialogFragment {
	public static final String FRAGMENT_TAG = DownloadProgressDialogFragment.class.getName();

	private static final String PARAM_MESSAGE_ID = "MESSAGE_ID";
	private static final String PARAM_COUNT = "COUNT";
	private static final String PARAM_CURRENT = "CURRENT";

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
		MaterialDialog pd = (MaterialDialog) getDialog();
		if (pd != null)
			pd.setProgress(progress);
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Bundle arguments = getArguments();

		final int count = arguments.getInt(PARAM_COUNT);
		final int current = arguments.getInt(PARAM_CURRENT);

		final MaterialDialog pd = new MaterialDialog.Builder(getActivity())
						.content(getText(arguments.getInt(PARAM_MESSAGE_ID)))
						.negativeText(R.string.cancel_button)
						.callback(new MaterialDialog.ButtonCallback() {
							@Override
							public void onNegative(MaterialDialog dialog) {
								callOnCancelListener(DownloadProgressDialogFragment.this);
							}
						})
						.progress(false, count, true)
						.build();

		if (count >= 0) {
			pd.setProgress(current);
		}

		Timber.d("Creating ProgressDialog; count:" + count + "; current:" + current);

		return pd;
	}
}
