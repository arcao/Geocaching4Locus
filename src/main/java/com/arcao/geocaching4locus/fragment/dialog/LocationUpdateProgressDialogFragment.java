package com.arcao.geocaching4locus.fragment.dialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.ContextThemeWrapper;

import com.arcao.geocaching4locus.R;

public final class LocationUpdateProgressDialogFragment extends AbstractDialogFragment {
	public static final String TAG = LocationUpdateProgressDialogFragment.class.getName();

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
		ProgressDialog pd = new ProgressDialog(new ContextThemeWrapper(getActivity(), R.style.G4LTheme_Dialog));

		switch (getArguments().getInt(PARAM_SOURCE, SOURCE_NETWORK)) {
			case SOURCE_GPS:
				pd.setMessage(getText(R.string.acquiring_gps_location));
				break;
			default:
				pd.setMessage(getText(R.string.acquiring_network_location));
		}

		pd.setButton(ProgressDialog.BUTTON_NEGATIVE, getText(R.string.cancel_button), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				callOnCancelListener(LocationUpdateProgressDialogFragment.this);
			}
		});

		return pd;
	}
}
