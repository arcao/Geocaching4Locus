package com.arcao.geocaching4locus.fragment.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.afollestad.materialdialogs.MaterialDialog;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.task.BookmarkImportTask;

import java.lang.ref.WeakReference;
import org.apache.commons.lang3.ArrayUtils;

public class BookmarkImportDialogFragment extends AbstractDialogFragment implements BookmarkImportTask.TaskListener {
	public static final String FRAGMENT_TAG = ImportDialogFragment.class.getName();

	private static final String PARAM_GUID = "GUID";
	private static final String PARAM_GEOCACHE_CODES = "GEOCACHE_CODES";

	public interface DialogListener {
		void onImportFinished(Intent errorIntent);
	}

	private BookmarkImportTask mTask;
	private WeakReference<DialogListener> mDialogListenerRef;

	public static BookmarkImportDialogFragment newInstance(String guid) {
		Bundle args = new Bundle();
		args.putString(PARAM_GUID, guid);

		BookmarkImportDialogFragment fragment = new BookmarkImportDialogFragment();
		fragment.setArguments(args);

		return fragment;
	}

	public static BookmarkImportDialogFragment newInstance(String[] geocacheCodes) {
		Bundle args = new Bundle();
		args.putStringArray(PARAM_GEOCACHE_CODES, geocacheCodes);

		BookmarkImportDialogFragment fragment = new BookmarkImportDialogFragment();
		fragment.setArguments(args);

		return fragment;
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRetainInstance(true);
		setCancelable(false);

		String guid = getArguments().getString(PARAM_GUID);
		String[] values = guid != null ? ArrayUtils.toArray(guid) : getArguments().getStringArray(PARAM_GEOCACHE_CODES);

		mTask = new BookmarkImportTask(getActivity(), this);
		mTask.execute(values);
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
	public void onTaskFailed(Intent errorIntent) {
		mTask = null;

		dismiss();

		DialogListener listener = mDialogListenerRef.get();
		if (listener != null) {
			listener.onImportFinished(errorIntent);
		}
	}

	@Override
	public void onProgressUpdate(int count, int max) {
		MaterialDialog dialog = (MaterialDialog) getDialog();
		if (dialog != null) {
			dialog.setMaxProgress(max);
			dialog.setProgress(count);
		}
	}


	@Override
	public void onTaskFinished(boolean success) {
		mTask = null;

		dismiss();

		DialogListener listener = mDialogListenerRef.get();
		if (listener != null) {
			listener.onImportFinished(null);
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
		return new MaterialDialog.Builder(getActivity())
						.content(R.string.import_caches_progress)
						.negativeText(R.string.cancel_button)
						.progress(false, 0)
						.build();
	}
}
