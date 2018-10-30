package com.arcao.geocaching4locus.base.fragment

import android.os.Bundle

import androidx.annotation.NonNull
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction

abstract class AbstractDialogFragment : DialogFragment() {
    val isShowing: Boolean
        get() = dialog != null && dialog.isShowing

    fun requireArguments(): Bundle {
        if (arguments == null) {
            throw IllegalStateException("Fragment " + this + " does not contains arguments.")
        }
        return arguments!!
    }

    // This is work around for the situation when method show is called after saving
    // state even if you do all right. Especially when show is called after click on
    // a button.
    override fun show(@NonNull transaction: FragmentTransaction, tag: String): Int {
        return try {
            super.show(transaction, tag)
        } catch (e: IllegalStateException) {
            // ignore
            0
        }
    }

    override fun show(@NonNull manager: FragmentManager, tag: String) {
        try {
            super.show(manager, tag)
        } catch (e: IllegalStateException) {
            // ignore
        }
    }

    // This is to work around what is apparently a bug. If you don't have it
    // here the dialog will be dismissed on rotation, so tell it not to dismiss.
    override fun onDestroyView() {
        if (dialog != null && retainInstance)
            dialog.setDismissMessage(null)

        super.onDestroyView()
    }

    override fun dismiss() {
        try {
            super.dismiss()
        } catch (e: IllegalStateException) {
            dismissAllowingStateLoss()
        }
    }
}
