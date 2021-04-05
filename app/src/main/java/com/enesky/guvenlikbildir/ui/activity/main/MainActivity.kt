package com.enesky.guvenlikbildir.ui.activity.main

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.enesky.guvenlikbildir.App
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.database.AppDatabase
import com.enesky.guvenlikbildir.database.entity.Earthquake
import com.enesky.guvenlikbildir.databinding.ActivityMainBinding
import com.enesky.guvenlikbildir.extensions.getViewModel
import com.enesky.guvenlikbildir.extensions.showToast
import com.enesky.guvenlikbildir.network.Result
import com.enesky.guvenlikbildir.network.Status
import com.enesky.guvenlikbildir.others.*
import com.enesky.guvenlikbildir.ui.base.BaseActivity
import com.enesky.guvenlikbildir.ui.dialog.EarthquakeItemOptionsBSDFragment
import com.enesky.guvenlikbildir.ui.fragment.latestEarthquakes.LatestEarthquakesFragment
import com.enesky.guvenlikbildir.ui.fragment.notify.NotifyFragment
import com.enesky.guvenlikbildir.ui.fragment.options.OptionsFragment
import com.enesky.guvenlikbildir.ui.fragment.options.contacts.AddContactsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.trendyol.medusalib.navigator.MultipleStackNavigator
import com.trendyol.medusalib.navigator.Navigator
import com.trendyol.medusalib.navigator.NavigatorConfiguration
import com.trendyol.medusalib.navigator.transaction.NavigatorTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

class MainActivity : BaseActivity(),
    Navigator.NavigatorListener,
    BottomNavigationView.OnNavigationItemSelectedListener,
    BottomNavigationView.OnNavigationItemReselectedListener,
    EasyPermissions.PermissionCallbacks {

    private lateinit var binding: ActivityMainBinding
    private val mainVM by lazy {
        getViewModel {
            MainVM(AppDatabase.getDatabaseManager(application))
        }
    }

    private lateinit var locationAPI: LocationAPI
    private lateinit var smsAPI: SmsAPI

    private var rootFragmentProvider: List<() -> Fragment> = listOf(
        { LatestEarthquakesFragment() },
        { NotifyFragment() },
        { OptionsFragment() }
    )

    var navigator: MultipleStackNavigator = MultipleStackNavigator(
        supportFragmentManager,
        R.id.container,
        rootFragmentProvider,
        this,
        NavigatorConfiguration(
            initialTabIndex = 1,
            alwaysExitFromInitial = false,
            defaultNavigatorTransaction = NavigatorTransaction.SHOW_HIDE
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        App.mAnalytics.setCurrentScreen(this, "activity", this.javaClass.simpleName)
        mainVM.init(binding)

        locationAPI = LocationAPI(this)
        smsAPI = SmsAPI(this)

        EasyPermissions.requestPermissions(
            this,
            "",
            1,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        mainVM.responseHandler.addObserver { _, response ->
            GlobalScope.launch(Dispatchers.Main) {
                if (response != null && response is Result<*>) {
                    when (response.status) {
                        Status.SUCCESS -> return@launch
                        Status.FAILURE -> showToast(response.data.toString())
                    }
                }
            }
        }

        if (isNotificationsEnabled) {
            if (isWorkerStarted)
                GlobalScope.launch(Dispatchers.IO) {
                    mainVM.getEarthquakes()
                }
            else
                App.startWorker()
        } else
            GlobalScope.launch(Dispatchers.IO) {
                mainVM.getEarthquakes()
            }

        if (intent?.getParcelableExtra<Earthquake>(Constants.NOTIFICATION_EARTHQUAKE) != null)
            openEarthquakeOption(intent.getParcelableExtra(Constants.NOTIFICATION_EARTHQUAKE))

        navigator.initialize(savedInstanceState)
        binding.bottomNav.setOnNavigationItemReselectedListener(this)
        binding.bottomNav.setOnNavigationItemSelectedListener(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.getParcelableExtra<Earthquake>(Constants.NOTIFICATION_EARTHQUAKE) != null)
            openEarthquakeOption(intent.getParcelableExtra(Constants.NOTIFICATION_EARTHQUAKE))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        navigator.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onBackPressed() {
        if (navigator.canGoBack())
            navigator.goBack()
        else
            super.onBackPressed()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.latest_earthquakes -> {
                navigator.switchTab(0)
                true
            }
            R.id.notify -> {
                navigator.switchTab(1)
                true
            }
            R.id.options -> {
                navigator.switchTab(2)
                true
            }
            else -> false
        }
    }

    override fun onTabChanged(tabIndex: Int) {
        binding.bottomNav.selectedItemId = when (tabIndex) {
            0 -> R.id.latest_earthquakes
            1 -> R.id.notify
            else -> R.id.options
        }
    }

    override fun onNavigationItemReselected(item: MenuItem) {}

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.hasPermissions(this,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
            locationAPI.startLocationUpdates()

        if (EasyPermissions.hasPermissions(this, Manifest.permission.READ_CONTACTS))
            GlobalScope.launch(Dispatchers.IO) {
                mainVM.contactRepository.refreshContacts(
                    AddContactsFragment.getContactsList(this@MainActivity).toMutableList()
                )
            }

        if (EasyPermissions.hasPermissions(this, Manifest.permission.SEND_SMS))
            smsAPI.setReceivers()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms))
            AppSettingsDialog.Builder(this).build().show()
    }

    override fun onDestroy() {
        super.onDestroy()
        locationAPI.stopLocationUpdates()
        smsAPI.onDestroy()
    }

    private fun openEarthquakeOption(earthquake: Earthquake?) {
        if (earthquake != null) {
            EarthquakeItemOptionsBSDFragment.newInstance(earthquake)
                .show(supportFragmentManager,"EarthquakeItemOptionsBSDFragment")

            App.mAnalytics.logEvent("notification_click", bundleOf(
                    "earthquake_location" to earthquake.location,
                    "earthquake_mag" to earthquake.mag,
                    "earthquake_date" to earthquake.dateTime
                )
            )
        }
    }

}
