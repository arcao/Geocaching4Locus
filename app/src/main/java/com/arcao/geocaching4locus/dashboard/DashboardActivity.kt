package com.arcao.geocaching4locus.dashboard

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.ActivityResultCallback
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.authentication.LoginActivity
import com.arcao.geocaching4locus.base.AbstractActionBarActivity
import com.arcao.geocaching4locus.base.constants.AppConstants
import com.arcao.geocaching4locus.base.util.isCalledFromLocusMap
import com.arcao.geocaching4locus.base.util.showLocusMissingError
import com.arcao.geocaching4locus.base.util.showWebPage
import com.arcao.geocaching4locus.base.util.withObserve
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_dashboard)
        binding.lifecycleOwner = this
        binding.vm = viewModel

        viewModel.action.withObserve(this, ::handleAction)

        @Suppress("USELESS_CAST")
        setSupportActionBar(binding.toolbar as Toolbar)
        supportActionBar?.title = title
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
