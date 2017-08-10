package com.arcao.geocaching4locus.weblink.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.afollestad.materialdialogs.MaterialDialog;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.base.fragment.AbstractDialogFragment;
import com.arcao.geocaching4locus.weblink.task.RefreshWebLinkTask;
import java.lang.ref.WeakReference;
import locus.api.objects.extra.Waypoint;

public final class RefreshWebLinkDialogFragment extends AbstractDialogFragment implements RefreshWebLinkTask.TaskListener {
	public static final String FRAGMENT_TAG = RefreshWebLinkDialogFragment.class.getName();

	private static final String PARAM_CACHE_ID = "CACHE_ID";

	public interface DialogListener {
		void onRefreshFinished(Waypoint waypoint);
	}

	@Nullable private RefreshWebLinkTask mTask;
	private WeakReference<DialogListener> mDialogListenerRef;

	public static RefreshWebLinkDialogFragment newInstance(String cacheId) {
		Bundle args = new Bundle();
		args.putString(PARAM_CACHE_ID, cacheId);

		RefreshWebLinkDialogFragment fragment = new RefreshWebLinkDialogFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRetainInstance(true);
		setCancelable(false);

		String cacheId = getArguments().getString(PARAM_CACHE_ID);

		mTask = new RefreshWebLinkTask(getActivity(), this);
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
	public void onTaskFinished(Waypoint waypoint) {
		dismiss();

		DialogListener listener = mDialogListenerRef.get();
		if (listener != null) {
			listener.onRefreshFinished(waypoint);
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
						.content(R.string.progress_import_geocache)
						.negativeText(R.string.button_cancel)
						.progress(true, 0)
						.build();
	}
}
