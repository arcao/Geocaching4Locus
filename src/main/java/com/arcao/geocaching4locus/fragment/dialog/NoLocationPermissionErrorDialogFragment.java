package com.arcao.geocaching4locus.fragment.dialog;

import com.arcao.geocaching4locus.R;

public class NoLocationPermissionErrorDialogFragment extends AbstractErrorDialogFragment {
	public static final String FRAGMENT_TAG = LocusTestingErrorDialogFragment.class.getName();

	public static NoLocationPermissionErrorDialogFragment newInstance() {
		NoLocationPermissionErrorDialogFragment fragment = new NoLocationPermissionErrorDialogFragment();
		fragment.prepareDialog(R.string.error_title, R.string.error_no_location_permission, null);

		return fragment;
	}

}
