package com.arcao.geocaching4locus;

import android.app.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import com.arcao.geocaching4locus.fragment.dialog.AbstractDialogFragment;
import com.arcao.geocaching4locus.util.SpannedFix;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.Builder;

public class ErrorActivity extends Activity {
	public static final String ACTION_ERROR = "com.arcao.geocaching4locus.intent.action.ERROR";

	private static final String PARAM_RESOURCE_TITLE = "RESOURCE_TITLE";
	public static final String PARAM_RESOURCE_TEXT = "RESOURCE_TEXT";
	public static final String PARAM_ADDITIONAL_MESSAGE = "ADDITIONAL_MESSAGE";
	private static final String PARAM_PREFERENCE_FRAGMENT = "PREFERENCE_FRAGMENT";
	private static final String PARAM_EXCEPTION = "EXCEPTION";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancelAll();
	}

	@Override
	protected void onPostResume() {
		super.onPostResume();

		showErrorDialog();
	}

	private void showErrorDialog () {
		final int resTitleId = getIntent().getIntExtra(PARAM_RESOURCE_TITLE, 0);
		final int resTextId = getIntent().getIntExtra(PARAM_RESOURCE_TEXT, 0);
		final Class<?> preferenceFragment = (Class<?>) getIntent().getSerializableExtra(PARAM_PREFERENCE_FRAGMENT);
		final String additionalMessage = getIntent().getStringExtra(PARAM_ADDITIONAL_MESSAGE);
		final Throwable t = (Throwable) getIntent().getSerializableExtra(PARAM_EXCEPTION);

		if (getFragmentManager().findFragmentByTag(ErrorDialogFragment.FRAGMENT_TAG) != null)
			return;

		ErrorDialogFragment.newInstance(resTitleId, resTextId, preferenceFragment, additionalMessage, t)
			.show(getFragmentManager(), ErrorDialogFragment.FRAGMENT_TAG);
	}


	public static class ErrorDialogFragment extends AbstractDialogFragment {
		public static final String FRAGMENT_TAG = ErrorDialogFragment.class.getName();


		public static DialogFragment newInstance(int resTitleId, int resTextId, Class<?> preferenceFragment, String additionalMessage, Throwable t) {
			ErrorDialogFragment fragment = new ErrorDialogFragment();
			fragment.setCancelable(false);

			Bundle args = new Bundle();
			args.putInt(PARAM_RESOURCE_TITLE, resTitleId == 0 ? R.string.error_title : resTitleId);
			args.putInt(PARAM_RESOURCE_TEXT, resTextId);
			args.putSerializable(PARAM_PREFERENCE_FRAGMENT, preferenceFragment);
			args.putString(PARAM_ADDITIONAL_MESSAGE, additionalMessage);
			args.putSerializable(PARAM_EXCEPTION, t);

			fragment.setArguments(args);

			return fragment;
		}

		@NonNull
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final int resTitleId = getArguments().getInt(PARAM_RESOURCE_TITLE);
			final int resTextId = getArguments().getInt(PARAM_RESOURCE_TEXT);
			final Class<?> preferenceFragment = (Class<?>) getArguments().getSerializable(PARAM_PREFERENCE_FRAGMENT);
			final String additionalMessage = getArguments().getString(PARAM_ADDITIONAL_MESSAGE);
			final Throwable t = (Throwable) getArguments().getSerializable(PARAM_EXCEPTION);

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
				.setTitle(resTitleId)
				.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();

						if (preferenceFragment != null) {
							Intent intent = SettingsActivity.createIntent(getActivity(), preferenceFragment);
							intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
						}
						getActivity().finish();
					}
				});

			if (resTextId != 0) {
				builder.setMessage(SpannedFix.fromHtml(String.format(getString(resTextId), StringUtils.defaultString(additionalMessage))));
			} else {
				builder.setMessage(SpannedFix.fromHtml(StringUtils.defaultString(additionalMessage)));
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

	public static class IntentBuilder implements Builder<Intent> {
		private final Context context;
		private int title = 0;
		private int text = 0;
		private String additionalMessage;
		private Class<?> preferenceFragment;
		private Throwable exception;


		public IntentBuilder(Context context) {
			this.context = context;
		}

		public IntentBuilder setTitle(int resTitle) {
			this.title = resTitle;
			return this;
		}

		public IntentBuilder setText(int text) {
			this.text = text;
			return this;
		}

		public IntentBuilder setAdditionalMessage(String additionalMessage) {
			this.additionalMessage = additionalMessage;
			return this;
		}

		public IntentBuilder setPreferenceFragment(Class<?> preferenceFragment) {
			this.preferenceFragment = preferenceFragment;
			return this;
		}

		public IntentBuilder setException(Throwable exception) {
			this.exception = exception;
			return this;
		}

		@Override
		public Intent build() {
			return new Intent(context, ErrorActivity.class)
							.setAction(ErrorActivity.ACTION_ERROR)
							.putExtra(ErrorActivity.PARAM_RESOURCE_TITLE, title)
							.putExtra(ErrorActivity.PARAM_RESOURCE_TEXT, text)
							.putExtra(ErrorActivity.PARAM_ADDITIONAL_MESSAGE, additionalMessage)
							.putExtra(ErrorActivity.PARAM_PREFERENCE_FRAGMENT, preferenceFragment)
							.putExtra(ErrorActivity.PARAM_EXCEPTION, exception);

		}
	}
}
