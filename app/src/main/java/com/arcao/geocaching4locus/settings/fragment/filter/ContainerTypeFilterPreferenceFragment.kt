package com.arcao.geocaching4locus.settings.fragment.filter

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.MenuProvider
import androidx.preference.CheckBoxPreference
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.base.constants.AppConstants
import com.arcao.geocaching4locus.base.constants.PrefConstants.FILTER_CONTAINER_TYPE_PREFIX
import com.arcao.geocaching4locus.base.fragment.AbstractPreferenceFragment

class ContainerTypeFilterPreferenceFragment : AbstractPreferenceFragment() {
    override val preferenceResource: Int
        get() = R.xml.preference_category_filter_container_type

    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.toolbar_select_deselect, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            val containerTypeLength = AppConstants.GEOCACHE_SIZES.size

            return when (menuItem.itemId) {
                android.R.id.home -> {
                    // app icon in action bar clicked; go home
                    requireActivity().finish()
                    true
                }

                R.id.selectAll -> {
                    for (i in 0 until containerTypeLength)
                        preference<CheckBoxPreference>(FILTER_CONTAINER_TYPE_PREFIX + i).isChecked =
                            true
                    true
                }

                R.id.deselectAll -> {
                    for (i in 0 until containerTypeLength)
                        preference<CheckBoxPreference>(FILTER_CONTAINER_TYPE_PREFIX + i).isChecked =
                            false
                    true
                }

                else -> false
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().addMenuProvider(menuProvider, viewLifecycleOwner)
    }
}
