package com.arcao.geocaching4locus.fragment.preference;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.view.View;
import com.afollestad.materialdialogs.MaterialDialog;
import com.arcao.feedback.FeedbackHelper;
import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.fragment.dialog.AbstractDialogFragment;
import com.arcao.geocaching4locus.util.IntentUtil;
import hu.supercluster.paperwork.Paperwork;

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
		final Preference facebookPreference = findPreference(ABOUT_FACEBOOK, Preference.class);
		final Preference gplusPreference = findPreference(ABOUT_GPLUS, Preference.class);
		final Preference feedbackPreference = findPreference(ABOUT_FEEDBACK, Preference.class);
		final Preference donatePaypalPreference = findPreference(ABOUT_DONATE_PAYPAL, Preference.class);
		final Paperwork paperwork = new Paperwork(getActivity());

		versionPreference.setSummary(App.get(getActivity()).getVersion() + " (" + paperwork.get("gitSha") + ")");

		websitePreference.setSummary(AppConstants.WEBSITE_URI.toString());
		websitePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				IntentUtil.showWebPage(getActivity(), AppConstants.WEBSITE_URI);
				return true;
			}
		});

		facebookPreference.setSummary(AppConstants.FACEBOOK_URI.toString());
		facebookPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				IntentUtil.showWebPage(getActivity(), AppConstants.FACEBOOK_URI);
				return true;
			}
		});

		gplusPreference.setSummary(AppConstants.GPLUS_URI.toString());
		gplusPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				IntentUtil.showWebPage(getActivity(), AppConstants.GPLUS_URI);
				return true;
			}
		});

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
							.itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
								@Override
								public boolean onSelection(MaterialDialog materialDialog, View view, int which, CharSequence charSequence) {
									IntentUtil.showWebPage(getActivity(), Uri.parse(String.format(AppConstants.DONATE_PAYPAL_URI, getResources().getStringArray(R.array.currency)[which])));
									return true;
								}
							})
							.cancelable(true)
							.build();
		}
	}
}
