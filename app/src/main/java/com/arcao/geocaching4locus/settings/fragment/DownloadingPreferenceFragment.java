package com.arcao.geocaching4locus.settings.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;

import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.base.fragment.AbstractPreferenceFragment;
import com.arcao.geocaching4locus.settings.widget.ListPreference;
import com.arcao.geocaching4locus.settings.widget.SliderPreference;

public class DownloadingPreferenceFragment extends AbstractPreferenceFragment {
    private boolean premiumMember;

    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preference_category_downloading);

        premiumMember = App.get(getActivity()).getAccountManager().isPremium();
    }

    @Override
    protected void preparePreference() {
        final CheckBoxPreference simpleCacheDataPreference = findPreference(DOWNLOADING_SIMPLE_CACHE_DATA, CheckBoxPreference.class);
        final ListPreference fullCacheDataOnShowPreference = findPreference(DOWNLOADING_FULL_CACHE_DATE_ON_SHOW, ListPreference.class);
        final SliderPreference downloadingCountOfLogsPreference = findPreference(DOWNLOADING_COUNT_OF_LOGS, SliderPreference.class);
        final ListPreference countOfCachesStepPreference = findPreference(DOWNLOADING_COUNT_OF_CACHES_STEP, ListPreference.class);
        final CheckBoxPreference disableDnfNmNaCachesPreference = findPreference(DOWNLOADING_DISABLE_DNF_NM_NA_CACHES, CheckBoxPreference.class);
        final SliderPreference disableDnfNmNaCachesLogsCountPreference = findPreference(DOWNLOADING_DISABLE_DNF_NM_NA_CACHES_LOGS_COUNT, SliderPreference.class);

        simpleCacheDataPreference.setEnabled(premiumMember);
        disableDnfNmNaCachesPreference.setEnabled(premiumMember);

        simpleCacheDataPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            fullCacheDataOnShowPreference.setEnabled((Boolean) newValue);
            return true;
        });

        fullCacheDataOnShowPreference.setEnabled(simpleCacheDataPreference.isChecked() && premiumMember);
        fullCacheDataOnShowPreference.setSummary(preparePreferenceSummary(fullCacheDataOnShowPreference.getEntry(), R.string.pref_download_on_show_summary));

        downloadingCountOfLogsPreference.setEnabled(premiumMember);
        downloadingCountOfLogsPreference.setSummary(preparePreferenceSummary(String.valueOf(downloadingCountOfLogsPreference.getProgress()),
                R.string.pref_logs_count_summary));

        countOfCachesStepPreference.setSummary(preparePreferenceSummary(countOfCachesStepPreference.getEntry(), R.string.pref_step_geocaching_count_summary));
        disableDnfNmNaCachesLogsCountPreference.setSummary(preparePreferenceSummary(String.valueOf(disableDnfNmNaCachesLogsCountPreference.getProgress()), 0));

        if (!premiumMember) {
            applyPremiumTitleSign(simpleCacheDataPreference);
            applyPremiumTitleSign(fullCacheDataOnShowPreference);
            applyPremiumTitleSign(downloadingCountOfLogsPreference);
            applyPremiumTitleSign(disableDnfNmNaCachesPreference);
            applyPremiumTitleSign(disableDnfNmNaCachesLogsCountPreference);
            disableDnfNmNaCachesPreference.setChecked(false);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        super.onSharedPreferenceChanged(sharedPreferences, key);

        if (key == null)
            return;

        switch (key) {
            case DOWNLOADING_COUNT_OF_LOGS:
                SliderPreference countOfLogsPreference = findPreference(key, SliderPreference.class);
                SliderPreference disableDnfNmNaCachesLogsCountPreference = findPreference(DOWNLOADING_DISABLE_DNF_NM_NA_CACHES_LOGS_COUNT, SliderPreference.class);
                CheckBoxPreference disableDnfNmNaCachesPreference = findPreference(DOWNLOADING_DISABLE_DNF_NM_NA_CACHES, CheckBoxPreference.class);

                int newIntValue = countOfLogsPreference.getProgress();
                countOfLogsPreference.setSummary(preparePreferenceSummary(String.valueOf(newIntValue), R.string.pref_logs_count_summary));

                // additional checking if enough logs will be downloaded
                if (disableDnfNmNaCachesPreference.isChecked() && disableDnfNmNaCachesLogsCountPreference.getProgress() > newIntValue) {
                    if (newIntValue > 1) {
                        disableDnfNmNaCachesLogsCountPreference.setProgress(newIntValue);
                    } else {
                        disableDnfNmNaCachesPreference.setChecked(false);
                        disableDnfNmNaCachesLogsCountPreference.setProgress(1);
                    }
                }
                break;

            case DOWNLOADING_COUNT_OF_CACHES_STEP:
                final ListPreference countOfCachesStepPreference = findPreference(key, ListPreference.class);
                countOfCachesStepPreference.setSummary(preparePreferenceSummary(countOfCachesStepPreference.getEntry(), R.string.pref_step_geocaching_count_summary));
                break;

            case DOWNLOADING_FULL_CACHE_DATE_ON_SHOW:
                final ListPreference fullCacheDataOnShowPreference = findPreference(key, ListPreference.class);
                fullCacheDataOnShowPreference.setSummary(preparePreferenceSummary(fullCacheDataOnShowPreference.getEntry(), R.string.pref_download_on_show_summary));
                break;

            case DOWNLOADING_DISABLE_DNF_NM_NA_CACHES_LOGS_COUNT:
                disableDnfNmNaCachesLogsCountPreference = findPreference(key, SliderPreference.class);
                countOfLogsPreference = findPreference(DOWNLOADING_COUNT_OF_LOGS, SliderPreference.class);
                disableDnfNmNaCachesPreference = findPreference(DOWNLOADING_DISABLE_DNF_NM_NA_CACHES, CheckBoxPreference.class);

                newIntValue = disableDnfNmNaCachesLogsCountPreference.getProgress();
                disableDnfNmNaCachesLogsCountPreference.setSummary(preparePreferenceSummary(String.valueOf(newIntValue), 0));

                // additional checking if enough logs will be downloaded
                if (disableDnfNmNaCachesPreference.isChecked() && newIntValue > countOfLogsPreference.getProgress()) {
                    countOfLogsPreference.setProgress(newIntValue);
                }
                break;

            case DOWNLOADING_DISABLE_DNF_NM_NA_CACHES:
                disableDnfNmNaCachesPreference = findPreference(key, CheckBoxPreference.class);
                disableDnfNmNaCachesLogsCountPreference = findPreference(DOWNLOADING_DISABLE_DNF_NM_NA_CACHES_LOGS_COUNT, SliderPreference.class);
                countOfLogsPreference = findPreference(DOWNLOADING_COUNT_OF_LOGS, SliderPreference.class);

                // additional checking if enough logs will be downloaded
                if (disableDnfNmNaCachesPreference.isChecked() && disableDnfNmNaCachesLogsCountPreference.getProgress() > countOfLogsPreference.getProgress()) {
                    countOfLogsPreference.setProgress(disableDnfNmNaCachesLogsCountPreference.getProgress());
                }
                break;
        }
    }
}
