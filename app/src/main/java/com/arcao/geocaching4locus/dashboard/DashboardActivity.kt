package com.arcao.geocaching4locus.dashboard

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.authentication.LoginActivity
import com.arcao.geocaching4locus.base.AbstractActionBarActivity
import com.arcao.geocaching4locus.base.constants.AppConstants
import com.arcao.geocaching4locus.base.util.isCalledFromLocusMap
import com.arcao.geocaching4locus.base.util.showLocusMissingError
import com.arcao.geocaching4locus.base.util.showWebPage
import com.arcao.geocaching4locus.base.util.withObserve
import com.arcao.geocaching4locus.dashboard.fragment.NoPostNotificationPermissionErrorDialogFragment
import com.arcao.geocaching4locus.databinding.ActivityDashboardBinding
import com.arcao.geocaching4locus.download_rectangle.DownloadRectangleActivity
import com.arcao.geocaching4locus.import_bookmarks.ImportBookmarkActivity
import com.arcao.geocaching4locus.importgc.ImportGeocacheCodeActivity
import com.arcao.geocaching4locus.live_map.fragment.PowerSaveWarningDialogFragment
import com.arcao.geocaching4locus.search_nearest.SearchNearestActivity
import com.arcao.geocaching4locus.settings.SettingsActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class DashboardActivity : AbstractActionBarActivity(),
    PowerSaveWarningDialogFragment.OnPowerSaveWarningConfirmedListener {

    private val viewModel by viewModel<DashboardViewModel> {
        parametersOf(isCalledFromLocusMap())
    }

    private lateinit var binding: ActivityDashboardBinding

    private val successCallback = ActivityResultCallback<Boolean> { success ->
        if (success) finish()
    }
    private val searchNearestActivity =
        registerForActivityResult(SearchNearestActivity.Contract, successCallback)
    private val importGeocacheCodeActivity =
        registerForActivityResult(ImportGeocacheCodeActivity.Contract, successCallback)
    private val downloadRectangleActivity =
        registerForActivityResult(DownloadRectangleActivity.Contract, successCallback)
    private val importBookmarkActivity =
        registerForActivityResult(ImportBookmarkActivity.Contract, successCallback)
    private val settingsActivity = registerForActivityResult(SettingsActivity.Contract) {}
    private val loginActivity = registerForActivityResult(LoginActivity.Contract) { success ->
        if (success) viewModel.onClickLiveMap()
    }

    private val requestPostNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { result ->
        if (result) {
            viewModel.onClickLiveMap()
        } else {
            NoPostNotificationPermissionErrorDialogFragment.newInstance()
                .show(supportFragmentManager)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar.root)
        supportActionBar?.title = title

        initViews()

        viewModel.action.withObserve(this, ::handleAction)
    }

    private fun initViews() = with(binding) {
        dbSearchNearest.setOnClickListener { viewModel.onClickSearchNearest() }
        dbImportGc.setOnClickListener { viewModel.onClickImportGcCode() }
        dbImportBookmark.setOnClickListener { viewModel.onClickImportBookmarks() }
        dbLiveMap.setOnClickListener { viewModel.onClickLiveMap() }
        dbLiveMapDownloadCaches.setOnClickListener { viewModel.onClickImportLiveMapGc() }
        dbPreferences.setOnClickListener { viewModel.onClickPreferences() }
        dbUsersGuide.setOnClickListener { viewModel.onClickUsersGuide() }

        viewModel.liveMapEnabled.observe(this@DashboardActivity) { enabled ->
            val liveMapEnabled = enabled == true
            dbLiveMap.isChecked = liveMapEnabled
            dbLiveMapDownloadCaches.isEnabled = liveMapEnabled
        }
    }

    override fun onResume() {
        super.onResume()

        // The account can change while the dashboard is in the background (sign in from other screen)
        updateImportBookmarkButton()
    }

    private fun updateImportBookmarkButton() = with(binding.dbImportBookmark) {
        val premium = viewModel.premium
        isEnabled = premium
        text = if (premium) {
            getString(R.string.menu_import_bookmarks)
        } else {
            getString(R.string.format_premium, getString(R.string.menu_import_bookmarks))
        }
    }

    private fun handleAction(action: DashboardAction) {
        when (action) {
            is DashboardAction.SearchNearest -> searchNearestActivity.launch(
                if (isCalledFromLocusMap()) intent else null
            )

            is DashboardAction.ImportGcCode -> importGeocacheCodeActivity.launch(null)
            is DashboardAction.DownloadLiveMapGeocaches -> downloadRectangleActivity.launch(null)
            is DashboardAction.ImportBookmarks -> importBookmarkActivity.launch(null)
            is DashboardAction.Preferences -> settingsActivity.launch(null)
            is DashboardAction.UsersGuide -> showWebPage(AppConstants.USERS_GUIDE_URI)
            is DashboardAction.LocusMapNotInstalled -> showLocusMissingError()
            is DashboardAction.SignIn -> loginActivity.launch(null)
            is DashboardAction.WarnPowerSaveActive -> PowerSaveWarningDialogFragment.newInstance()
                .show(supportFragmentManager)

            is DashboardAction.NavigationBack -> finish()
            DashboardAction.RequestPostNotificationPermission -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPostNotificationPermission.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_dashboard, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.main_activity_option_menu_preferences -> {
                viewModel.onClickPreferences()
                true
            }

            android.R.id.home -> {
                viewModel.onClickNavigationBack()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPowerSaveWarningConfirmed() {
        viewModel.onPowerSaveWarningConfirmed()
    }
}
