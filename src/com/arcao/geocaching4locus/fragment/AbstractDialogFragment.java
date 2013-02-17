package com.arcao.geocaching4locus.fragment;

import java.lang.ref.WeakReference;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

public abstract class AbstractDialogFragment extends DialogFragment {
	protected WeakReference<CancellableDialog> cancellableRef;
	
  // This is to work around what is apparently a bug. If you don't have it
  // here the dialog will be dismissed on rotation, so tell it not to dismiss.
  @Override
  public void onDestroyView()
  {
      if (getDialog() != null && getRetainInstance())
          getDialog().setDismissMessage(null);
      super.onDestroyView();
  }
	
	@Override
	public void show(FragmentManager manager, String tag) {
		// DialogFragment.show() will take care of adding the fragment
		// in a transaction.  We also want to remove any currently showing
		// dialog, so make our own transaction and take care of that here.
		Fragment prev = manager.findFragmentByTag(tag);
		if (prev != null) {
			return;
		}
		
		super.show(manager, tag);
	}

	@Override
	public void dismiss() {
		// this fix IllegalStateException when App is hidden
		dismissAllowingStateLoss();
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
		cancellableRef = new WeakReference<CancellableDialog>(cancellableDialog);
	}
	
	public abstract static interface CancellableDialog {
		abstract void onCancel(AbstractDialogFragment dialogFragment);
	}
}
