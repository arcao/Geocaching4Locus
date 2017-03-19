package com.arcao.geocaching4locus.import_gc.fragment;

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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.arcao.geocaching.api.data.Geocache;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.base.fragment.AbstractDialogFragment;
import com.arcao.geocaching4locus.import_bookmarks.widget.decorator.SpacesItemDecoration;
import com.arcao.geocaching4locus.import_gc.adapter.OpenWithRecyclerAdapter;
import com.arcao.geocaching4locus.import_gc.task.RetrieveGeocacheTask;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PreviewDialogFragment extends AbstractDialogFragment implements RetrieveGeocacheTask.TaskListener, OpenWithRecyclerAdapter.OnItemClickListener {
    public static final String FRAGMENT_TAG = PreviewDialogFragment.class.getName();

    private static final String PARAM_URI = "CACHE_URI";
    private static final String PARAM_CACHE_ID = "CACHE_ID";
    private Geocache geocache = null;

    public interface DialogListener {
        void onImport(@Nullable String cacheId);
        void onClose();
    }

    @Nullable private RetrieveGeocacheTask mTask;
    WeakReference<DialogListener> mDialogListenerRef;

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String geocacheCode = getArguments().getString(PARAM_CACHE_ID);

        mTask = new RetrieveGeocacheTask(getActivity(), this);
        mTask.execute(geocacheCode);
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

        //Set callback
        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }

        ButterKnife.bind(this, contentView);

        setupOpenWith();

        if (geocache != null) {
            setupPreview();
        }

        // fix width for larger screens like tablets
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                View decorView = dialog.getWindow().getDecorView();
                int width = decorView == null ? 0 : decorView.getWidth();
                int maxWidth = getResources().getDimensionPixelSize(R.dimen.bottom_sheet_max_width);
                if (width > maxWidth) {
                    dialog.getWindow().setLayout(maxWidth, ViewGroup.LayoutParams.MATCH_PARENT);
                }
            }
        });

        return dialog;
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

    public void onImportClick() {
        dismiss();

        String geocacheCode = getArguments().getString(PARAM_CACHE_ID);

        if (geocache != null) {
            geocacheCode = geocache.code();
        }

        DialogListener listener = mDialogListenerRef.get();
        if (listener != null) {
            listener.onImport(geocacheCode);
        }
    }

    @Override
    public void onTaskFinished(Geocache geocache) {
        this.geocache = geocache;
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


    @BindView(R.id.app_list) RecyclerView recyclerView;

    private void setupOpenWith() {
        Uri url = getArguments().getParcelable(PARAM_URI);
        Intent intent = new Intent(Intent.ACTION_VIEW, url);

        OpenWithRecyclerAdapter adapter = new OpenWithRecyclerAdapter(getActivity(), intent);
        adapter.setOnItemClickListener(this);

        recyclerView.setAdapter(adapter);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.addItemDecoration(new SpacesItemDecoration((int)getResources().getDimension(R.dimen.cardview_space)));
    }


    private void setupPreview() {
    }
}
