package com.arcao.geocaching4locus.fragment.dialog;

import com.arcao.geocaching4locus.R;

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
  }
}
