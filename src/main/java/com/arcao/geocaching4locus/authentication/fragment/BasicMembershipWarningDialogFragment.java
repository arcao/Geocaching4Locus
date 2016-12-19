package com.arcao.geocaching4locus.authentication.fragment;

import android.support.annotation.NonNull;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.base.fragment.AbstractErrorDialogFragment;
import com.arcao.geocaching4locus.base.constants.AppConstants;
import com.arcao.geocaching4locus.base.util.IntentUtil;

public class BasicMembershipWarningDialogFragment extends AbstractErrorDialogFragment {
  public static final String FRAGMENT_TAG = BasicMembershipWarningDialogFragment.class.getName();

  public static BasicMembershipWarningDialogFragment newInstance() {
    BasicMembershipWarningDialogFragment fragment = new BasicMembershipWarningDialogFragment();
    fragment.prepareDialog(R.string.title_basic_member_warning, R.string.warning_basic_member, null);
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

    builder.neutralText(R.string.button_show_manual)
        .onNeutral(new MaterialDialog.SingleButtonCallback() {
          @Override
          public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
            IntentUtil.showWebPage(getActivity(), AppConstants.MANUAL_URI);
          }
        });
  }
}
