package com.arcao.geocaching4locus.fragment;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import java.lang.ref.WeakReference;

public abstract class AbstractDialogFragment extends DialogFragment {
	private static final String PARAM_DISMISS_LATER = "DISMISS_LATER";

	protected WeakReference<CancellableDialog> cancellableRef;

	// This is to work around what is apparently a bug. If you don't have it
	// here the dialog will be dismissed on rotation, so tell it not to dismiss.
	@Override
	public void onDestroyView() {
			if (getDialog() != null && getRetainInstance())
					getDialog().setDismissMessage(null);

			super.onDestroyView();
	}

	@Override
	public void dismiss() {
		// this fix IllegalStateException when App is hidden
		if (!isAdded() || getFragmentManager() == null) {
			if (getArguments() == null)
				setArguments(new Bundle());

			getArguments().putBoolean(PARAM_DISMISS_LATER, true);
			return;
		}

		try {
			super.dismiss();
		} catch(IllegalStateException e) {
			dismissAllowingStateLoss();
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		if (getArguments() != null && getArguments().getBoolean(PARAM_DISMISS_LATER, false)) {
			dismiss();
		}
	}

	public boolean isShowing() {
		return getDialog() != null && getDialog().isShowing();
	}

	public void callOnCancelListener(AbstractDialogFragment dialogFragment) {
		if (cancellableRef == null)
			return;

		CancellableDialog listener = cancellableRef.get();
		if (listener != null) {
			listener.onCancel(dialogFragment);
		}
	}

	public void setOnCancelListener(CancellableDialog cancellableDialog) {
		cancellableRef = new WeakReference<>(cancellableDialog);
	}

	public abstract static interface CancellableDialog {
		abstract void onCancel(AbstractDialogFragment dialogFragment);
	}
}
