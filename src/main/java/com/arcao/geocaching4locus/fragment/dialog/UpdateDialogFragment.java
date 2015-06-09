package com.arcao.geocaching4locus.fragment.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.afollestad.materialdialogs.MaterialDialog;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.task.UpdateTask;
import com.arcao.geocaching4locus.task.UpdateTask.UpdateTaskData;

import java.lang.ref.WeakReference;

import locus.api.objects.extra.Waypoint;

public final class UpdateDialogFragment extends AbstractDialogFragment implements UpdateTask.TaskListener {
	public static final String FRAGMENT_TAG = UpdateDialogFragment.class.getName();

	private static final String PARAM_UPDATE_DATA = "UPDATE_DATA";

	public interface DialogListener {
		void onUpdateFinished(Intent result);
	}

	private UpdateTask mTask;
	private WeakReference<DialogListener> mDialogListenerRef;

	public static UpdateDialogFragment newInstance(String cacheId, Waypoint oldPoint, boolean updateLogs) {
		UpdateDialogFragment fragment = new UpdateDialogFragment();

		Bundle args = new Bundle();
		args.putSerializable(PARAM_UPDATE_DATA, new UpdateTaskData(cacheId, oldPoint, updateLogs));
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRetainInstance(true);
		setCancelable(false);

		UpdateTaskData data = (UpdateTaskData) getArguments().getSerializable(PARAM_UPDATE_DATA);

		mTask = new UpdateTask(getActivity(), this);
		mTask.execute(data);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mDialogListenerRef = new WeakReference<>((DialogListener) activity);
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnTaskFinishListener");
		}
	}


	@Override
	public void onTaskFinished(Intent intent) {
		mTask = null;

		dismiss();

		DialogListener listener = mDialogListenerRef.get();
		if (listener != null) {
			listener.onUpdateFinished(intent);
		}
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		if (mTask != null) {
			mTask.cancel(true);
		}
	}

	@Override
	public void onUpdateState(State state, int progress, int max) {
		MaterialDialog dialog = (MaterialDialog) getDialog();
		if (dialog != null) {
			updateDialog(state, progress, max, dialog);
		}
	}

	private void updateDialog(State state, int progress, int max, MaterialDialog dialog) {
		switch (state) {
			case CACHE:
				dialog.setContent(R.string.update_cache_progress);
				dialog.setProgress(-1);
				dialog.setMaxProgress(1);
				break;
			case LOGS:
				dialog.setContent(R.string.download_logs_progress);
				dialog.setMaxProgress(max);
				dialog.setProgress(progress);
				break;
		}
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
						.progress(false, -1)
						.negativeText(R.string.cancel_button)
						.build();

		updateDialog(State.CACHE, 0, 0, dialog);
		return dialog;
	}
}
