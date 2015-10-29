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
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.TextView;
import android.widget.Toast;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.arcao.geocaching4locus.fragment.dialog.AbstractDialogFragment;
import com.arcao.geocaching4locus.util.SpannedFix;
import com.crashlytics.android.Crashlytics;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.Builder;
import timber.log.Timber;

public class ErrorActivity extends AppCompatActivity {
	private static final String KEY_TITLE = "TITLE";
	private static final String KEY_MESSAGE = "MESSAGE";
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
		Bundle args = getIntent().getExtras();
		if (args == null) {
			finish();
			return;
		}

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
			if (args == null) {
				Timber.e("getArguments() is null");
			}

			final CharSequence title = args != null ? args.getCharSequence(KEY_TITLE) : null;
			final CharSequence message = args != null ? args.getCharSequence(KEY_MESSAGE) : StringUtils.EMPTY;
			final Intent positiveAction = args != null ? args.<Intent>getParcelable(KEY_POSITIVE_ACTION) : null;
			int positiveButtonText = args != null ? args.getInt(KEY_POSITIVE_BUTTON_TEXT) : 0;
			int negativeButtonText = args != null ? args.getInt(KEY_NEGATIVE_BUTTON_TEXT) : 0;
			final Throwable t = (Throwable) (args != null ? args.getSerializable(KEY_EXCEPTION) : null);

			MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
					.positiveText(positiveButtonText != 0 ? positiveButtonText : R.string.ok_button);

			if (!TextUtils.isEmpty(title)) {
				builder.title(title);
			}

			builder.customView(R.layout.dialog_error, false);

			if (negativeButtonText != 0) {
				builder.negativeText(negativeButtonText);
			}

			builder.onPositive(new MaterialDialog.SingleButtonCallback() {
				@Override
				public void onClick(@NonNull MaterialDialog materialDialog,
						@NonNull DialogAction dialogAction) {
					if (materialDialog.getCustomView() != null) {
						final Checkable checkBox = (Checkable) materialDialog.getCustomView().findViewById(R.id.checkbox);
						if (checkBox != null && checkBox.isChecked()) {
							Crashlytics.logException(t);
							Toast.makeText(materialDialog.getContext(), R.string.error_report_sent_toast, Toast.LENGTH_LONG)
											.show();
						}
					}

					if (positiveAction != null) {
						startActivity(positiveAction);
					}
					getActivity().finish();
				}
			});
			builder.onNegative(new MaterialDialog.SingleButtonCallback() {
				@Override
				public void onClick(@NonNull MaterialDialog materialDialog,
						@NonNull DialogAction dialogAction) {
					getActivity().finish();
				}
			});

			final MaterialDialog dialog = builder.build();
			View rootView = dialog.getCustomView();
			if (rootView != null) {
				TextView content = (TextView) rootView.findViewById(R.id.content);
				CheckBox checkBox = (CheckBox) rootView.findViewById(R.id.checkbox);

				// add paddingTop for untitled dialogs
				if (TextUtils.isEmpty(title)) {
					rootView.setPadding(0, dialog.getContext().getResources()
									.getDimensionPixelSize(com.afollestad.materialdialogs.R.dimen.md_notitle_vertical_padding), 0, 0);
				}

				if (content != null && !TextUtils.isEmpty(message)) {
					dialog.setTypeface(content, builder.getRegularFont());
					content.setMovementMethod(new LinkMovementMethod());
					content.setText(SpannedFix.applyFix(message));
				}

				if (checkBox != null) {
					dialog.setTypeface(checkBox, builder.getRegularFont());
					checkBox.setVisibility(t != null ? View.VISIBLE : View.GONE);
				}
			}

			return dialog;
		}
	}

	public static class IntentBuilder implements Builder<Intent> {
		private final Context context;
		private CharSequence title = null;
		private CharSequence message = null;
		private Intent positiveAction = null;
		private int positiveButtonText = 0;
		private int negativeButtonText = 0;
		private Throwable exception = null;

		public IntentBuilder(@NonNull Context context) {
			this.context = context;
		}

		public IntentBuilder setTitle(@StringRes int resTitle) {
			this.title = context.getString(resTitle);
			return this;
		}

		public IntentBuilder setMessage(@StringRes int message, Object... params) {
			this.message = Html.fromHtml(context.getString(message, params));
			return this;
		}

		public IntentBuilder setMessage(String message, Object... params) {
			this.message = Html.fromHtml(String.format(message, params));
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
			return new Intent(context, ErrorActivity.class)
					.putExtra(KEY_TITLE, title)
					.putExtra(KEY_MESSAGE, message)
					.putExtra(KEY_POSITIVE_ACTION, positiveAction)
					.putExtra(KEY_POSITIVE_BUTTON_TEXT, positiveButtonText)
					.putExtra(KEY_NEGATIVE_BUTTON_TEXT, negativeButtonText)
					.putExtra(KEY_EXCEPTION, exception);
		}
	}
}
