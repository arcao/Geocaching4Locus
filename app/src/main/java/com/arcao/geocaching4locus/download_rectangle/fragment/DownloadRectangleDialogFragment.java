package com.arcao.geocaching4locus.download_rectangle.fragment;

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
import com.arcao.geocaching4locus.download_rectangle.task.DownloadRectangleTask;
import java.lang.ref.WeakReference;

public class DownloadRectangleDialogFragment extends AbstractDialogFragment implements DownloadRectangleTask.TaskListener {
  public static final String FRAGMENT_TAG = DownloadRectangleDialogFragment.class.getName();

  private static final String PARAM_TOP_LEFT_COORDINATES = "TOP_LEFT_COORDINATES";
  private static final String PARAM_BOTTOM_RIGHT_COORDINATES = "BOTTOM_RIGHT_COORDINATES";

  public interface DialogListener {
    void onDownloadFinished(Intent intent);
    void onDownloadError(Intent errorIntent);
  }

  @Nullable private DownloadRectangleTask mTask;
  private WeakReference<DialogListener> mDialogListenerRef;

  public static DownloadRectangleDialogFragment newInstance() {
    Bundle args = new Bundle();

    DownloadRectangleDialogFragment fragment = new DownloadRectangleDialogFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @SuppressWarnings("ConstantConditions")
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setRetainInstance(true);
    setCancelable(false);

    mTask = new DownloadRectangleTask(getActivity(), this);
    mTask.execute();
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);

    try {
      mDialogListenerRef = new WeakReference<>((DialogListener) activity);
    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString() + " must implement DialogListener");
    }
  }


  @Override
  public void onTaskFinished(Intent intent) {
    dismiss();

    DialogListener listener = mDialogListenerRef.get();
    if (listener != null) {
      listener.onDownloadFinished(intent);
    }
  }

  @Override
  public void onTaskError(@NonNull Intent errorIntent) {
    dismiss();

    DialogListener listener = mDialogListenerRef.get();
    if (listener != null) {
      listener.onDownloadError(errorIntent);
    }
  }

  @Override
  public void onDismiss(DialogInterface dialog) {
    super.onDismiss(dialog);
    if (mTask != null) {
      mTask.cancel(true);
      mTask = null;
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
