package com.arcao.geocaching4locus.error.fragment;

import android.content.Context;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.base.fragment.AbstractErrorDialogFragment;
import com.arcao.geocaching4locus.base.constants.AppConstants;
import locus.api.android.utils.LocusUtils;

public final class LocusTestingErrorDialogFragment extends AbstractErrorDialogFragment {
	public static final String FRAGMENT_TAG = LocusTestingErrorDialogFragment.class.getName();

	public static LocusTestingErrorDialogFragment newInstance(Context context) {
		LocusTestingErrorDialogFragment fragment = new LocusTestingErrorDialogFragment();
		fragment.prepareDialog(0, LocusUtils.isLocusAvailable(context) ? R.string.error_old_locus_map : R.string.error_locus_not_found, AppConstants.LOCUS_MIN_VERSION.toString());

		return fragment;
	}


	@Override
	public void onPositiveButtonClick() {
		LocusUtils.callInstallLocus(getActivity());
		getActivity().finish();
	}
}
