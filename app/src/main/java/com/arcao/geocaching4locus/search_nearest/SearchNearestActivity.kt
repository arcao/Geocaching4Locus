package com.arcao.geocaching4locus.search_nearest

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.authentication.util.requestSignOn
import com.arcao.geocaching4locus.base.AbstractActionBarActivity
import com.arcao.geocaching4locus.base.fragment.SliderDialogFragment
import com.arcao.geocaching4locus.base.util.PermissionUtil
import com.arcao.geocaching4locus.base.util.exhaustive
import com.arcao.geocaching4locus.base.util.invoke
import com.arcao.geocaching4locus.base.util.showLocusMissingError
import com.arcao.geocaching4locus.base.util.withObserve
import com.arcao.geocaching4locus.data.account.AccountManager
import com.arcao.geocaching4locus.databinding.ActivitySearchNearestBinding
import com.arcao.geocaching4locus.error.ErrorActivity
import com.arcao.geocaching4locus.search_nearest.fragment.NoLocationPermissionErrorDialogFragment
import com.arcao.geocaching4locus.search_nearest.fragment.NoLocationProviderDialogFragment
import com.arcao.geocaching4locus.settings.SettingsActivity
import com.arcao.geocaching4locus.settings.fragment.FilterPreferenceFragment
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class SearchNearestActivity : AbstractActionBarActivity(), SliderDialogFragment.DialogListener {
    private val viewModel by viewModel<SearchNearestViewModel> {
        parametersOf(intent)
    }

    private val accountManager by inject<AccountManager>()

    private lateinit var binding: ActivitySearchNearestBinding

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_search_nearest)
        binding.lifecycleOwner = this
        binding.vm = viewModel

        val toolbar = binding.toolbar as Toolbar
        val latitude = binding.incCoordinates.latitude
        val longitude = binding.incCoordinates.longitude

        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.title = title
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        latitude.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) viewModel.formatCoordinates()
        }
        longitude.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) viewModel.formatCoordinates()
        }

        viewModel.action.withObserve(this, ::handleAction)
        viewModel.progress.withObserve(this, ::handleProgress)
    }

    @Suppress("IMPLICIT_CAST_TO_ANY")
    fun handleAction(action: SearchNearestAction) {
        when (action) {
            SearchNearestAction.SignIn -> {
                accountManager.requestSignOn(this, REQUEST_SIGN_ON)
            }
            is SearchNearestAction.Error -> {
                startActivity(action.intent)
                setResult(Activity.RESULT_CANCELED)
            }
            is SearchNearestAction.Finish -> {
                startActivity(action.intent)
                setResult(Activity.RESULT_OK)
                finish()
            }
            is SearchNearestAction.LocusMapNotInstalled -> {
                showLocusMissingError()
            }
            SearchNearestAction.RequestGpsLocationPermission -> {
                PermissionUtil.requestGpsLocationPermission(this, REQUEST_LOCATION_PERMISSION)
            }
            SearchNearestAction.RequestWifiLocationPermission -> {
                PermissionUtil.requestWifiLocationPermission(this, REQUEST_LOCATION_PERMISSION)
            }
            SearchNearestAction.WrongCoordinatesFormat -> {
                startActivity(ErrorActivity.IntentBuilder(this).message(R.string.error_coordinates_format).build())
            }
            SearchNearestAction.ShowFilters -> {
                startActivity(SettingsActivity.createIntent(this, FilterPreferenceFragment::class.java))
            }
            SearchNearestAction.LocationProviderDisabled -> {
                NoLocationProviderDialogFragment.newInstance().show(supportFragmentManager)
            }
            is SearchNearestAction.RequestCacheCount -> {
                SliderDialogFragment.newInstance(
                    title = R.string.title_geocache_count,
                    min = action.step,
                    max = action.max,
                    step = action.step,
                    defaultValue = action.value
                ).show(supportFragmentManager, "COUNTER")
            }
        }.exhaustive
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_search_nearest, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.main_activity_option_menu_preferences -> {
            startActivity(SettingsActivity.createIntent(this))
            true
        }
        android.R.id.home -> {
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // restart download process after log in
        if (requestCode == REQUEST_SIGN_ON && resultCode == Activity.RESULT_OK) {
            viewModel.download()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (PermissionUtil.verifyPermissions(grantResults)) {
                viewModel.retrieveCoordinates()
            } else {
                NoLocationPermissionErrorDialogFragment.newInstance().show(supportFragmentManager)
            }
        }
    }

    override fun onProgressCancel(requestId: Int) {
        viewModel.cancelProgress()
    }

    // ---------------- SliderDialogFragment.DialogListener methods ----------------
    override fun onDialogClosed(fragment: SliderDialogFragment) {
        viewModel.requestedCaches(fragment.getValue())
    }

    companion object {
        private const val REQUEST_SIGN_ON = 1
        private const val REQUEST_LOCATION_PERMISSION = 2
    }
}
