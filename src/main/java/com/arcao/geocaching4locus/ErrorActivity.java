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

	private static final String BUNDLE_RESOURCE_TITLE = "RESOURCE_TITLE";
	private static final String BUNDLE_RESOURCE_TEXT = "RESOURCE_TEXT";
	private static final String BUNDLE_ADDITIONAL_MESSAGE = "ADDITIONAL_MESSAGE";
	private static final String BUNDLE_NEXT_ACTION = "NEXT_ACTION";
	private static final String BUNDLE_NEXT_ACTION_TEXT = "NEXT_ACTION_TEXT";
	private static final String BUNDLE_EXCEPTION = "EXCEPTION";

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

			final int resTitleId = args.getInt(BUNDLE_RESOURCE_TITLE);
			final int resTextId = args.getInt(BUNDLE_RESOURCE_TEXT);
			final String additionalMessage = args.getString(BUNDLE_ADDITIONAL_MESSAGE);
			final Intent nextAction = args.getParcelable(BUNDLE_NEXT_ACTION);
			int resNextActionText = args.getInt(BUNDLE_NEXT_ACTION_TEXT);
			final Throwable t = (Throwable) args.getSerializable(BUNDLE_EXCEPTION);

			if (resNextActionText == 0)
				resNextActionText = R.string.continue_button;

			MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
					.positiveText(nextAction != null ? resNextActionText : R.string.ok_button);

			if (resTitleId != 0) {
				builder.title(resTitleId);
			}

			if (resTextId != 0) {
				builder.content(SpannedFix.fromHtml(
						String.format(getString(resTextId), StringUtils.defaultString(additionalMessage))));
			} else {
				builder.content(SpannedFix.fromHtml(StringUtils.defaultString(additionalMessage)));
			}


			if (nextAction != null) {
				builder.negativeText(R.string.cancel_button);
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

					if (nextAction != null) {
						startActivity(nextAction);
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
		private int text = 0;
		private String additionalMessage;
		private Intent nextAction;
		private int nextActionText;
		private Throwable exception;


		public IntentBuilder(@NonNull Context context) {
			this.context = context;
		}

		public IntentBuilder setTitle(@StringRes int resTitle) {
			this.title = resTitle;
			return this;
		}

		public IntentBuilder setText(@StringRes int text) {
			this.text = text;
			return this;
		}

		public IntentBuilder setAdditionalMessage(@Nullable String additionalMessage) {
			this.additionalMessage = additionalMessage;
			return this;
		}

		public IntentBuilder setNextAction(@Nullable Intent nextAction) {
			this.nextAction = nextAction;
			return this;
		}

		public IntentBuilder setNextActionText(@StringRes int nextActionText) {
			this.nextActionText = nextActionText;
			return this;
		}

		public IntentBuilder setException(@Nullable Throwable exception) {
			this.exception = exception;
			return this;
		}

		@Override
		public Intent build() {
			Bundle args = new Bundle();
			args.putInt(BUNDLE_RESOURCE_TITLE, title);
			args.putInt(BUNDLE_RESOURCE_TEXT, text);
			args.putCharSequence(BUNDLE_ADDITIONAL_MESSAGE, additionalMessage);
			args.putParcelable(BUNDLE_NEXT_ACTION, nextAction);
			args.putInt(BUNDLE_NEXT_ACTION_TEXT, nextActionText);
			args.putSerializable(BUNDLE_EXCEPTION, exception);

			return new Intent(context, ErrorActivity.class)
					.putExtra(PARAM_ARGUMENTS, args);
		}
	}
}
