package com.arcao.geocaching4locus;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;

import com.arcao.geocaching4locus.service.SearchGeocacheService;

public class ErrorActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {	
		super.onCreate(savedInstanceState);
		
		if (SearchGeocacheService.ACTION_ERROR.equals(getIntent().getAction())) {
			int resId = getIntent().getIntExtra(SearchGeocacheService.PARAM_RESOURCE_ID, 0);
			String additionalMessage = getIntent().getStringExtra(SearchGeocacheService.PARAM_ADDITIONAL_MESSAGE);
			final boolean openPreference = getIntent().getBooleanExtra(SearchGeocacheService.PARAM_OPEN_PREFERENCE, false);
			
			showError(resId, additionalMessage, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (openPreference)
						startActivity(new Intent(ErrorActivity.this, PreferenceActivity.class));
					ErrorActivity.this.finish();
				}
			});
			
			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.cancelAll();
		} else {
			finish();
		}
	}
	
	protected void showError(int errorResId, String additionalMessage, DialogInterface.OnClickListener onClickListener) {
		if (isFinishing())
			return;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String message = String.format(getString(errorResId), additionalMessage);
		
		builder.setMessage(Html.fromHtml(message));
		builder.setTitle(R.string.error_title);
		builder.setPositiveButton(R.string.ok_button, onClickListener);
		builder.show();
	}
}
