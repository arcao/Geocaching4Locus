package com.arcao.geocaching4locus.fragment.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import com.afollestad.materialdialogs.MaterialDialog;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.util.SpannedFix;
import org.apache.commons.lang3.StringUtils;

public class AbstractErrorDialogFragment extends AbstractDialogFragment {
	private static final String PARAM_TITLE = "TITLE";
	private static final String PARAM_ERROR_MESSAGE = "ERROR_MESSAGE";
	private static final String PARAM_ADDITIONAL_MESSAGE = "ADDITIONAL_MESSAGE";

	protected void prepareDialog(@StringRes int resTitle, @StringRes int resErrorMessage, @Nullable String additionalMessage) {
		Bundle args = new Bundle();
		args.putInt(PARAM_TITLE, resTitle);
		args.putInt(PARAM_ERROR_MESSAGE, resErrorMessage);
		args.putString(PARAM_ADDITIONAL_MESSAGE, additionalMessage);
		setArguments(args);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setCancelable(false);
	}

	protected void onPositiveButtonClick() {
		// do nothing
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Bundle args = getArguments();

		MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
						.content(SpannedFix.fromHtml(getString(args.getInt(PARAM_ERROR_MESSAGE),
								StringUtils.defaultString(args.getString(PARAM_ADDITIONAL_MESSAGE)))))
						.positiveText(R.string.ok_button)
						.callback(new MaterialDialog.ButtonCallback() {
							@Override
							public void onPositive(MaterialDialog dialog) {
								onPositiveButtonClick();
							}
						});


		int title = args.getInt(PARAM_TITLE);
		if (title != 0) {
			builder.title(title);
		}

		return builder.build();
	}
}
