package com.arcao.geocaching4locus.preview.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.arcao.geocaching.api.data.Geocache;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.base.fragment.AbstractDialogFragment;
import com.arcao.geocaching4locus.import_bookmarks.widget.decorator.SpacesItemDecoration;
import com.arcao.geocaching4locus.import_gc.ImportFromGCActivity;
import com.arcao.geocaching4locus.preview.adapter.OpenWithRecyclerAdapter;
import com.arcao.geocaching4locus.preview.model.ShortcutModel;
import com.arcao.geocaching4locus.preview.task.RetrieveGeocacheTask;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PreviewDialogFragment extends AbstractDialogFragment implements RetrieveGeocacheTask.TaskListener, OpenWithRecyclerAdapter.OnItemClickListener {
    public static final String FRAGMENT_TAG = PreviewDialogFragment.class.getName();

    private static final String PARAM_URI = "CACHE_URI";
    private static final String PARAM_CACHE_ID = "CACHE_ID";

    public interface DialogListener {
        void onClose();
    }

    WeakReference<DialogListener> mDialogListenerRef;

    @Nullable private RetrieveGeocacheTask mTask;
    private Geocache geocache;
    private OpenWithRecyclerAdapter openWithAdapter;

    @BindView(R.id.app_list) RecyclerView openWithView;

    //Bottom Sheet Callback
    private final BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();

                DialogListener listener = mDialogListenerRef.get();
                if (listener != null) {
                    listener.onClose();
                }
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
    };

    public static PreviewDialogFragment newInstance(String cacheId, Uri data) {
        Bundle args = new Bundle();
        args.putString(PARAM_CACHE_ID, cacheId);
        args.putParcelable(PARAM_URI, data);

        PreviewDialogFragment fragment = new PreviewDialogFragment();
        fragment.setCancelable(true);
        fragment.setArguments(args);
        return fragment;
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
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final BottomSheetDialog dialog = new BottomSheetDialog(getActivity(), getTheme());

        //Get the content View
        View contentView = View.inflate(getActivity(), R.layout.fragment_preview_bottom_sheet, null);
        dialog.setContentView(contentView);

        //Set the coordinator layout behavior
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        if (!(behavior instanceof BottomSheetBehavior))
            throw new IllegalStateException("Bottom sheet Behavior expected.");

        BottomSheetBehavior bottomSheetBehavior = (BottomSheetBehavior) behavior;

        //Set callback
        bottomSheetBehavior.setBottomSheetCallback(mBottomSheetBehaviorCallback);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        ButterKnife.bind(this, contentView);

        setupOpenWith();

        // fix width for larger screens like tablets
        dialog.setOnShowListener(dialogInterface -> {
            Window window = dialog.getWindow();
            if (window == null)
                return;

            View decorView = window.getDecorView();
            int width = decorView == null ? 0 : decorView.getWidth();
            int maxWidth = getResources().getDimensionPixelSize(R.dimen.bottom_sheet_max_width);
            if (width > maxWidth) {
                window.setLayout(maxWidth, ViewGroup.LayoutParams.MATCH_PARENT);
            }
        });

        return dialog;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String geocacheCode = getArguments().getString(PARAM_CACHE_ID);

        mTask = new RetrieveGeocacheTask(getActivity(), this);
        mTask.execute(geocacheCode);
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
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

        DialogListener listener = mDialogListenerRef.get();
        if (listener != null) {
            listener.onClose();
        }
    }

    @Override
    public void onTaskFinished(Geocache geocache) {
        this.geocache = geocache;

        if (getDialog() != null)
            setupPreview();
    }

    @Override
    public void onAppListItemClick(Intent intent) {
        getActivity().startActivity(intent);
        dismiss();

        DialogListener listener = mDialogListenerRef.get();
        if (listener != null) {
            listener.onClose();
        }
    }

    private void setupOpenWith() {
        Uri url = getArguments().getParcelable(PARAM_URI);
        Intent intent = new Intent(Intent.ACTION_VIEW, url);

        openWithAdapter = new OpenWithRecyclerAdapter(getActivity(), intent);
        openWithAdapter.setOnItemClickListener(this);

        openWithView.setAdapter(openWithAdapter);
        openWithView.setNestedScrollingEnabled(false);
        openWithView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        openWithView.addItemDecoration(new SpacesItemDecoration((int)getResources().getDimension(R.dimen.cardview_space)));
    }


    private void setupPreview() {
        ShortcutModel importShortcut = ShortcutModel.builder()
                .icon(ContextCompat.getDrawable(getActivity(), R.drawable.ic_launcher))
                .title(getString(R.string.launcher_import_geocache))
                .intent(ImportFromGCActivity.createIntent(getActivity(), geocache.code()))
                .build();

        openWithAdapter.setDefaultAction(importShortcut);
    }
}
