package com.arcao.geocaching4locus.search_nearest

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.authentication.LoginActivity
import com.arcao.geocaching4locus.base.AbstractActionBarActivity
import com.arcao.geocaching4locus.base.fragment.SliderDialogFragment
import com.arcao.geocaching4locus.base.util.PermissionUtil
import com.arcao.geocaching4locus.base.util.exhaustive
import com.arcao.geocaching4locus.base.util.invoke
import com.arcao.geocaching4locus.base.util.showLocusMissingError
import com.arcao.geocaching4locus.base.util.withObserve
import com.arcao.geocaching4locus.databinding.ActivitySearchNearestBinding
import com.arcao.geocaching4locus.error.ErrorActivity
import com.arcao.geocaching4locus.search_nearest.fragment.NoLocationPermissionErrorDialogFragment
import com.arcao.geocaching4locus.search_nearest.fragment.NoLocationProviderDialogFragment
import com.arcao.geocaching4locus.settings.SettingsActivity
import com.arcao.geocaching4locus.settings.fragment.FilterPreferenceFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class SearchNearestActivity : AbstractActionBarActivity(), SliderDialogFragment.DialogListener {
    private val viewModel by viewModel<SearchNearestViewModel> {
        parametersOf(intent)
    }

    private lateinit var binding: ActivitySearchNearestBinding

    private val settingsActivity = registerForActivityResult(SettingsActivity.Contract) {}
    private val requestLocationPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.all { it.value }) {
            viewModel.retrieveCoordinates()
        } else {
            NoLocationPermissionErrorDialogFragment.newInstance().show(supportFragmentManager)
        }
    }

    private val loginActivity = registerForActivityResult(LoginActivity.Contract) { success ->
        if (success) viewModel.download()
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_search_nearest)
        binding.lifecycleOwner = this
        binding.vm = viewModel

        @Suppress("USELESS_CAST")
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
            SearchNearestAction.SignIn -> loginActivity.launch(null)
            is SearchNearestAction.Error -> {
                startActivity(action.intent)
                setResult(Activity.RESULT_CANCELED)
            }
            is SearchNearestAction.Finish -> {
                startActivity(action.intent)
                setResult(Activity.RESULT_OK)
                finish()
            }
            is SearchNearestAction.LocusMapNotInstalled -> showLocusMissingError()
            SearchNearestAction.RequestGpsLocationPermission -> requestLocationPermission.launch(
                PermissionUtil.PERMISSION_LOCATION_GPS
            )
            SearchNearestAction.RequestWifiLocationPermission -> requestLocationPermission.launch(
                PermissionUtil.PERMISSION_LOCATION_WIFI
            )
            SearchNearestAction.WrongCoordinatesFormat -> startActivity(
                ErrorActivity.IntentBuilder(this)
                    .message(R.string.error_coordinates_format)
                    .build()
            )
            SearchNearestAction.ShowFilters -> {
                settingsActivity.launch(FilterPreferenceFragment::class.java)
            }
            SearchNearestAction.LocationProviderDisabled ->
                NoLocationProviderDialogFragment.newInstance().show(supportFragmentManager)
            is SearchNearestAction.RequestCacheCount -> SliderDialogFragment.newInstance(
                title = R.string.title_geocache_count,
                min = action.step,
                max = action.max,
                step = action.step,
                defaultValue = action.value
            ).show(supportFragmentManager, "COUNTER")
        }.exhaustive
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_search_nearest, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.main_activity_option_menu_preferences -> {
            settingsActivity.launch(null)
            true
        }
        android.R.id.home -> {
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onProgressCancel(requestId: Int) {
        viewModel.cancelProgress()
    }

    // ---------------- SliderDialogFragment.DialogListener methods ----------------
    override fun onDialogClosed(fragment: SliderDialogFragment) {
        viewModel.requestedCaches(fragment.getValue())
    }

    object Contract : ActivityResultContract<Intent?, Boolean>() {
        override fun createIntent(context: Context, input: Intent?) =
            Intent(context, SearchNearestActivity::class.java).apply {
                input?.let { putExtras(it) }
            }

        override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
            return resultCode == Activity.RESULT_OK
        }
    }
}
