package com.arcao.geocaching4locus.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.ContextThemeWrapper;

import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.task.UpdateTask;
import com.arcao.geocaching4locus.task.UpdateTask.UpdateTaskData;

import java.lang.ref.WeakReference;

import locus.api.objects.extra.Waypoint;

public final class UpdateDialogFragment extends AbstractDialogFragment implements UpdateTask.OnTaskListener {
	public static final String TAG = UpdateDialogFragment.class.getName();

	public static final String PARAM_UPDATE_DATA = "UPDATE_DATA";

	protected UpdateTask mTask;
	protected WeakReference<UpdateTask.OnTaskListener> taskFinishedListenerRef;

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

		mTask = new UpdateTask();
		mTask.setOnTaskListener(this);
		mTask.execute(data);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			taskFinishedListenerRef = new WeakReference<>((UpdateTask.OnTaskListener) activity);
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnTaskFinishListener");
		}
	}


	@Override
	public void onTaskFinished(Intent intent) {
		mTask = null;

		dismiss();

		UpdateTask.OnTaskListener taskFinishedListener = taskFinishedListenerRef.get();
		if (taskFinishedListener != null) {
			taskFinishedListener.onTaskFinished(intent);
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
	public void onUpdateState(State state, int progress) {
		ProgressDialog dialog = (ProgressDialog) getDialog();
		if (dialog != null) {
			updateDialog(state, progress, dialog);
		}
	}

	private void updateDialog(State state, int progress, ProgressDialog dialog) {
		dialog.setButton(ProgressDialog.BUTTON_NEGATIVE, getText(R.string.cancel_button), (OnClickListener) null);
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

		switch (state) {
			case CACHE:
				dialog.setMessage(getText(R.string.update_cache_progress));
				dialog.setIndeterminate(true);
				break;
			case LOGS:
				dialog.setMessage(getText(R.string.download_logs_progress));
				dialog.setMax(AppConstants.LOGS_TO_UPDATE_MAX);
				dialog.setProgress(progress);
				dialog.setIndeterminate(progress == 0);
				break;
		}
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		ProgressDialog dialog = new ProgressDialog(new ContextThemeWrapper(getActivity(), R.style.G4LTheme_Dialog));
		updateDialog(State.CACHE, 0, dialog);
		return dialog;
	}
}
