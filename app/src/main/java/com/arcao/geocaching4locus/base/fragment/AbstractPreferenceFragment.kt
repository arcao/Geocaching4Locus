package com.arcao.geocaching4locus.base.fragment

import android.content.SharedPreferences
import android.os.Bundle
import androidx.annotation.XmlRes
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.arcao.geocaching4locus.base.constants.AppConstants
import com.arcao.geocaching4locus.base.util.HtmlUtil
import org.oshkimaadziig.george.androidutils.SpanFormatter

abstract class AbstractPreferenceFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {
    protected lateinit var preferences: SharedPreferences

    @get:XmlRes
    protected abstract val preferenceResource: Int

    protected inline fun <reified P : Preference> preference(key: CharSequence): P {
        return findPreference(key) as P
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(preferenceResource, rootKey)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        // empty
    }

    override fun onCreate(paramBundle: Bundle?) {
        super.onCreate(paramBundle)
        preferences = PreferenceManager.getDefaultSharedPreferences(activity)
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        preparePreference()
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    protected open fun preparePreference() {
        requireActivity().title = preferenceScreen.title
    }

    protected fun preparePreferenceSummary(value: CharSequence?, resId: Int): CharSequence {
        var summary: CharSequence? = null
        if (resId != 0)
            summary = getText(resId)

        return if (!value.isNullOrEmpty()) {
            SpanFormatter.format("%s %s", stylizedValue(value), summary ?: "")
        } else {
            summary ?: ""
        }
    }

    protected fun stylizedValue(value: CharSequence): CharSequence {
        return SpanFormatter.format(HtmlUtil.fromHtml(VALUE_HTML_FORMAT), value)
    }

    protected fun applyPremiumTitleSign(preference: Preference) {
        preference.title = String.format("%s %s", preference.title, AppConstants.PREMIUM_CHARACTER)
    }

    companion object {
        private const val VALUE_HTML_FORMAT = "<font color=\"#FF8000\"><b>%s</b></font>"
    }
}
