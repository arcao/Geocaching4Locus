package com.arcao.geocaching4locus.activity;

import com.arcao.geocaching4locus.MainTabActivity;

import android.app.Activity;
import android.content.Intent;

public class SearchActivity extends Activity implements TabIntentReceiver {

	protected MainTabActivity getTabActivity() {
		return (MainTabActivity) getParent();
	}

	@Override
	public void onReceiveTabIntent(Intent intent) {
		onNewIntent(intent);
	}
}
