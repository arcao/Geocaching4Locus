package com.arcao.geocaching4locus.settings.fragment

import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.base.fragment.AbstractPreferenceFragment

/**
 * Created by Arcao on 15.10.2018.
 */
class SettingsPreferenceFragment : AbstractPreferenceFragment() {
    override val preferenceResource: Int
        get() = R.xml.preference_categories
}
