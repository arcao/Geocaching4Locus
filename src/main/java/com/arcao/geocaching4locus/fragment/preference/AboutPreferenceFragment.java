package com.arcao.geocaching4locus.fragment.preference;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import com.arcao.geocaching4locus.BuildConfig;
import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.fragment.dialog.AbstractDialogFragment;

public class AboutPreferenceFragment extends AbstractPreferenceFragment {
	@Override
	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preference_category_about);
	}

	@Override
	protected void preparePreference() {
		final Preference versionPreference = findPreference(ABOUT_VERSION, Preference.class);
		final Preference donatePaypalPreference = findPreference(ABOUT_DONATE_PAYPAL, Preference.class);
		final Preference websitePreference = findPreference(ABOUT_WEBSITE, Preference.class);

		versionPreference.setSummary(App.get(getActivity()).getVersion() + " (" + BuildConfig.GIT_SHA + ")");

		websitePreference.setIntent(new Intent(Intent.ACTION_VIEW, AppConstants.WEBSITE_URI));
		websitePreference.setSummary(AppConstants.WEBSITE_URI.toString());

		donatePaypalPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				new DonatePaypalDialogFragment().show(getActivity().getSupportFragmentManager(), DonatePaypalDialogFragment.TAG);
				return true;
			}
		});
	}

	public static class DonatePaypalDialogFragment extends AbstractDialogFragment {
		public static final String TAG = DonatePaypalDialogFragment.class.getName();

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return new AlertDialog.Builder(getActivity())
							.setTitle(R.string.pref_donate_paypal_choose_currency)
							.setSingleChoiceItems(R.array.currency, -1, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
									startActivity(new Intent(
													Intent.ACTION_VIEW,
													Uri.parse(String.format(AppConstants.DONATE_PAYPAL_URI, getResources().getStringArray(R.array.currency)[which]))
									));
								}
							})
							.setCancelable(true)
							.create();
		}
	}
}
