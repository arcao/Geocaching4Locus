package com.arcao.geocaching4locus.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

import com.arcao.geocaching4locus.Geocaching4LocusApplication;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.constants.AppConstants;

import locus.api.android.utils.LocusUtils;

public final class LocusTestingErrorDialogFragment extends AbstractErrorDialogFragment {
	public static final String TAG = LocusTestingErrorDialogFragment.class.getName();

	public static LocusTestingErrorDialogFragment newInstance() {
		Context context = Geocaching4LocusApplication.getAppContext();

		LocusTestingErrorDialogFragment fragment = new LocusTestingErrorDialogFragment();
		fragment.prepareDialog(R.string.error_title, LocusUtils.isLocusAvailable(context) ? R.string.error_locus_old : R.string.error_locus_not_found, AppConstants.LOCUS_MIN_VERSION.toString());

		return fragment;
	}


	@Override
	public OnClickListener getPositiveButtonOnClickListener() {
		return new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				LocusUtils.callInstallLocus(getActivity());
			}
		};
	}
}
