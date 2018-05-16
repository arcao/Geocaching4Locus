package com.arcao.geocaching4locus.base.fragment;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;

public abstract class AbstractDialogFragment extends DialogFragment {
    private static final String PARAM_DISMISS_LATER = "DISMISS_LATER";

    public AbstractDialogFragment() {
        try {
            if (getArguments() == null)
                setArguments(new Bundle());
        } catch (Exception e) {
            // do nothing
        }
    }

    // This is work around for the situation when method show is called after saving
    // state even if you do all right. Especially when show is called after click on
    // a button.
    @Override
    public int show(@NonNull FragmentTransaction transaction, String tag) {
        try {
            return super.show(transaction, tag);
        } catch (IllegalStateException e) {
            // ignore
            return 0;
        }
    }

    @Override
    public void show(@NonNull FragmentManager manager, String tag) {
        try {
            super.show(manager, tag);
        } catch (IllegalStateException e) {
            // ignore
        }
    }

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
            if (getArguments() != null)
                getArguments().putBoolean(PARAM_DISMISS_LATER, true);

            return;
        }

        try {
            super.dismiss();
        } catch (IllegalStateException e) {
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
}
