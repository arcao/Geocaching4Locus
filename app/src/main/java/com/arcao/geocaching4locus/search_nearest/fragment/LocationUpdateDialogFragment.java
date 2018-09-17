package com.arcao.geocaching4locus.search_nearest.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.MaterialDialog;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.base.fragment.AbstractDialogFragment;
import com.arcao.geocaching4locus.search_nearest.task.LocationUpdateTask;

import java.lang.ref.WeakReference;

public class LocationUpdateDialogFragment extends AbstractDialogFragment implements LocationUpdateTask.TaskListener {
    public static final String FRAGMENT_TAG = LocationUpdateDialogFragment.class.getName();

    public interface DialogListener {
        void onLocationUpdate(Location location);
    }

    @Nullable private LocationUpdateTask task;
    private WeakReference<DialogListener> dialogListenerRef;
    @NonNull private String provider = LocationManager.GPS_PROVIDER;

    public static LocationUpdateDialogFragment newInstance() {
        return new LocationUpdateDialogFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        setCancelable(false);

        task = new LocationUpdateTask(getActivity(), this);
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
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
                .content(R.string.progress_acquire_gps_location)
                .progress(true, 0)
                .negativeText(R.string.button_cancel);

        switch (provider) {
            case LocationManager.GPS_PROVIDER:
                builder.content(R.string.progress_acquire_gps_location);
                break;
            default:
                builder.content(R.string.progress_acquire_network_location);
                break;
        }

        return builder.build();
    }

    // ----------------- TaskListener methods ------------------
    @Override
    public void onTaskFinished(Location location) {
        dismiss();

        DialogListener listener = dialogListenerRef.get();
        if (listener != null) {
            listener.onLocationUpdate(location);
        }
    }

    @Override
    public void onProviderChanged(String provider) {
        this.provider = provider;

        MaterialDialog dialog = (MaterialDialog) getDialog();
        if (dialog == null)
            return;

        switch (provider) {
            case LocationManager.GPS_PROVIDER:
                dialog.setContent(R.string.progress_acquire_gps_location);
                break;
            default:
                dialog.setContent(R.string.progress_acquire_network_location);
                break;
        }
    }
}
