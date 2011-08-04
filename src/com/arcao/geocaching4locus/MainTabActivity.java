package com.arcao.geocaching4locus;

import com.arcao.geocaching4locus.activity.CacheListActivity;
import com.arcao.geocaching4locus.activity.TabIntentReceiver;
import com.arcao.geocaching4locus.activity.SearchActivity;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TabHost;

public class MainTabActivity extends TabActivity {

	public static final String TAB_SEARCH = "search";
	public static final String TAB_LIST = "list";
	public static final String TAB_FIELD_NOTES = "field_notes";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main_tab_activity);

		addTab(TAB_SEARCH, SearchActivity.class, R.id.image_button_title_setting);
		addTab(TAB_LIST, CacheListActivity.class, R.id.image_button_title_setting);
		
		switchTab(TAB_SEARCH);
	}
	
	public void onClickSettings(View view) {
		Log.i(getClass().getSimpleName(), getTabHost().getContext().toString());
		Log.i(getClass().getSimpleName(), getTabHost().getCurrentTabView().getContext().toString());
		Log.i(getClass().getSimpleName(), getTabHost().getCurrentView().getContext().toString());
	}
	
	public void onClickClose(View view) {
		Intent intent = new Intent();
		intent.putExtra("a", "ahoj");
		switchTab(TAB_LIST, intent);
	}
	
	
	protected void addTab(String tag, Class<?> clazz, int drawableId) {
		Resources res = getResources();
		TabHost tabHost = getTabHost();

		Intent intent = new Intent().setClass(this, clazz);
		TabHost.TabSpec spec = tabHost.newTabSpec(tag).setIndicator(tag, res.getDrawable(drawableId)).setContent(intent);
		tabHost.addTab(spec);
	}
	
	protected void switchTab(String tag) {
		getTabHost().setCurrentTabByTag(tag);
	}
	
	protected Activity currentTabActivity() {
		return (Activity) getTabHost().getCurrentView().getContext();
	}
	
	protected void switchTab(String tag, Intent intent) {
		switchTab(tag);
		
		Activity a = currentTabActivity();
				
		if (a instanceof TabIntentReceiver) {
			a.setIntent(intent);
			((TabIntentReceiver) a).onReceiveTabIntent(intent);
		}
		
	}
}
