package com.arcao.geocaching4locus.search_nearest.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.afollestad.materialdialogs.MaterialDialog;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.base.fragment.AbstractDialogFragment;
import com.arcao.geocaching4locus.search_nearest.task.DownloadNearestTask;

import java.lang.ref.WeakReference;

public class DownloadNearestDialogFragment extends AbstractDialogFragment implements DownloadNearestTask.TaskListener {
    public static final String FRAGMENT_TAG = DownloadNearestDialogFragment.class.getName();

    private static final String PARAM_LATITUDE = "LATITUDE";
    private static final String PARAM_LONGITUDE = "LONGITUDE";
    private static final String PARAM_COUNT = "COUNT";
    private int count;

    public interface DialogListener {
        void onDownloadFinished(Intent intent);
        void onDownloadError(Intent intent);
    }

    @Nullable
    private DownloadNearestTask task;
    private WeakReference<DialogListener> dialogListenerRef;

    public static DownloadNearestDialogFragment newInstance(double latitude, double longitude, int count) {
        Bundle args = new Bundle();
        args.putDouble(PARAM_LATITUDE, latitude);
        args.putDouble(PARAM_LONGITUDE, longitude);
        args.putInt(PARAM_COUNT, count);

        DownloadNearestDialogFragment fragment = new DownloadNearestDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        setCancelable(false);

        Bundle args = getArguments();
        double latitude = args.getDouble(PARAM_LATITUDE);
        double longitude = args.getDouble(PARAM_LONGITUDE);
        count = args.getInt(PARAM_COUNT);

        task = new DownloadNearestTask(getActivity(), this, latitude, longitude, count);
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
    public void onTaskFinish(Intent intent) {
        dismiss();

        DialogListener listener = dialogListenerRef.get();
        if (listener != null) listener.onDownloadFinished(intent);
    }

    @Override
    public void onTaskError(@NonNull Intent intent) {
        dismiss();

        DialogListener listener = dialogListenerRef.get();
        if (listener != null) listener.onDownloadError(intent);
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
                .progress(false, count, true)
                .negativeText(R.string.button_cancel)
                .build();
    }

}
