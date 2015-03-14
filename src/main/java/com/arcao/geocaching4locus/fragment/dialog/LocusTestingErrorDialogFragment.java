package com.arcao.geocaching4locus.fragment.dialog;

import android.content.Context;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.constants.AppConstants;
import locus.api.android.utils.LocusUtils;

public final class LocusTestingErrorDialogFragment extends AbstractErrorDialogFragment {
	public static final String FRAGMENT_TAG = LocusTestingErrorDialogFragment.class.getName();

	public static LocusTestingErrorDialogFragment newInstance(Context context) {
		LocusTestingErrorDialogFragment fragment = new LocusTestingErrorDialogFragment();
		fragment.prepareDialog(R.string.error_title, LocusUtils.isLocusAvailable(context) ? R.string.error_locus_old : R.string.error_locus_not_found, AppConstants.LOCUS_MIN_VERSION.toString());

		return fragment;
	}


	@Override
	public void onPositiveButtonClick() {
		LocusUtils.callInstallLocus(getActivity());
	}
}
