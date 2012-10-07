package com.arcao.geocaching4locus.fragment;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public abstract class CustomDialogFragment extends DialogFragment {
	public String createTag() {
		return getClass().getName();
	}
	
	public void show(FragmentManager manager) {
		super.show(manager, createTag());
	}
	
	@Override
	public void show(FragmentManager manager, String tag) {
		// DialogFragment.show() will take care of adding the fragment
		// in a transaction.  We also want to remove any currently showing
		// dialog, so make our own transaction and take care of that here.
		FragmentTransaction ft = manager.beginTransaction();
		Fragment prev = manager.findFragmentByTag(tag);
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);		
		
		super.show(manager, tag);
	}
	
	public boolean isShowing() {
		return getDialog() != null && getDialog().isShowing();
	}
	
	public abstract static interface Cancellable<F extends CustomDialogFragment> {
		abstract void onCancel(F customDialogFragment);
	}
}
