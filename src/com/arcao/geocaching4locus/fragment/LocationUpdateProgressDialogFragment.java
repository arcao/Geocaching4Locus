package com.arcao.geocaching4locus.fragment;

import java.lang.ref.WeakReference;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.arcao.geocaching4locus.R;

public class LocationUpdateProgressDialogFragment extends CustomDialogFragment {
	private static final String PARAM_SOURCE = "SOURCE";
	
	public static final int SOURCE_GPS = 0;
	public static final int SOURCE_NETWORK = 1;
	
	private WeakReference<Cancellable<LocationUpdateProgressDialogFragment>> listenerRef;
	
	public LocationUpdateProgressDialogFragment() {
		super();
	}
		
	public static LocationUpdateProgressDialogFragment newInstance(int source, Cancellable<LocationUpdateProgressDialogFragment> listener) {
		LocationUpdateProgressDialogFragment frag = new LocationUpdateProgressDialogFragment();
		frag.listenerRef = new WeakReference<CustomDialogFragment.Cancellable<LocationUpdateProgressDialogFragment>>(listener);
		
		Bundle args = new Bundle();
    args.putInt(PARAM_SOURCE, source);
    frag.setArguments(args);
					
		return frag;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		ProgressDialog pd = new ProgressDialog(getActivity());
		
		switch (getArguments().getInt(PARAM_SOURCE, SOURCE_NETWORK)) {
			case SOURCE_GPS:
				pd.setMessage(getText(R.string.acquiring_gps_location));
				break;
			default:
				pd.setMessage(getText(R.string.acquiring_network_location));
		}
		
		pd.setCancelable(false);
		
		if (listenerRef.get() != null) {
			pd.setButton(ProgressDialog.BUTTON_NEGATIVE, getText(R.string.cancel_button), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Cancellable<LocationUpdateProgressDialogFragment> listener = listenerRef.get();
					if (listener != null) {
						listener.onCancel(LocationUpdateProgressDialogFragment.this);
					}
				}
			});
		}
		
		return pd;
	}
}
