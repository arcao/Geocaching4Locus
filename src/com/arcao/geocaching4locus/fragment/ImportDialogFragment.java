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
import com.arcao.geocaching4locus.task.ImportTask;
import com.arcao.geocaching4locus.task.ImportTask.OnTaskFinishedListener;

public class ImportDialogFragment extends AbstractDialogFragment implements OnTaskFinishedListener {
	public static final String TAG = ImportDialogFragment.class.getName();
	
	protected String cacheId;
	protected ImportTask mTask;
	protected WeakReference<OnTaskFinishedListener> taskFinishedListenerRef;
	
	public static ImportDialogFragment newInstance(String cacheId) {
		ImportDialogFragment fragment = new ImportDialogFragment();
		fragment.cacheId = cacheId;
		fragment.mTask = new ImportTask();
		
		return fragment;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setRetainInstance(true);
		
		mTask.setOnTaskFinishedListener(this);
		mTask.execute(cacheId);
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
		dialog.setCancelable(false);
		dialog.setMessage(getText(R.string.import_cache_progress));
		dialog.setButton(ProgressDialog.BUTTON_NEGATIVE, getText(R.string.cancel_button), (OnClickListener) null);
		return dialog;
	}	
}
