package com.arcao.geocaching4locus.authentication.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.authentication.task.OAuthLoginTask;
import com.arcao.geocaching4locus.base.util.IntentUtil;

import java.lang.ref.WeakReference;

public class OAuthLoginCompatFragment extends Fragment implements OAuthLoginTask.TaskListener {
    @Nullable
    OAuthLoginTask task;
    private WeakReference<OAuthLoginDialogListener> dialogListenerRef;

    public static OAuthLoginCompatFragment newInstance() {
        return new OAuthLoginCompatFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        // clear geocaching.com cookies
        App.get(getActivity()).clearGeocachingCookies();

        task = new OAuthLoginTask(getActivity(), this);
        task.execute();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            dialogListenerRef = new WeakReference<>((OAuthLoginDialogListener) activity);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnTaskFinishListener");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (task != null) {
            task.cancel(true);
            task = null;
        }
    }

    @Override
    public void onLoginUrlAvailable(@NonNull String url) {
        new MaterialDialog.Builder(getActivity())
                .content(R.string.warning_compat_android_sign_in, true)
                .negativeText(R.string.button_cancel)
                .positiveText(R.string.button_ok)
                .onPositive((dialog, which) -> {
                    showOAuthVerifierDialog();
                    IntentUtil.showWebPage(getActivity(), Uri.parse(url));
                })
                .onNegative((dialog, which) -> getActivity().finish())
                .cancelable(false)
                .show();
    }

    public void showOAuthVerifierDialog() {
        new MaterialDialog.Builder(getActivity())
                .input(R.string.hint_authorization_code, 0, false, (dialog, input) -> {
                    task = new OAuthLoginTask(getActivity(), this);
                    task.execute(input.toString());
                })
                .content(R.string.message_enter_authorization_code)
                .negativeText(R.string.button_cancel)
                .positiveText(R.string.button_ok)
                .onNegative((dialog, which) -> getActivity().finish())
                .cancelable(false)
                .show();
    }

    @Override
    public void onTaskFinished(Intent errorIntent) {
        OAuthLoginDialogListener listener = dialogListenerRef.get();
        if (listener != null) {
            listener.onLoginFinished(errorIntent);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login_oauth_compat, container, false);
    }
}
