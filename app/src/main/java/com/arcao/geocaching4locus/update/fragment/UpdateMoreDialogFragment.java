package com.arcao.geocaching4locus.update.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.afollestad.materialdialogs.MaterialDialog;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.base.fragment.AbstractDialogFragment;
import com.arcao.geocaching4locus.update.task.UpdateMoreTask;
import com.arcao.geocaching4locus.update.task.UpdateMoreTask.TaskListener;

import org.apache.commons.lang3.ArrayUtils;

import java.lang.ref.WeakReference;

public final class UpdateMoreDialogFragment extends AbstractDialogFragment implements TaskListener {
    public static final String FRAGMENT_TAG = UpdateMoreDialogFragment.class.getName();

    private static final String PARAM_POINT_INDEXES = "POINT_INDEXES";

    public interface DialogListener {
        void onUpdateFinished(boolean success);
    }

    @Nullable private UpdateMoreTask task;
    private WeakReference<DialogListener> dialogListenerRef;

    public static UpdateMoreDialogFragment newInstance(long[] pointIndexes) {
        Bundle args = new Bundle();
        args.putLongArray(PARAM_POINT_INDEXES, pointIndexes);

        UpdateMoreDialogFragment fragment = new UpdateMoreDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        setCancelable(false);

        long[] pointIndexes = getArguments().getLongArray(PARAM_POINT_INDEXES);

        task = new UpdateMoreTask(getActivity(), this);
        task.execute(pointIndexes);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            dialogListenerRef = new WeakReference<>((DialogListener) activity);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnTaskFinishedListener");
        }
    }

    @Override
    public void onTaskFinished(boolean success) {
        dismiss();

        DialogListener listener = dialogListenerRef.get();
        if (listener != null) {
            listener.onUpdateFinished(success);
        }
    }

    @Override
    public void onProgressUpdate(int count) {
        MaterialDialog dialog = (MaterialDialog) getDialog();
        if (dialog != null) {
            dialog.setProgress(count);
        }

    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (task != null) {
            task.cancel(true);
            task = null;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        long[] pointIndexes = getArguments().getLongArray(PARAM_POINT_INDEXES);

        return new MaterialDialog.Builder(getActivity())
                .content(R.string.progress_update_geocaches)
                .negativeText(R.string.button_cancel)
                .progress(false, ArrayUtils.getLength(pointIndexes), true)
                .build();
    }
}
