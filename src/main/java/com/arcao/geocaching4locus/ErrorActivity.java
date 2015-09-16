package com.arcao.geocaching4locus;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;
import com.afollestad.materialdialogs.MaterialDialog;
import com.arcao.geocaching4locus.fragment.dialog.AbstractDialogFragment;
import com.arcao.geocaching4locus.util.SpannedFix;
import com.crashlytics.android.Crashlytics;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.Builder;

public class ErrorActivity extends AppCompatActivity {
	private static final String PARAM_ARGUMENTS = "ARGS";

	private static final String KEY_TITLE = "TITLE";
	private static final String KEY_MESSAGE = "MESSAGE";
	private static final String KEY_ADDITIONAL_MESSAGE = "ADDITIONAL_MESSAGE";
	private static final String KEY_POSITIVE_ACTION = "POSITIVE_ACTION";
	private static final String KEY_POSITIVE_BUTTON_TEXT = "POSITIVE_BUTTON_TEXT";
	private static final String KEY_NEGATIVE_BUTTON_TEXT = "NEGATIVE_BUTTON_TEXT";
	private static final String KEY_EXCEPTION = "EXCEPTION";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null)
			showErrorDialog();
	}

	private void showErrorDialog () {
		Bundle args = getIntent().getBundleExtra(PARAM_ARGUMENTS);

		ErrorDialogFragment.newInstance(args)
			.show(getFragmentManager(), ErrorDialogFragment.FRAGMENT_TAG);
	}

	public static class ErrorDialogFragment extends AbstractDialogFragment {
		public static final String FRAGMENT_TAG = ErrorDialogFragment.class.getName();


		private static DialogFragment newInstance(Bundle args) {
			ErrorDialogFragment fragment = new ErrorDialogFragment();
			fragment.setCancelable(false);
			fragment.setArguments(args);
			return fragment;
		}

		@NonNull
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final Bundle args = getArguments();

			final int title = args.getInt(KEY_TITLE);
			final int message = args.getInt(KEY_MESSAGE);
			final String additionalMessage = args.getString(KEY_ADDITIONAL_MESSAGE);
			final Intent positiveAction = args.getParcelable(KEY_POSITIVE_ACTION);
			int positiveButtonText = args.getInt(KEY_POSITIVE_BUTTON_TEXT);
			int negativeButtonText = args.getInt(KEY_NEGATIVE_BUTTON_TEXT);
			final Throwable t = (Throwable) args.getSerializable(KEY_EXCEPTION);


			MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
					.positiveText(positiveButtonText != 0 ? positiveButtonText : R.string.ok_button);

			if (title != 0) {
				builder.title(title);
			}

			if (message != 0) {
				builder.content(SpannedFix.fromHtml(
						String.format(getString(message), StringUtils.defaultString(additionalMessage))));
			} else {
				builder.content(SpannedFix.fromHtml(StringUtils.defaultString(additionalMessage)));
			}

			if (negativeButtonText != 0) {
				builder.negativeText(negativeButtonText);
			}

			builder.callback(new MaterialDialog.ButtonCallback() {
				@Override
				public void onPositive(MaterialDialog dialog) {
					final CheckBox checkBox = (CheckBox) dialog.getView().findViewById(R.id.checkbox);
					if (checkBox != null && checkBox.isChecked()) {
						Crashlytics.logException(t);
						Toast.makeText(dialog.getContext(), R.string.error_report_sent_toast, Toast.LENGTH_LONG)
								.show();
					}

					if (positiveAction != null) {
						startActivity(positiveAction);
					}
					getActivity().finish();
				}

				@Override
				public void onNegative(MaterialDialog dialog) {
					getActivity().finish();
				}
			});

			final MaterialDialog dialog = builder.build();

			if (t != null) {
				ViewGroup rootLayout = (ViewGroup) dialog.getView();
				ScrollView scrollView =
						(ScrollView) rootLayout.findViewById(
								com.afollestad.materialdialogs.R.id.contentScrollView);

				View[] innerViews = new View[scrollView.getChildCount()];
				for (int i = 0; i < innerViews.length; i++) {
					innerViews[i] = scrollView.getChildAt(i);
				}
				scrollView.removeAllViews();

				LinearLayout layout = new LinearLayout(dialog.getContext());
				layout.setOrientation(LinearLayout.VERTICAL);

				for (View innerView : innerViews) {
					layout.addView(innerView);
				}

				CheckBox checkBox = (CheckBox) LayoutInflater.from(dialog.getContext()).inflate(R.layout.dialog_error_checkbox, layout, false);
				if (dialog.getContentView() != null) {
					// fix for text color
					checkBox.setTextColor(dialog.getContentView().getTextColors());
				}
				layout.addView(checkBox);

				scrollView.addView(layout,
						new LinearLayout.LayoutParams(
								ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
			}

			return dialog;
		}
	}

	public static class IntentBuilder implements Builder<Intent> {
		private final Context context;
		private int title = 0;
		private int message = 0;
		private String additionalMessage = null;
		private Intent positiveAction = null;
		private int positiveButtonText = 0;
		private int negativeButtonText = 0;
		private Throwable exception = null;

		public IntentBuilder(@NonNull Context context) {
			this.context = context;
		}

		public IntentBuilder setTitle(@StringRes int resTitle) {
			this.title = resTitle;
			return this;
		}

		public IntentBuilder setMessage(@StringRes int message) {
			this.message = message;
			return this;
		}

		public IntentBuilder setAdditionalMessage(@Nullable String additionalMessage) {
			this.additionalMessage = additionalMessage;
			return this;
		}

		public IntentBuilder setPositiveAction(@Nullable Intent positiveAction) {
			this.positiveAction = positiveAction;
			return this;
		}

		public IntentBuilder setPositiveButtonText(@StringRes int positiveButtonText) {
			this.positiveButtonText = positiveButtonText;
			return this;
		}

		public IntentBuilder setNegativeButtonText(@StringRes int negativeButtonText) {
			this.negativeButtonText = negativeButtonText;
			return this;
		}

		public IntentBuilder setException(@Nullable Throwable exception) {
			this.exception = exception;
			return this;
		}

		@Override
		public Intent build() {
			Bundle args = new Bundle();
			args.putInt(KEY_TITLE, title);
			args.putInt(KEY_MESSAGE, message);
			args.putCharSequence(KEY_ADDITIONAL_MESSAGE, additionalMessage);
			args.putParcelable(KEY_POSITIVE_ACTION, positiveAction);
			args.putInt(KEY_POSITIVE_BUTTON_TEXT, positiveButtonText);
			args.putInt(KEY_NEGATIVE_BUTTON_TEXT, negativeButtonText);
			args.putSerializable(KEY_EXCEPTION, exception);

			return new Intent(context, ErrorActivity.class)
					.putExtra(PARAM_ARGUMENTS, args);
		}
	}
}
