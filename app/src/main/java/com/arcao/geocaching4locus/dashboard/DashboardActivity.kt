package com.arcao.geocaching4locus.dashboard

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
import com.arcao.geocaching4locus.base.constants.AppConstants
import com.arcao.geocaching4locus.base.util.isCalledFromLocusMap
import com.arcao.geocaching4locus.base.util.showLocusMissingError
import com.arcao.geocaching4locus.base.util.showWebPage
import com.arcao.geocaching4locus.base.util.withObserve
import com.arcao.geocaching4locus.data.account.AccountManager
import com.arcao.geocaching4locus.databinding.ActivityDashboardBinding
import com.arcao.geocaching4locus.download_rectangle.DownloadRectangleActivity
import com.arcao.geocaching4locus.import_bookmarks.ImportBookmarkActivity
import com.arcao.geocaching4locus.importgc.ImportGeocacheCodeActivity
import com.arcao.geocaching4locus.live_map.fragment.PowerSaveWarningDialogFragment
import com.arcao.geocaching4locus.search_nearest.SearchNearestActivity
import com.arcao.geocaching4locus.settings.SettingsActivity
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class DashboardActivity : AbstractActionBarActivity(),
    PowerSaveWarningDialogFragment.OnPowerSaveWarningConfirmedListener {

    private val viewModel by viewModel<DashboardViewModel> {
        parametersOf(isCalledFromLocusMap())
    }

    private val accountManager by inject<AccountManager>()

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_dashboard)
        binding.lifecycleOwner = this
        binding.vm = viewModel

        viewModel.action.withObserve(this, ::handleAction)

        setSupportActionBar(binding.toolbar as Toolbar)
        supportActionBar?.title = title
    }

    private fun handleAction(action: DashboardAction?) {
        when (action) {
            is DashboardAction.SearchNearest -> {
                startActivityForResult(
                    Intent(this, SearchNearestActivity::class.java).apply {
                        if (isCalledFromLocusMap()) putExtras(intent)
                    }, 0
                )
            }
            is DashboardAction.ImportGcCode ->
                startActivityForResult(Intent(this, ImportGeocacheCodeActivity::class.java), 0)
            is DashboardAction.DownloadLiveMapGeocaches ->
                startActivityForResult(Intent(this, DownloadRectangleActivity::class.java), 0)
            is DashboardAction.ImportBookmarks ->
                startActivityForResult(Intent(this, ImportBookmarkActivity::class.java), 0)
            is DashboardAction.Preferences ->
                startActivity(SettingsActivity.createIntent(this))
            is DashboardAction.UsersGuide ->
                showWebPage(AppConstants.USERS_GUIDE_URI)

            is DashboardAction.LocusMapNotInstalled ->
                showLocusMissingError()
            is DashboardAction.SignIn ->
                accountManager.requestSignOn(this, REQUEST_SIGN_ON)
            is DashboardAction.WarnPowerSaveActive ->
                PowerSaveWarningDialogFragment.newInstance().show(supportFragmentManager)

            is DashboardAction.NavigationBack ->
                finish()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_SIGN_ON && resultCode == Activity.RESULT_OK) {
            viewModel.onClickLiveMap()
        } else if (resultCode == Activity.RESULT_OK) {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()

        binding.vm = viewModel
    }

    override fun onPowerSaveWarningConfirmed() {
        viewModel.onPowerSaveWarningConfirmed()
    }

    companion object {
        private const val REQUEST_SIGN_ON = 1
    }
}
