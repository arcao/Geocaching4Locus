package com.arcao.geocaching4locus.base.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.base.util.ResourcesUtil;
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

	protected void onDialogBuild(MaterialDialog.Builder builder) {
		Bundle args = getArguments();

		builder.content(ResourcesUtil.getText(getActivity(), args.getInt(PARAM_ERROR_MESSAGE),
				StringUtils.defaultString(args.getString(PARAM_ADDITIONAL_MESSAGE))))
				.positiveText(R.string.button_ok)
				.onPositive(new MaterialDialog.SingleButtonCallback() {
					@Override
					public void onClick(@NonNull MaterialDialog materialDialog,
							@NonNull DialogAction dialogAction) {
						onPositiveButtonClick();
					}
				});

		int title = args.getInt(PARAM_TITLE);
		if (title != 0) {
			builder.title(title);
		}
	}

	@NonNull
	@Override
	public final Dialog onCreateDialog(Bundle savedInstanceState) {
		MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
		onDialogBuild(builder);
		return builder.build();
	}
}
