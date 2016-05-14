package com.arcao.geocaching4locus.import_gc.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
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

	private static final String PARAM_CACHE_ID = "CACHE_ID";

	public interface DialogListener {
		void onImportFinished(boolean success);
	}

	@Nullable private ImportTask mTask;
	private WeakReference<DialogListener> mDialogListenerRef;

	public static ImportDialogFragment newInstance(String cacheId) {
		Bundle args = new Bundle();
		args.putString(PARAM_CACHE_ID, cacheId);

		ImportDialogFragment fragment = new ImportDialogFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRetainInstance(true);
		setCancelable(false);

		String cacheId = getArguments().getString(PARAM_CACHE_ID);

		mTask = new ImportTask(getActivity(), this);
		mTask.execute(cacheId);
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
	public void onTaskFinished(boolean success) {
		dismiss();

		DialogListener listener = mDialogListenerRef.get();
		if (listener != null) {
			listener.onImportFinished(success);
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
						.content(R.string.import_cache_progress)
						.negativeText(R.string.cancel_button)
						.progress(true, 0)
						.build();
	}
}
