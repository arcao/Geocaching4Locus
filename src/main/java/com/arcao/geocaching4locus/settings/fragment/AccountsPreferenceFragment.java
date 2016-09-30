package com.arcao.geocaching4locus.settings.fragment;

import android.os.Bundle;
import android.preference.Preference;

import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.authentication.util.Account;
import com.arcao.geocaching4locus.authentication.util.AccountManager;
import com.arcao.geocaching4locus.base.fragment.AbstractPreferenceFragment;
import com.arcao.geocaching4locus.base.constants.AppConstants;
import com.arcao.geocaching4locus.base.util.IntentUtil;
import com.arcao.geocaching4locus.base.util.HtmlUtil;

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
		final AccountManager accountManager = App.get(getActivity()).getAccountManager();

		accountPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				if (accountManager.getAccount() != null) {
					accountManager.removeAccount();
					accountPreference.setTitle(R.string.pref_account_login);
					accountPreference.setSummary(R.string.pref_account_login_summary);
				} else {
					accountManager.requestSignOn(getActivity(), 0);
				}

				return true;
			}
		});

		Account account = accountManager.getAccount();
		if (account != null) {
			accountPreference.setTitle(R.string.pref_account_logout);
			//noinspection ConstantConditions
			accountPreference.setSummary(prepareAccountSummary(account.name()));
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
		String summary = getString(R.string.pref_account_logout_summary);
		return HtmlUtil.fromHtml(String.format(summary, "<font color=\"#FF8000\"><b>" + value.toString() + "</b></font>"));
	}
}
