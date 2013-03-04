package com.arcao.geocaching4locus.fragment;

import java.lang.ref.WeakReference;

import locus.api.objects.extra.Waypoint;

import org.apache.commons.lang3.tuple.Pair;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextThemeWrapper;

import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.task.UpdateTask;
import com.arcao.geocaching4locus.task.UpdateTask.OnTaskFinishedListener;
import com.arcao.geocaching4locus.task.UpdateTask.UpdateTaskData;

public final class UpdateDialogFragment extends AbstractDialogFragment implements OnTaskFinishedListener {
	public static final String TAG = UpdateDialogFragment.class.getName();

	protected UpdateTask mTask;
	protected UpdateTaskData data = new UpdateTaskData();
	protected WeakReference<OnTaskFinishedListener> taskFinishedListenerRef;

	public static UpdateDialogFragment newInstance(String cacheId, Waypoint oldPoint) {
		UpdateDialogFragment fragment = new UpdateDialogFragment();
		fragment.mTask = new UpdateTask();
		fragment.data.cache = Pair.of(cacheId, oldPoint);

		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRetainInstance(true);
		setCancelable(false);

		mTask.setOnTaskFinishedListener(this);
		mTask.execute(data);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			taskFinishedListenerRef = new WeakReference<OnTaskFinishedListener>((OnTaskFinishedListener) activity);
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnTaskFinishListener");
		}
	}
	

	@Override
	public void onTaskFinished(Intent intent) {
		mTask = null;

		dismiss();
		
		OnTaskFinishedListener taskFinishedListener = taskFinishedListenerRef.get();
		if (taskFinishedListener != null) {
			taskFinishedListener.onTaskFinished(intent);
		}
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		if (mTask != null) {
			mTask.cancel(false);
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		ProgressDialog dialog = new ProgressDialog(new ContextThemeWrapper(getActivity(), R.style.G4LTheme_Dialog));
		dialog.setIndeterminate(true);
		dialog.setMessage(getText(R.string.update_cache_progress));
		dialog.setButton(ProgressDialog.BUTTON_NEGATIVE, getText(R.string.cancel_button), (OnClickListener) null);
		return dialog;
	}
}
