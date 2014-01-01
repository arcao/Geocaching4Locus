package com.arcao.geocaching4locus.fragment;

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

import java.lang.ref.WeakReference;

public final class ImportDialogFragment extends AbstractDialogFragment implements OnTaskFinishedListener {
	public static final String TAG = ImportDialogFragment.class.getName();

	public static final String PARAM_CACHE_ID = "CACHE_ID";

	protected ImportTask mTask;
	protected WeakReference<OnTaskFinishedListener> taskFinishedListenerRef;

	public static ImportDialogFragment newInstance(String cacheId) {
		ImportDialogFragment fragment = new ImportDialogFragment();

		Bundle args = new Bundle();
		args.putString(PARAM_CACHE_ID, cacheId);

		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRetainInstance(true);
		setCancelable(false);

		String cacheId = getArguments().getString(PARAM_CACHE_ID);

		mTask = new ImportTask();
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
			mTask.cancel(true);
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		ProgressDialog dialog = new ProgressDialog(new ContextThemeWrapper(getActivity(), R.style.G4LTheme_Dialog));
		dialog.setIndeterminate(true);
		dialog.setMessage(getText(R.string.import_cache_progress));
		dialog.setButton(ProgressDialog.BUTTON_NEGATIVE, getText(R.string.cancel_button), (OnClickListener) null);
		return dialog;
	}
}
