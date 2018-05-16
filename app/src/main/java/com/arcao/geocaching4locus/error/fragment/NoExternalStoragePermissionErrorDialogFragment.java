package com.arcao.geocaching4locus.error.fragment;

import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.base.fragment.AbstractErrorDialogFragment;

public class NoExternalStoragePermissionErrorDialogFragment extends AbstractErrorDialogFragment {
    public static final String FRAGMENT_TAG = NoExternalStoragePermissionErrorDialogFragment.class.getName();
    private static final String PARAM_CLOSE_PARENT = "CLOSE_PARENT";

    @Override
    protected void onPositiveButtonClick() {
        super.onPositiveButtonClick();
        if (getArguments().getBoolean(PARAM_CLOSE_PARENT))
            getActivity().finish();
    }

    public static NoExternalStoragePermissionErrorDialogFragment newInstance(boolean closeParent) {
        NoExternalStoragePermissionErrorDialogFragment
                fragment = new NoExternalStoragePermissionErrorDialogFragment();
        fragment.prepareDialog(0, R.string.error_no_external_storage_permission, null);
        fragment.getArguments().putBoolean(PARAM_CLOSE_PARENT, closeParent);

        return fragment;
    }

}
