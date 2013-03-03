package com.arcao.geocaching4locus;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.view.ContextThemeWrapper;

import com.arcao.geocaching4locus.fragment.AbstractDialogFragment;

public class ErrorActivity extends FragmentActivity {
	public static final String ACTION_ERROR = "com.arcao.geocaching4locus.intent.action.ERROR";
	
	public static final String PARAM_RESOURCE_TITLE = "RESOURCE_TITLE";
	public static final String PARAM_RESOURCE_TEXT = "RESOURCE_TEXT";
	public static final String PARAM_ADDITIONAL_MESSAGE = "ADDITIONAL_MESSAGE";
	public static final String PARAM_OPEN_PREFERENCE = "OPEN_PREFERENCE";
	public static final String PARAM_EXCEPTION = "EXCEPTION";
		
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (ACTION_ERROR.equals(getIntent().getAction())) {
			showErrorDialog();
			
			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.cancelAll();
		}
	}
	
	protected void showErrorDialog () {
		final int resTitleId = getIntent().getIntExtra(PARAM_RESOURCE_TITLE, 0);
		final int resTextId = getIntent().getIntExtra(PARAM_RESOURCE_TEXT, 0);
		final boolean openPreference = getIntent().getBooleanExtra(PARAM_OPEN_PREFERENCE, false);
		final String additionalMessage = getIntent().getStringExtra(PARAM_ADDITIONAL_MESSAGE);
		final Throwable t = (Throwable) getIntent().getSerializableExtra(PARAM_EXCEPTION);
		
		CustomErrorDialogFragment.newInstance(resTitleId, resTextId, openPreference, additionalMessage, t)
			.show(getSupportFragmentManager(), CustomErrorDialogFragment.TAG);
	}
		
	public static Intent createErrorIntent(Context ctx, int resErrorText, String errorText, boolean openPreference, Throwable exception) {
		return createErrorIntent(ctx, 0, resErrorText, errorText, openPreference, exception);
	}
	
	public static Intent createErrorIntent(Context ctx, int resErrorTitle, int resErrorText, String errorText, boolean openPreference, Throwable exception) {
		Intent intent = new Intent(ctx, ErrorActivity.class);
		intent.setAction(ErrorActivity.ACTION_ERROR);
		intent.putExtra(ErrorActivity.PARAM_RESOURCE_TITLE, resErrorTitle);
		intent.putExtra(ErrorActivity.PARAM_RESOURCE_TEXT, resErrorText);
		intent.putExtra(ErrorActivity.PARAM_ADDITIONAL_MESSAGE, errorText);
		intent.putExtra(ErrorActivity.PARAM_OPEN_PREFERENCE, openPreference);
		
		if (exception != null)
			intent.putExtra(ErrorActivity.PARAM_EXCEPTION, exception);
		
		return intent;
	}

	public static class CustomErrorDialogFragment extends AbstractDialogFragment {
		public static final String TAG = CustomErrorDialogFragment.class.getName();
		
		int resTitleId;
		int resTextId;
		boolean openPreference;
		String additionalMessage;
		Throwable t;
		
		public static CustomErrorDialogFragment newInstance(int resTitleId, int resTextId, boolean openPreference, String additionalMessage, Throwable t) {
			CustomErrorDialogFragment fragment = new CustomErrorDialogFragment();
			
			fragment.resTitleId = resTitleId == 0 ? R.string.error_title : resTitleId;
			fragment.resTextId = resTextId;
			fragment.openPreference = openPreference;
			fragment.additionalMessage = additionalMessage;
			fragment.t = t;
			
			return fragment;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.G4LTheme_Dialog))
					.setTitle(resTitleId)
					.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();

							if (openPreference) {
								Intent intent = new Intent(getActivity(), PreferenceActivity.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
								startActivity(intent);
							}
							getActivity().finish();
						}
					})
					.setCancelable(false);

			if (resTextId != 0) {
				builder.setMessage(Html.fromHtml(String.format(getString(resTextId), additionalMessage)));
			} else {
				builder.setMessage(Html.fromHtml(additionalMessage));
			}

			if (t != null) {
				builder.setNeutralButton(R.string.error_report_error, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();

						Intent intent = new Intent(getActivity(), SendErrorActivity.class);
						intent.setAction(SendErrorActivity.ACTION_SEND_ERROR);
						intent.putExtra(SendErrorActivity.PARAM_EXCEPTION, t);
						startActivity(intent);
						getActivity().finish();
					}
				});
			}

			return builder.create();
		}
	}
}
