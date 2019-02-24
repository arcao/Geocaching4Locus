package com.arcao.feedback

import android.os.Build
import com.arcao.feedback.collector.AccountInfoCollector
import com.arcao.feedback.collector.AppInfoCollector
import com.arcao.feedback.collector.BuildConfigCollector
import com.arcao.feedback.collector.ConfigurationCollector
import com.arcao.feedback.collector.ConstantsCollector
import com.arcao.feedback.collector.DisplayManagerCollector
import com.arcao.feedback.collector.LocusMapInfoCollector
import com.arcao.feedback.collector.LogCatCollector
import com.arcao.feedback.collector.MemoryCollector
import com.arcao.feedback.collector.SharedPreferencesCollector
import org.koin.dsl.module.module

const val DEP_FEEDBACK_COLLECTORS = "feedbackCollectors"

internal val feedbackModule = module {
    single(DEP_FEEDBACK_COLLECTORS) {
        arrayOf(
            AppInfoCollector(get()),
            BuildConfigCollector(),
            ConfigurationCollector(get()),
            ConstantsCollector(Build::class.java, "BUILD"),
            ConstantsCollector(Build.VERSION::class.java, "VERSION"),
            MemoryCollector(),
            SharedPreferencesCollector(get()),
            DisplayManagerCollector(get()),
            AccountInfoCollector(get(), get()),
            LocusMapInfoCollector(get()),

            // LogCat collector has to be the latest one to receive exceptions from collectors
            LogCatCollector()
        )
    }
    single { FeedbackHelper(get(), get(DEP_FEEDBACK_COLLECTORS), get()) }
}