package com.arcao.geocaching4locus.error.fragment;

import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.base.fragment.AbstractErrorDialogFragment;
import com.arcao.geocaching4locus.base.util.PermissionUtil;

import androidx.core.app.ActivityCompat;

public class ExternalStoragePermissionWarningDialogFragment extends AbstractErrorDialogFragment {
    public static final String FRAGMENT_TAG = ExternalStoragePermissionWarningDialogFragment.class.getName();

    @Override
    protected void onPositiveButtonClick() {
        super.onPositiveButtonClick();
        ActivityCompat.requestPermissions(getActivity(), PermissionUtil.PERMISSION_EXTERNAL_STORAGE, PermissionUtil.REQUEST_EXTERNAL_STORAGE_PERMISSION);
    }

    public static ExternalStoragePermissionWarningDialogFragment newInstance() {
        ExternalStoragePermissionWarningDialogFragment
                fragment = new ExternalStoragePermissionWarningDialogFragment();
        fragment.prepareDialog(0, R.string.warning_external_storage_permission, null);

        return fragment;
    }

}
