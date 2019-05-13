package com.arcao.geocaching4locus.settings.fragment.filter

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.preference.CheckBoxPreference
import com.arcao.geocaching.api.data.type.GeocacheType
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.base.constants.PrefConstants.FILTER_CACHE_TYPE_PREFIX
import com.arcao.geocaching4locus.base.fragment.AbstractPreferenceFragment

class CacheTypeFilterPreferenceFragment : AbstractPreferenceFragment() {
    override val preferenceResource: Int
        get() = R.xml.preference_category_filter_cache_type

    override fun preparePreference() {
        super.preparePreference()

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_select_deselect, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val geocacheTypeLength = GeocacheType.values().size

        when (item.itemId) {
            android.R.id.home -> {
                // app icon in action bar clicked; go home
                requireActivity().finish()
                return true
            }

            R.id.selectAll -> {
                for (i in 0 until geocacheTypeLength)
                    preference<CheckBoxPreference>(FILTER_CACHE_TYPE_PREFIX + i).isChecked = true
                return true
            }

            R.id.deselectAll -> {
                for (i in 0 until geocacheTypeLength)
                    preference<CheckBoxPreference>(FILTER_CACHE_TYPE_PREFIX + i).isChecked = false
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }
}
