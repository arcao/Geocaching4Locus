package com.arcao.geocaching4locus.live_map.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.base.constants.AppConstants;
import com.arcao.geocaching4locus.base.constants.PrefConstants;
import com.arcao.geocaching4locus.base.fragment.AbstractErrorDialogFragment;
import com.arcao.geocaching4locus.base.util.IntentUtil;

public class PowerSaveWarningDialogFragment extends AbstractErrorDialogFragment {
    private static final String FRAGMENT_TAG = PowerSaveWarningDialogFragment.class.getName();

    public interface OnPowerSaveWarningConfirmedListener {
        void onPowerSaveWarningConfirmed();
    }

    public static PowerSaveWarningDialogFragment newInstance() {
        PowerSaveWarningDialogFragment fragment = new PowerSaveWarningDialogFragment();
        fragment.prepareDialog(R.string.title_warning, R.string.warning_power_management, null);

        return fragment;
    }

    @Override
    protected void onPositiveButtonClick() {
        Activity activity = getActivity();

        if (activity instanceof OnPowerSaveWarningConfirmedListener) {
            ((OnPowerSaveWarningConfirmedListener) activity).onPowerSaveWarningConfirmed();
        }
    }

    @Override
    protected void onDialogBuild(MaterialDialog.Builder builder) {
        super.onDialogBuild(builder);

        builder.neutralText(R.string.button_more_info).onNeutral((dialog, which) ->
                IntentUtil.showWebPage(getActivity(), AppConstants.POWER_SAVE_INFO_URI));

        builder.checkBoxPromptRes(R.string.checkbox_do_not_show_again, false, (buttonView, isChecked) -> {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(buttonView.getContext());
            preferences.edit().putBoolean(PrefConstants.HIDE_POWER_MANAGEMENT_WARNING, isChecked).apply();
        });
    }

    public static boolean showIfNeeded(Activity activity) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);

        if (preferences.getBoolean(PrefConstants.HIDE_POWER_MANAGEMENT_WARNING, false))
            return false;

        if (!isPowerSaveActive(activity)) return false;

        newInstance().show(activity.getFragmentManager(), FRAGMENT_TAG);
        return true;
    }

    private static boolean isPowerSaveActive(Context context) {
        return isHuaweiPowerManagerPresent(context) || isXaomiPowerManagerPresent(context);
    }

    private static boolean isHuaweiPowerManagerPresent(Context context) {
        return IntentUtil.isIntentCallable(context, new Intent().setClassName(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.optimize.process.ProtectActivity"
        ));
    }


    private static boolean isXaomiPowerManagerPresent(Context context) {
        return IntentUtil.isIntentCallable(context, new Intent().setClassName(
                "com.miui.powerkeeper",
                "com.miui.powerkeeper.ui.HiddenAppsContainerManagementActivity"
        ));
    }
}
