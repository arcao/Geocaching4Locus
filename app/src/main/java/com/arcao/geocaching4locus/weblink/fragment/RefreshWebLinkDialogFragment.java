package com.arcao.geocaching4locus.weblink.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.afollestad.materialdialogs.MaterialDialog;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.base.fragment.AbstractDialogFragment;
import com.arcao.geocaching4locus.weblink.task.RefreshWebLinkTask;

import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import locus.api.objects.extra.Point;

public final class RefreshWebLinkDialogFragment extends AbstractDialogFragment implements RefreshWebLinkTask.TaskListener {
    public static final String FRAGMENT_TAG = RefreshWebLinkDialogFragment.class.getName();

    private static final String PARAM_CACHE_ID = "CACHE_ID";

    public interface DialogListener {
        void onRefreshFinished(Point point);
        void onRefreshError(Intent intent);
    }

    @Nullable
    private RefreshWebLinkTask task;
    private WeakReference<DialogListener> dialogListenerRef;

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

        task = new RefreshWebLinkTask(getActivity(), this);
        task.execute(cacheId);
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
    public void onTaskFinish(Point point) {
        dismiss();

        DialogListener listener = dialogListenerRef.get();
        if (listener != null) listener.onRefreshFinished(point);
    }

    @Override
    public void onTaskError(Intent intent) {
        dismiss();

        DialogListener listener = dialogListenerRef.get();
        if (listener != null) listener.onRefreshError(intent);
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
        return new MaterialDialog.Builder(getActivity())
                .content(R.string.progress_download_geocache)
                .negativeText(R.string.button_cancel)
                .progress(true, 0)
                .build();
    }
}
