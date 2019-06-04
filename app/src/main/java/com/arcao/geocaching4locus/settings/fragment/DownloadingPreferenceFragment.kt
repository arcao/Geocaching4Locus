package com.arcao.geocaching4locus.settings.fragment

import android.content.SharedPreferences
import androidx.preference.CheckBoxPreference
import androidx.preference.ListPreference
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.authentication.util.isPremium
import com.arcao.geocaching4locus.base.constants.PrefConstants.DOWNLOADING_COUNT_OF_CACHES_STEP
import com.arcao.geocaching4locus.base.constants.PrefConstants.DOWNLOADING_COUNT_OF_LOGS
import com.arcao.geocaching4locus.base.constants.PrefConstants.DOWNLOADING_DISABLE_DNF_NM_NA_CACHES
import com.arcao.geocaching4locus.base.constants.PrefConstants.DOWNLOADING_DISABLE_DNF_NM_NA_CACHES_LOGS_COUNT
import com.arcao.geocaching4locus.base.constants.PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW
import com.arcao.geocaching4locus.base.constants.PrefConstants.DOWNLOADING_SIMPLE_CACHE_DATA
import com.arcao.geocaching4locus.base.fragment.AbstractPreferenceFragment
import com.arcao.geocaching4locus.data.account.AccountManager
import com.arcao.geocaching4locus.settings.widget.SliderPreference
import org.koin.android.ext.android.get

class DownloadingPreferenceFragment : AbstractPreferenceFragment() {

    override val preferenceResource: Int
        get() = R.xml.preference_category_downloading

    override fun preparePreference() {
        super.preparePreference()

        val premiumMember: Boolean = get<AccountManager>().isPremium

        val simpleCacheDataPreference = preference<CheckBoxPreference>(DOWNLOADING_SIMPLE_CACHE_DATA)
        val fullCacheDataOnShowPreference = preference<ListPreference>(DOWNLOADING_FULL_CACHE_DATE_ON_SHOW)
        val downloadingCountOfLogsPreference = preference<SliderPreference>(DOWNLOADING_COUNT_OF_LOGS)
        val countOfCachesStepPreference = preference<ListPreference>(DOWNLOADING_COUNT_OF_CACHES_STEP)
        val disableDnfNmNaCachesPreference = preference<CheckBoxPreference>(DOWNLOADING_DISABLE_DNF_NM_NA_CACHES)
        val disableDnfNmNaCachesLogsCountPreference =
            preference<SliderPreference>(DOWNLOADING_DISABLE_DNF_NM_NA_CACHES_LOGS_COUNT)

        simpleCacheDataPreference.apply {
            isEnabled = premiumMember
            setOnPreferenceChangeListener { _, newValue ->
                fullCacheDataOnShowPreference.isEnabled = newValue as Boolean
                true
            }
        }

        fullCacheDataOnShowPreference.apply {
            isEnabled = simpleCacheDataPreference.isChecked && premiumMember
            summary = preparePreferenceSummary(entry, R.string.pref_download_on_show_summary)
        }

        downloadingCountOfLogsPreference.apply {
            isEnabled = premiumMember
            summary = preparePreferenceSummary(progress.toString(), R.string.pref_logs_count_summary)
        }

        countOfCachesStepPreference.apply {
            summary = preparePreferenceSummary(entry, R.string.pref_step_geocaching_count_summary)
        }

        disableDnfNmNaCachesPreference.isEnabled = premiumMember

        disableDnfNmNaCachesLogsCountPreference.apply {
            summary = preparePreferenceSummary(progress.toString(), 0)
        }

        if (!premiumMember) {
            applyPremiumTitleSign(simpleCacheDataPreference)
            applyPremiumTitleSign(fullCacheDataOnShowPreference)
            applyPremiumTitleSign(downloadingCountOfLogsPreference)
            applyPremiumTitleSign(disableDnfNmNaCachesPreference)
            applyPremiumTitleSign(disableDnfNmNaCachesLogsCountPreference)

            disableDnfNmNaCachesPreference.isChecked = false
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        super.onSharedPreferenceChanged(sharedPreferences, key)

        when (key) {
            DOWNLOADING_COUNT_OF_LOGS -> {
                val logsCountPreference = preference<SliderPreference>(key)
                val disableLogsCountPreference =
                    preference<SliderPreference>(DOWNLOADING_DISABLE_DNF_NM_NA_CACHES_LOGS_COUNT)
                val disablePreference = preference<CheckBoxPreference>(DOWNLOADING_DISABLE_DNF_NM_NA_CACHES)

                val logsCount = logsCountPreference.progress
                logsCountPreference.summary =
                    preparePreferenceSummary(logsCount.toString(), R.string.pref_logs_count_summary)

                // additional checking if enough logs will be downloaded
                if (disablePreference.isChecked && disableLogsCountPreference.progress > logsCount) {
                    if (logsCount > 1) {
                        disableLogsCountPreference.progress = logsCount
                    } else {
                        disablePreference.isChecked = false
                        disableLogsCountPreference.progress = 1
                    }
                }
            }

            DOWNLOADING_COUNT_OF_CACHES_STEP -> {
                preference<ListPreference>(key).apply {
                    summary = preparePreferenceSummary(entry, R.string.pref_step_geocaching_count_summary)
                }
            }

            DOWNLOADING_FULL_CACHE_DATE_ON_SHOW -> {
                preference<ListPreference>(key).apply {
                    summary = preparePreferenceSummary(entry, R.string.pref_download_on_show_summary)
                }
            }

            DOWNLOADING_DISABLE_DNF_NM_NA_CACHES_LOGS_COUNT -> {
                val disableLogsCountPreference = preference<SliderPreference>(key)
                val logsCountPreference = preference<SliderPreference>(DOWNLOADING_COUNT_OF_LOGS)
                val disablePreference = preference<CheckBoxPreference>(DOWNLOADING_DISABLE_DNF_NM_NA_CACHES)

                val logsCount = disableLogsCountPreference.progress
                disableLogsCountPreference.summary = preparePreferenceSummary(logsCount.toString(), 0)

                // additional checking if enough logs will be downloaded
                if (disablePreference.isChecked && logsCount > logsCountPreference.progress) {
                    logsCountPreference.progress = logsCount
                }
            }

            DOWNLOADING_DISABLE_DNF_NM_NA_CACHES -> {
                val disablePreference = preference<CheckBoxPreference>(key)
                val disableLogsCountPreference =
                    preference<SliderPreference>(DOWNLOADING_DISABLE_DNF_NM_NA_CACHES_LOGS_COUNT)
                val logsCountPreference = preference<SliderPreference>(DOWNLOADING_COUNT_OF_LOGS)

                // additional checking if enough logs will be downloaded
                if (disablePreference.isChecked && disableLogsCountPreference.progress > logsCountPreference.progress) {
                    logsCountPreference.progress = disableLogsCountPreference.progress
                }
            }
        }
    }
}
