package com.arcao.geocaching4locus.search_nearest.fragment;

import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.base.fragment.AbstractErrorDialogFragment;
import com.arcao.geocaching4locus.error.fragment.LocusTestingErrorDialogFragment;

public class NoLocationPermissionErrorDialogFragment extends AbstractErrorDialogFragment {
	public static final String FRAGMENT_TAG = LocusTestingErrorDialogFragment.class.getName();

	public static NoLocationPermissionErrorDialogFragment newInstance() {
		NoLocationPermissionErrorDialogFragment fragment = new NoLocationPermissionErrorDialogFragment();
		fragment.prepareDialog(0, R.string.error_no_location_permission, null);

		return fragment;
	}

}
