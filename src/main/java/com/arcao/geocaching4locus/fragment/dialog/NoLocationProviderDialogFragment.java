package com.arcao.geocaching4locus.fragment.dialog;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.arcao.geocaching4locus.R;

public class NoLocationProviderDialogFragment extends AbstractDialogFragment {
	public static final String FRAGMENT_TAG = NoLocationProviderDialogFragment.class.getName();

	public static AbstractDialogFragment newInstance() {
		return new NoLocationProviderDialogFragment();
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new MaterialDialog.Builder(getActivity())
				.title(R.string.error_location_title)
				.content(R.string.error_location)
				.positiveText(R.string.ok_button)
				.neutralText(R.string.error_location_settings_button)

				.onNeutral(new MaterialDialog.SingleButtonCallback() {
					@Override
					public void onClick(@NonNull MaterialDialog materialDialog,
							@NonNull DialogAction dialogAction) {
						getActivity().startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
					}
				}).build();
	}
}
