package com.arcao.geocaching4locus;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;

public class ErrorActivity extends Activity {
	public static final String ACTION_ERROR = "com.arcao.geocaching4locus.intent.action.ERROR";
	
	public static final String PARAM_RESOURCE_ID = "RESOURCE_ID";
	public static final String PARAM_ADDITIONAL_MESSAGE = "ADDITIONAL_MESSAGE";
	public static final String PARAM_OPEN_PREFERENCE = "OPEN_PREFERENCE";
	public static final String PARAM_EXCEPTION = "EXCEPTION";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {	
		super.onCreate(savedInstanceState);
		
		if (ACTION_ERROR.equals(getIntent().getAction())) {
			int resId = getIntent().getIntExtra(PARAM_RESOURCE_ID, 0);
			String additionalMessage = getIntent().getStringExtra(PARAM_ADDITIONAL_MESSAGE);
			final boolean openPreference = getIntent().getBooleanExtra(PARAM_OPEN_PREFERENCE, false);
			Throwable t = (Throwable) getIntent().getSerializableExtra(PARAM_EXCEPTION);
			
			showError(resId, additionalMessage, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {			
					dialog.dismiss();
					
					if (openPreference) {
						Intent intent = new Intent(ErrorActivity.this, PreferenceActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
						startActivity(intent);
					}
					ErrorActivity.this.finish();
				}
			}, t);
			
			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.cancelAll();
		} else {
			finish();
		}
	}
	
	protected void showError(int errorResId, String additionalMessage, DialogInterface.OnClickListener onClickListener, final Throwable t) {
		if (isFinishing())
			return;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String message = String.format(getString(errorResId), additionalMessage);
		
		builder.setMessage(Html.fromHtml(message));
		builder.setTitle(R.string.error_title);
		builder.setPositiveButton(R.string.ok_button, onClickListener);
		builder.setCancelable(false);
		
		if (t != null) {
			builder.setNeutralButton(R.string.error_report_error, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(ErrorActivity.this, SendErrorActivity.class);
					intent.setAction(SendErrorActivity.ACTION_SEND_ERROR);
					intent.putExtra(SendErrorActivity.PARAM_EXCEPTION, t);
					startActivity(intent);
					
					dialog.dismiss();
				}
			});
		}
		
		builder.show();
	}
	
	public static Intent createErrorIntent(Context ctx, int resErrorId, String errorText, boolean openPreference, Throwable exception) {
		Intent intent = new Intent(ctx, ErrorActivity.class);
		intent.setAction(ErrorActivity.ACTION_ERROR);
		intent.putExtra(ErrorActivity.PARAM_RESOURCE_ID, resErrorId);
		intent.putExtra(ErrorActivity.PARAM_ADDITIONAL_MESSAGE, errorText);
		intent.putExtra(ErrorActivity.PARAM_OPEN_PREFERENCE, openPreference);
		
		if (exception != null)
			intent.putExtra(ErrorActivity.PARAM_EXCEPTION, exception);
		
		return intent;
	}

}
