package com.arcao.geocaching4locus.fragment.dialog;

import android.support.annotation.NonNull;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.util.IntentUtil;

public class BasicMembershipWarningDialogFragment extends AbstractErrorDialogFragment {
  public static final String FRAGMENT_TAG = BasicMembershipWarningDialogFragment.class.getName();

  public static BasicMembershipWarningDialogFragment newInstance() {
    BasicMembershipWarningDialogFragment fragment = new BasicMembershipWarningDialogFragment();
    fragment.prepareDialog(R.string.basic_member_warning_title, R.string.basic_member_warning_message, null);
    return fragment;
  }

  @Override
  protected void onPositiveButtonClick() {
    super.onPositiveButtonClick();
    getActivity().finish();
    dismiss();
  }

  @Override
  protected void onDialogBuild(MaterialDialog.Builder builder) {
    super.onDialogBuild(builder);

    // disable auto dismiss
    builder.autoDismiss(false);

    builder.neutralText(R.string.button_users_guide)
        .onNeutral(new MaterialDialog.SingleButtonCallback() {
          @Override
          public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
            IntentUtil.showWebPage(getActivity(), AppConstants.MANUAL_URI);
          }
        });
  }
}
