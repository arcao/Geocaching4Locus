package com.arcao.geocaching4locus.search_nearest

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.authentication.util.AccountManager
import com.arcao.geocaching4locus.base.AbstractActionBarActivity
import com.arcao.geocaching4locus.base.fragment.SliderDialogFragment
import com.arcao.geocaching4locus.base.util.PermissionUtil
import com.arcao.geocaching4locus.base.util.exhaustive
import com.arcao.geocaching4locus.base.util.invoke
import com.arcao.geocaching4locus.base.util.observe
import com.arcao.geocaching4locus.base.util.showLocusMissingError
import com.arcao.geocaching4locus.error.ErrorActivity
import com.arcao.geocaching4locus.error.fragment.ExternalStoragePermissionWarningDialogFragment
import com.arcao.geocaching4locus.error.fragment.NoExternalStoragePermissionErrorDialogFragment
import com.arcao.geocaching4locus.search_nearest.fragment.NoLocationPermissionErrorDialogFragment
import com.arcao.geocaching4locus.search_nearest.widget.SpinnerTextView
import com.arcao.geocaching4locus.settings.SettingsActivity
import com.arcao.geocaching4locus.settings.fragment.FilterPreferenceFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class SearchNearestActivity : AbstractActionBarActivity(), SliderDialogFragment.DialogListener {
    private val viewModel by viewModel<SearchNearestViewModel> {
        parametersOf(intent)
    }

    private val accountManager by inject<AccountManager>()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_search_nearest)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val spinner = findViewById<SpinnerTextView>(R.id.counter)
        val latitude = findViewById<TextView>(R.id.latitude)
        val longitude = findViewById<TextView>(R.id.longitude)
        val gps = findViewById<Button>(R.id.gps)
        val filter = findViewById<Button>(R.id.filter)
        val fab = findViewById<FloatingActionButton>(R.id.fab)

        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.title = title
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        viewModel.latitude.observe(this) { value ->
            latitude.text = value
        }
        latitude.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) viewModel.latitude(latitude.text ?: "")
        }
        viewModel.longitude.observe(this) { value ->
            longitude.text = value
        }
        longitude.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) viewModel.latitude(longitude.text ?: "")
        }

        viewModel.requestedCaches.observe(this) { value ->
            spinner.setText(value.toString())
        }
        spinner.setOnClickListener {
            val step = viewModel.requestedCachesStep
            val max = viewModel.requestedCachesMax
            SliderDialogFragment.newInstance(
                title = R.string.title_geocache_count,
                min = step,
                max = max,
                step = step,
                defaultValue = requireNotNull(viewModel.requestedCaches.value)
            ).show(supportFragmentManager, "COUNTER")
        }

        gps.setOnClickListener {
            viewModel.retrieveCoordinates()
        }

        filter.setOnClickListener {
            viewModel.showFilters()
        }

        fab.setOnClickListener {
            viewModel.download()
        }

        viewModel.action.observe(this, ::handleAction)
        viewModel.progress.observe(this, ::handleProgress)
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
                onBackPressed()
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
                PermissionUtil.requestGpsLocationPermission(this)
            }
            SearchNearestAction.RequestWifiLocationPermission -> {
                PermissionUtil.requestWifiLocationPermission(this)
            }
            SearchNearestAction.RequestExternalStoragePermission -> {
                ExternalStoragePermissionWarningDialogFragment.newInstance().show(supportFragmentManager)
            }
            SearchNearestAction.WrongCoordinatesFormat -> {
                startActivity(ErrorActivity.IntentBuilder(this).message(R.string.error_coordinates_format).build())
            }
            SearchNearestAction.ShowFilters -> {
                startActivity(SettingsActivity.createIntent(this, FilterPreferenceFragment::class.java))
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

        if (requestCode == PermissionUtil.REQUEST_LOCATION_PERMISSION) {
            if (PermissionUtil.verifyPermissions(grantResults)) {
                viewModel.retrieveCoordinates()
            } else {
                NoLocationPermissionErrorDialogFragment.newInstance().show(supportFragmentManager)
            }
        }

        if (requestCode == PermissionUtil.REQUEST_EXTERNAL_STORAGE_PERMISSION) {
            if (PermissionUtil.verifyPermissions(grantResults)) {
                viewModel.download()
            } else {
                NoExternalStoragePermissionErrorDialogFragment.newInstance(false).show(supportFragmentManager)
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
        private val REQUEST_SIGN_ON = 1
    }
}