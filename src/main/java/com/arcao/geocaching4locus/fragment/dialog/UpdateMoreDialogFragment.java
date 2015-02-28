package com.arcao.geocaching4locus.fragment.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.task.UpdateMoreTask;
import com.arcao.geocaching4locus.task.UpdateMoreTask.TaskListener;

import java.lang.ref.WeakReference;

public final class UpdateMoreDialogFragment extends AbstractDialogFragment implements TaskListener {
	public static final String FRAGMENT_TAG = UpdateMoreDialogFragment.class.getName();

	private static final String PARAM_POINT_INDEXES = "POINT_INDEXES";

	public interface DialogListener {
		void onUpdateFinished(boolean success);
	}

	private UpdateMoreTask mTask;
	private WeakReference<DialogListener> mDialogListenerRef;

	public static UpdateMoreDialogFragment newInstance(long[] pointIndexes) {
		UpdateMoreDialogFragment fragment = new UpdateMoreDialogFragment();

		Bundle args = new Bundle();
		args.putLongArray(PARAM_POINT_INDEXES, pointIndexes);
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRetainInstance(true);
		setCancelable(false);

		long[] pointIndexes = getArguments().getLongArray(PARAM_POINT_INDEXES);

		mTask = new UpdateMoreTask(getActivity(), this);
		mTask.execute(pointIndexes);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mDialogListenerRef = new WeakReference<>((DialogListener) activity);
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnTaskFinishedListener");
		}
	}

	@Override
	public void onTaskFinished(boolean success) {
		mTask = null;

		dismiss();

		DialogListener listener = mDialogListenerRef.get();
		if (listener != null) {
			listener.onUpdateFinished(success);
		}
	}

	@Override
	public void onProgressUpdate(int count) {
		ProgressDialog dialog = (ProgressDialog) getDialog();
		if (dialog != null) {
			dialog.setProgress(count);
		}

	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		if (mTask != null) {
			mTask.cancel(true);
		}
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		long[] pointIndexes = getArguments().getLongArray(PARAM_POINT_INDEXES);

		ProgressDialog dialog = new ProgressDialog(getActivity());
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setMax(pointIndexes.length);
		dialog.setMessage(getText(R.string.update_caches_progress));
		dialog.setButton(ProgressDialog.BUTTON_NEGATIVE, getText(R.string.cancel_button), (OnClickListener) null);
		return dialog;
	}
}
