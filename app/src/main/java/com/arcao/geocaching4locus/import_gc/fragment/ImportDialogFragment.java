package com.arcao.geocaching4locus.import_gc.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.afollestad.materialdialogs.MaterialDialog;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.base.fragment.AbstractDialogFragment;
import com.arcao.geocaching4locus.import_gc.task.ImportTask;
import com.arcao.geocaching4locus.import_gc.task.ImportTask.TaskListener;

import java.lang.ref.WeakReference;

public final class ImportDialogFragment extends AbstractDialogFragment implements TaskListener {
	public static final String FRAGMENT_TAG = ImportDialogFragment.class.getName();

	private static final String PARAM_CACHE_IDS = "CACHE_IDS";

	public interface DialogListener {
		void onImportFinished(@Nullable Intent intent);
		void onImportError(@NonNull Intent intent);
	}

	@Nullable private ImportTask mTask;
	private WeakReference<DialogListener> mDialogListenerRef;
	private String[] cacheIds;

	public static ImportDialogFragment newInstance(String[] cacheIds) {
		Bundle args = new Bundle();
		args.putStringArray(PARAM_CACHE_IDS, cacheIds);

		ImportDialogFragment fragment = new ImportDialogFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRetainInstance(true);
		setCancelable(false);

		cacheIds = getArguments().getStringArray(PARAM_CACHE_IDS);

		mTask = new ImportTask(getActivity(), this);
		mTask.execute(cacheIds);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mDialogListenerRef = new WeakReference<>((DialogListener) activity);
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement DialogListener");
		}
	}

	@Override
	public void onTaskFinished(@Nullable Intent intent) {
		dismiss();

		DialogListener listener = mDialogListenerRef.get();
		if (listener != null) {
			listener.onImportFinished(intent);
		}
	}

	@Override
	public void onTaskError(@NonNull Intent errorIntent) {
		dismiss();

		DialogListener listener = mDialogListenerRef.get();
		if (listener != null) {
			listener.onImportError(errorIntent);
		}
	}

	@Override
	public void onProgressUpdate(int current, int count) {
		MaterialDialog dialog = (MaterialDialog) getDialog();
		if (dialog != null) {
			dialog.setProgress(current);
		}
	}


	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		if (mTask != null) {
			mTask.cancel(true);
			mTask = null;
		}
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new MaterialDialog.Builder(getActivity())
						.content(cacheIds.length > 1 ? R.string.progress_download_geocaches : R.string.progress_download_geocache)
						.negativeText(R.string.button_cancel)
						.progress(false, cacheIds.length, true)
						.build();
	}
}
