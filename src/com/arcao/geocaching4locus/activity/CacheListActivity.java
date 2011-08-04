package com.arcao.geocaching4locus.activity;

import com.arcao.geocaching4locus.MainTabActivity;

import android.app.ListActivity;
import android.content.Intent;
import android.widget.Toast;

public class CacheListActivity extends ListActivity implements TabIntentReceiver {

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		if (intent.getExtras().containsKey("a")) {
			Toast.makeText(this, intent.getExtras().getString("a"), Toast.LENGTH_LONG).show();
		}
	}
	
	protected MainTabActivity getTabActivity() {
		return (MainTabActivity) getParent();
	}

	@Override
	public void onReceiveTabIntent(Intent intent) {
		onNewIntent(intent);		
	}
}
