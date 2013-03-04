package com.arcao.geocaching4locus.fragment;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.ContextThemeWrapper;

import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.task.UpdateMoreTask;
import com.arcao.geocaching4locus.task.UpdateMoreTask.OnTaskFinishedListener;

public final class UpdateMoreDialogFragment extends AbstractDialogFragment implements OnTaskFinishedListener {
	public static final String TAG = UpdateMoreDialogFragment.class.getName();
			
	protected UpdateMoreTask mTask;
	protected long[] pointIndexes;
	protected WeakReference<OnTaskFinishedListener> taskFinishedListenerRef;
	
	public static UpdateMoreDialogFragment newInstance(long[] pointIndexes) {
		UpdateMoreDialogFragment fragment = new UpdateMoreDialogFragment();
		fragment.mTask = new UpdateMoreTask();
		fragment.pointIndexes = pointIndexes;
		return fragment;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setRetainInstance(true);
		setCancelable(false);
		
		mTask.setOnTaskUpdateListener(this);
		mTask.execute(pointIndexes);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		try {
			taskFinishedListenerRef = new WeakReference<OnTaskFinishedListener>((OnTaskFinishedListener) activity);
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnTaskFinishedListener");
		}
	}
		
	@Override
	public void onTaskFinished(boolean success) {
		mTask = null;
		
		dismiss();
		
		OnTaskFinishedListener taskFinishedListener = taskFinishedListenerRef.get();
		if (taskFinishedListener != null) {
			taskFinishedListener.onTaskFinished(success);
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
			mTask.cancel(false);
		}
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		ProgressDialog dialog = new ProgressDialog(new ContextThemeWrapper(getActivity(), R.style.G4LTheme_Dialog));
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setMax(pointIndexes.length);
		dialog.setMessage(getText(R.string.update_caches_progress));
		dialog.setButton(ProgressDialog.BUTTON_NEGATIVE, getText(R.string.cancel_button), (OnClickListener) null);
		return dialog;
	}	
}
