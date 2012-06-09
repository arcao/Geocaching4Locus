package com.arcao.geocaching4locus;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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
	
	public static final int DIALOG_ERROR_ID = 0;
		
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		
		if (ACTION_ERROR.equals(getIntent().getAction())) {
			showDialog(DIALOG_ERROR_ID);
			
			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.cancelAll();
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DIALOG_ERROR_ID:
				final int resId = getIntent().getIntExtra(PARAM_RESOURCE_ID, 0);
				final boolean openPreference = getIntent().getBooleanExtra(PARAM_OPEN_PREFERENCE, false);
				final String additionalMessage = getIntent().getStringExtra(PARAM_ADDITIONAL_MESSAGE);
				final Throwable t = (Throwable) getIntent().getSerializableExtra(PARAM_EXCEPTION);
				
				AlertDialog.Builder builder = new AlertDialog.Builder(this)
					.setTitle(R.string.error_title)
					.setMessage(Html.fromHtml(String.format(getString(resId), additionalMessage)))
					.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {			
							dialog.dismiss();
							
							if (openPreference) {
								Intent intent = new Intent(ErrorActivity.this, PreferenceActivity.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
								startActivity(intent);
							}
							finish();
						}
					})
					.setCancelable(false);
					
				if (t != null) {
					builder.setNeutralButton(R.string.error_report_error, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							
							Intent intent = new Intent(ErrorActivity.this, SendErrorActivity.class);
							intent.setAction(SendErrorActivity.ACTION_SEND_ERROR);
							intent.putExtra(SendErrorActivity.PARAM_EXCEPTION, t);
							startActivity(intent);
							finish();
						}
					});
				}

				return builder.create();

			default:
				return super.onCreateDialog(id);
		}
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
