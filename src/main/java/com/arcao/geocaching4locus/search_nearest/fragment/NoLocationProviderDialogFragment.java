package com.arcao.geocaching4locus.search_nearest.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.base.fragment.AbstractDialogFragment;

public class NoLocationProviderDialogFragment extends AbstractDialogFragment {
	public static final String FRAGMENT_TAG = NoLocationProviderDialogFragment.class.getName();

	public static AbstractDialogFragment newInstance() {
		return new NoLocationProviderDialogFragment();
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new MaterialDialog.Builder(getActivity())
				.title(R.string.error_location_not_allowed)
				.content(R.string.error_location_disabled)
				.positiveText(R.string.button_ok)
				.neutralText(R.string.button_settings)

				.onNeutral(new MaterialDialog.SingleButtonCallback() {
					@Override
					public void onClick(@NonNull MaterialDialog materialDialog,
							@NonNull DialogAction dialogAction) {
						getActivity().startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
					}
				}).build();
	}
}
