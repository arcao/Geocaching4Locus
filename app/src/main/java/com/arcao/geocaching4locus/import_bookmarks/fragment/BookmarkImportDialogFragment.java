package com.arcao.geocaching4locus.import_bookmarks.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.afollestad.materialdialogs.MaterialDialog;
import com.arcao.geocaching.api.data.bookmarks.BookmarkList;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.base.fragment.AbstractDialogFragment;
import com.arcao.geocaching4locus.import_gc.fragment.ImportDialogFragment;
import com.arcao.geocaching4locus.import_bookmarks.task.BookmarkImportTask;

import org.apache.commons.lang3.ArrayUtils;

import java.lang.ref.WeakReference;

public class BookmarkImportDialogFragment extends AbstractDialogFragment implements BookmarkImportTask.TaskListener {
    public static final String FRAGMENT_TAG = ImportDialogFragment.class.getName();

    private static final String PARAM_GUID = "GUID";
    private static final String PARAM_COUNT = "COUNT";
    private static final String PARAM_GEOCACHE_CODES = "GEOCACHE_CODES";

    public interface DialogListener {
        void onImportFinished(Intent errorIntent);
    }

    @Nullable private BookmarkImportTask task;
    private WeakReference<DialogListener> dialogListenerRef;
    private int count;

    public static BookmarkImportDialogFragment newInstance(BookmarkList bookmarkList) {
        Bundle args = new Bundle();
        args.putString(PARAM_GUID, bookmarkList.guid());
        args.putInt(PARAM_COUNT, bookmarkList.itemCount());

        BookmarkImportDialogFragment fragment = new BookmarkImportDialogFragment();
        fragment.setArguments(args);

        return fragment;
    }

    public static BookmarkImportDialogFragment newInstance(String[] geocacheCodes) {
        Bundle args = new Bundle();
        args.putStringArray(PARAM_GEOCACHE_CODES, geocacheCodes);
        args.putInt(PARAM_COUNT, geocacheCodes.length);

        BookmarkImportDialogFragment fragment = new BookmarkImportDialogFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        setCancelable(false);

        Bundle args = getArguments();

        String guid = args.getString(PARAM_GUID);
        count = args.getInt(PARAM_COUNT);

        String[] values = guid != null ? ArrayUtils.toArray(guid) : args.getStringArray(PARAM_GEOCACHE_CODES);

        task = new BookmarkImportTask(getActivity(), this);
        task.execute(values);
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
    public void onTaskFailed(Intent errorIntent) {
        dismiss();

        DialogListener listener = dialogListenerRef.get();
        if (listener != null) {
            listener.onImportFinished(errorIntent);
        }
    }

    @Override
    public void onProgressUpdate(int count, int max) {
        MaterialDialog dialog = (MaterialDialog) getDialog();
        if (dialog != null) {
            dialog.setMaxProgress(max);
            dialog.setProgress(count);
        }
    }

    @Override
    public void onTaskFinished() {
        dismiss();

        DialogListener listener = dialogListenerRef.get();
        if (listener != null) {
            listener.onImportFinished(null);
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
        return new MaterialDialog.Builder(getActivity())
                .content(R.string.progress_download_geocaches)
                .negativeText(R.string.button_cancel)
                .progress(false, count, true)
                .build();
    }
}
