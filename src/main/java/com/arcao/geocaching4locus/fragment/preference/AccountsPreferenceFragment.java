package com.arcao.geocaching4locus.fragment.preference;

import android.os.Bundle;
import android.preference.Preference;
import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.authentication.helper.AuthenticatorHelper;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.util.IntentUtil;
import com.arcao.geocaching4locus.util.ResourcesUtil;

public class AccountsPreferenceFragment extends AbstractPreferenceFragment {
	private static final String ACCOUNT = "account";

	@Override
	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preference_category_accounts);
	}

	@Override
	protected void preparePreference() {
		final Preference accountPreference = findPreference(ACCOUNT, Preference.class);
		final AuthenticatorHelper authenticatorHelper = App.get(getActivity()).getAuthenticatorHelper();

		accountPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				if (authenticatorHelper.hasAccount()) {
					authenticatorHelper.removeAccount();
					accountPreference.setTitle(R.string.pref_account_login);
					accountPreference.setSummary(R.string.pref_account_login_summary);
				} else {
					authenticatorHelper.requestSignOn(getActivity(), 0);
				}

				return true;
			}
		});

		if (authenticatorHelper.hasAccount()) {
			accountPreference.setTitle(R.string.pref_account_logout);
			//noinspection ConstantConditions
			accountPreference.setSummary(prepareAccountSummary(authenticatorHelper.getAccount().name));
		} else {
			accountPreference.setTitle(R.string.pref_account_login);
			accountPreference.setSummary(R.string.pref_account_login_summary);
		}

		final Preference geocachingLivePreference = findPreference(ACCOUNT_GEOCACHING_LIVE, Preference.class);
		geocachingLivePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				IntentUtil.showWebPage(getActivity(), AppConstants.GEOCACHING_LIVE_URI);
				return true;
			}
		});
	}

	private CharSequence prepareAccountSummary(CharSequence value) {
		return ResourcesUtil.getText(getActivity(), R.string.pref_account_logout_summary, stylizedValue(value));
	}
}
