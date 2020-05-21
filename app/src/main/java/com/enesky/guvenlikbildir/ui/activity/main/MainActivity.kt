package com.enesky.guvenlikbildir.ui.activity.main

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
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
import com.enesky.guvenlikbildir.others.Constants
import com.enesky.guvenlikbildir.others.LocationAPI
import com.enesky.guvenlikbildir.others.isNotificationsEnabled
import com.enesky.guvenlikbildir.others.isWorkerStarted
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
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber

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

        EasyPermissions.requestPermissions(
            this,
            "",
            1,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        mainVM.responseHandler.addObserver { _, response ->
            GlobalScope.launch {
                withContext(Dispatchers.Main) {
                    if (response != null && response is Result<*>) {
                        when (response.status) {
                            Status.SUCCESS -> ""
                            Status.FAILURE -> showToast(response.data.toString())
                        }
                    }
                }
            }
        }

        if (isNotificationsEnabled) {
            if (isWorkerStarted) {
                GlobalScope.launch(Dispatchers.Default) {
                    mainVM.getEarthquakes()
                }
            } else {
                App.startWorker()
            }
        } else {
            GlobalScope.launch(Dispatchers.Default) {
                mainVM.getEarthquakes()
            }
        }

        if (intent?.getParcelableExtra<Earthquake>(Constants.NOTIFICATION_EARTHQUAKE) != null)
            openEarthquakeOption(intent.getParcelableExtra(Constants.NOTIFICATION_EARTHQUAKE))

        navigator.initialize(savedInstanceState)
        bottom_nav.setOnNavigationItemReselectedListener(this)
        bottom_nav.setOnNavigationItemSelectedListener(this)
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
        when (tabIndex) {
            0 -> bottom_nav.selectedItemId = R.id.latest_earthquakes
            1 -> bottom_nav.selectedItemId = R.id.notify
            2 -> bottom_nav.selectedItemId = R.id.options
        }
    }

    override fun onNavigationItemReselected(item: MenuItem) {}

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.hasPermissions(this,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )) {
            Timber.tag("MainActivity").d("ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION granted.")
           locationAPI.startLocationUpdates()
        }

        if (EasyPermissions.hasPermissions(this, Manifest.permission.READ_CONTACTS)) {
            Timber.tag("MainActivity").d("READ_CONTACTS granted.")
            GlobalScope.launch(Dispatchers.Default) {
                mainVM.contactRepository.refreshContacts(
                    AddContactsFragment.getContactsList(this@MainActivity).toMutableList()
                )
            }
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        Timber.tag("MainActivity").d("onPermissionsDenied: $requestCode : ${perms.size}")
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms))
            AppSettingsDialog.Builder(this).build().show()
    }

    override fun onDestroy() {
        super.onDestroy()
        locationAPI.stopLocationUpdates()
    }

    private fun openEarthquakeOption(earthquake: Earthquake?) {
        if (earthquake != null) {
            EarthquakeItemOptionsBSDFragment.newInstance(earthquake)
                .show(supportFragmentManager,"EarthquakeItemOptionsBSDFragment")

            Timber.tag("MainActivity").d("onNewIntent -> Clicked to notification")
            val params = Bundle().apply {
                putString("earthquake_location", earthquake!!.location)
                putDouble("earthquake_mag", earthquake.magML)
                putString("earthquake_date", earthquake.dateTime)
            }
            App.mAnalytics.logEvent("MainActivity_clicked2notification", params)
        }
    }

}
