package com.arcao.geocaching4locus.download_rectangle.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.afollestad.materialdialogs.MaterialDialog;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.base.fragment.AbstractDialogFragment;
import com.arcao.geocaching4locus.download_rectangle.task.DownloadRectangleTask;

import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DownloadRectangleDialogFragment extends AbstractDialogFragment implements DownloadRectangleTask.TaskListener {

    public interface DialogListener {
        void onDownloadFinished(Intent intent);

        void onDownloadError(Intent errorIntent);
    }

    @Nullable private DownloadRectangleTask task;
    private WeakReference<DialogListener> dialogListenerRef;

    public static DownloadRectangleDialogFragment newInstance() {
        Bundle args = new Bundle();

        DownloadRectangleDialogFragment fragment = new DownloadRectangleDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        setCancelable(false);

        task = new DownloadRectangleTask(getActivity(), this);
        task.execute();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            dialogListenerRef = new WeakReference<>((DialogListener) activity);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement DialogListener");
        }
    }


    @Override
    public void onTaskFinished(Intent intent) {
        dismiss();

        DialogListener listener = dialogListenerRef.get();
        if (listener != null) {
            listener.onDownloadFinished(intent);
        }
    }

    @Override
    public void onTaskError(@NonNull Intent errorIntent) {
        dismiss();

        DialogListener listener = dialogListenerRef.get();
        if (listener != null) {
            listener.onDownloadError(errorIntent);
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

    @Override
    public void onProgressUpdate(int progress, int maxProgress) {
        MaterialDialog dialog = (MaterialDialog) getDialog();
        if (dialog != null) {
            dialog.setMaxProgress(maxProgress);
            dialog.setProgress(progress);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new MaterialDialog.Builder(getActivity())
                .content(R.string.progress_download_geocaches)
                .progress(false, 0, true)
                .negativeText(R.string.button_cancel)
                .build();
    }

}
