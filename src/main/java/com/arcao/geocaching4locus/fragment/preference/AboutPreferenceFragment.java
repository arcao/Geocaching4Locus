package com.arcao.geocaching4locus.fragment.preference;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.view.View;
import com.afollestad.materialdialogs.MaterialDialog;
import com.arcao.feedback.FeedbackHelper;
import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.BuildConfig;
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
		final Preference websitePreference = findPreference(ABOUT_WEBSITE, Preference.class);
		final Preference feedbackPreference = findPreference(ABOUT_FEEDBACK, Preference.class);
		final Preference donatePaypalPreference = findPreference(ABOUT_DONATE_PAYPAL, Preference.class);

		versionPreference.setSummary(App.get(getActivity()).getVersion() + " (" + BuildConfig.GIT_SHA + ")");

		websitePreference.setIntent(new Intent(Intent.ACTION_VIEW, AppConstants.WEBSITE_URI));
		websitePreference.setSummary(AppConstants.WEBSITE_URI.toString());

		feedbackPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				FeedbackHelper.sendFeedback(getActivity(), R.string.feedback_email, R.string.feedback_subject, R.string.feedback_body);
				return true;
			}
		});

		donatePaypalPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				new DonatePaypalDialogFragment().show(getActivity().getFragmentManager(), DonatePaypalDialogFragment.TAG);
				return true;
			}
		});
	}

	public static class DonatePaypalDialogFragment extends AbstractDialogFragment {
		public static final String TAG = DonatePaypalDialogFragment.class.getName();

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return new MaterialDialog.Builder(getActivity())
							.title(R.string.pref_donate_paypal_choose_currency)
							.items(R.array.currency)
							.itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallback() {
								@Override
								public void onSelection(MaterialDialog materialDialog, View view, int which, CharSequence charSequence) {
									startActivity(new Intent(
													Intent.ACTION_VIEW,
													Uri.parse(String.format(AppConstants.DONATE_PAYPAL_URI, getResources().getStringArray(R.array.currency)[which]))
									));
								}
							})
							.cancelable(true)
							.build();
		}
	}
}
