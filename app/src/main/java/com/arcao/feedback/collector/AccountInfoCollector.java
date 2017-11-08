package com.arcao.feedback.collector;

import android.content.Context;

import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.authentication.util.Account;
import com.arcao.geocaching4locus.base.constants.PrefConstants;

public class AccountInfoCollector extends Collector {
	private final Context context;

	public AccountInfoCollector(Context context) {
		this.context = context.getApplicationContext();
	}

	@Override
	public String getName() {
		return "AccountInfo";
	}

	@Override
	protected String collect() {
		final StringBuilder sb = new StringBuilder();

		Account account = App.get(context).getAccountManager().getAccount();
		if (account == null) {
			sb.append("No Account").append("\n");
		} else {
			//noinspection ConstantConditions
			sb.append("NAME=").append(account.name()).append("\n");

			sb.append("\n--- RESTRICTIONS ---\n");
			sb.append(new SharedPreferencesCollector(context, PrefConstants.RESTRICTION_STORAGE_NAME).collect());
		}
		return sb.toString();
	}
}
