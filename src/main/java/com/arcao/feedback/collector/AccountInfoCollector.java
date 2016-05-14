package com.arcao.feedback.collector;

import android.content.Context;
import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.authentication.util.AccountManager;
import com.arcao.geocaching4locus.base.constants.PrefConstants;

public class AccountInfoCollector extends Collector {
	private final Context mContext;

	public AccountInfoCollector(Context context) {
		this.mContext = context.getApplicationContext();
	}

	@Override
	public String getName() {
		return "AccountInfo";
	}

	@Override
	protected String collect() {
		final StringBuilder sb = new StringBuilder();

		AccountManager helper = App.get(mContext).getAccountManager();

		if (!helper.hasAccount()) {
			sb.append("No Account").append("\n");
		} else {
			//noinspection ConstantConditions
			sb.append("NAME=").append(helper.getAccount().name).append("\n");

			sb.append("\n--- RESTRICTIONS ---\n");
			sb.append(new SharedPreferencesCollector(mContext, PrefConstants.RESTRICTION_STORAGE_NAME).collect());
		}
		return sb.toString();
	}
}
