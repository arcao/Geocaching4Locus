package com.arcao.geocaching4locus.fragment.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import com.afollestad.materialdialogs.MaterialDialog;
import com.arcao.geocaching4locus.R;

public final class LocationUpdateProgressDialogFragment extends AbstractDialogFragment {
	public static final String FRAGMENT_TAG = LocationUpdateProgressDialogFragment.class.getName();

	private static final String PARAM_SOURCE = "SOURCE";

	public static final int SOURCE_GPS = 0;
	public static final int SOURCE_NETWORK = 1;

	public static LocationUpdateProgressDialogFragment newInstance(int source) {
		LocationUpdateProgressDialogFragment fragment = new LocationUpdateProgressDialogFragment();

		Bundle args = new Bundle();
		args.putInt(PARAM_SOURCE, source);
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setCancelable(false);
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
						.progress(true, 0)
						.negativeText(R.string.cancel_button);

		switch (getArguments().getInt(PARAM_SOURCE, SOURCE_NETWORK)) {
			case SOURCE_GPS:
				builder.content(getText(R.string.acquiring_gps_location));
				break;
			default:
				builder.content(getText(R.string.acquiring_network_location));
		}

		return builder.build();
	}
}
