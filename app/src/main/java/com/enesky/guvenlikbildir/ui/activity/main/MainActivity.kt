package com.enesky.guvenlikbildir.ui.activity.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.enesky.guvenlikbildir.App
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.database.AppDatabase
import com.enesky.guvenlikbildir.database.entity.Earthquake
import com.enesky.guvenlikbildir.databinding.ActivityMainBinding
import com.enesky.guvenlikbildir.extensions.*
import com.enesky.guvenlikbildir.network.Result
import com.enesky.guvenlikbildir.network.Status
import com.enesky.guvenlikbildir.others.Constants
import com.enesky.guvenlikbildir.ui.activity.BaseActivity
import com.enesky.guvenlikbildir.ui.dialog.InfoCountDownDialog
import com.enesky.guvenlikbildir.ui.fragment.latestEarthquakes.LatestEarthquakesFragment
import com.enesky.guvenlikbildir.ui.fragment.notify.NotifyFragment
import com.enesky.guvenlikbildir.ui.fragment.options.OptionsFragment
import com.enesky.guvenlikbildir.ui.fragment.options.contacts.AddContactsFragment
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.*
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
import timber.log.Timber


class MainActivity : BaseActivity(),
    Navigator.NavigatorListener,
    BottomNavigationView.OnNavigationItemSelectedListener,
    BottomNavigationView.OnNavigationItemReselectedListener {

    private lateinit var binding: ActivityMainBinding
    private val mainVM by lazy {
        getViewModel {
            MainVM(AppDatabase.getDatabaseManager(application))
        }
    }

    private var locationManager: LocationManager? = null
    private var locationListener: LocationListener? = null
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null
    private var locationCallback: LocationCallback? = null

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

        if (isFirstTime) {
            requireAllPermissions()
            isFirstTime = false
        }

        requireLocationPermission { setLocationUpdateWatcher() }

        requireReadContactsPermission {
            GlobalScope.launch(Dispatchers.Default) {
                mainVM.contactRepository.refreshContacts(
                    AddContactsFragment.getContactsList(this@MainActivity).toMutableList()
                )
            }
        }

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

        if (isNotificationsEnabled)
            App.startWorker()
        else
            GlobalScope.launch(Dispatchers.Default) {
                mainVM.getEarthquakes()
            }

        navigator.initialize(savedInstanceState)
        bottom_nav.setOnNavigationItemReselectedListener(this)
        bottom_nav.setOnNavigationItemSelectedListener(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (intent?.getParcelableExtra<Earthquake>(Constants.NOTIFICATION_EARTHQUAKE) != null) {
            val earthquake = intent.getParcelableExtra<Earthquake>(Constants.NOTIFICATION_EARTHQUAKE)
            navigator.switchTab(0)
            mainVM.earthquakeFromNotification.value = earthquake
            Timber.tag("MainActivity").d("onNewIntent -> Clicked to notification")
            val params = Bundle().apply {
                putString("earthquake_location", earthquake!!.location)
                putDouble("earthquake_mag", earthquake.magML)
                putString("earthquake_date", earthquake.dateTime)
            }
            App.mAnalytics.logEvent("MainActivity_clicked2notification", params)
        }
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

    override fun onTabChanged(tabIndex: Int) {
        when (tabIndex) {
            0 -> bottom_nav.selectedItemId = R.id.latest_earthquakes
            1 -> bottom_nav.selectedItemId = R.id.notify
            2 -> bottom_nav.selectedItemId = R.id.options
        }
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

    override fun onNavigationItemReselected(item: MenuItem) {
        //ignored
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager = null
        locationListener = null
        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient!!.removeLocationUpdates(locationCallback);
    }

    private fun checkPlayServiceAvailability(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = apiAvailability.isGooglePlayServicesAvailable(this)

        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode))
                apiAvailability.getErrorDialog(this, resultCode, 9000).show()
            return false
        }

        return true
    }

    private fun setLocationUpdateWatcher() {
        if (checkPlayServiceAvailability())
            requestLocationUpdatesWithGoogleApi()
        else
            requestLocationUpdatesWithoutGoogleApi()
    }

    private fun requestLocationUpdatesWithGoogleApi() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = Constants.MIN_TIME_BW_LOCATION_UPDATE
            fastestInterval = Constants.MIN_TIME_BW_LOCATION_UPDATE
            smallestDisplacement = Constants.MIN_DISTANCE_BW_LOCATION_UPDATE
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return

                val mLastKnownLocation = locationResult.lastLocation
                if (mLastKnownLocation != null) {
                    lastKnownLocation = "${mLastKnownLocation.latitude},${mLastKnownLocation.longitude}"
                    Timber.tag("MainActivity").d(lastKnownLocation)
                }

            }
        }

        fusedLocationProviderClient!!.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()
        )

    }

    @SuppressLint("MissingPermission")
    fun requestLocationUpdatesWithoutGoogleApi() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                lastKnownLocation = "${location.latitude},${location.longitude}"
            }
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String?) {}
            override fun onProviderDisabled(provider: String?) {
                InfoCountDownDialog().show(supportFragmentManager, Constants.gpsSetting)
            }
        }

        val gpsProvider = LocationManager.GPS_PROVIDER
        val networkProvider = LocationManager.NETWORK_PROVIDER
        var finalProvider: String? = null

        val isGpsEnabled = locationManager!!.isProviderEnabled(gpsProvider)
        val isNetworkEnabled = locationManager!!.isProviderEnabled(networkProvider)

        val gpsLocation: Location? = locationManager!!.getLastKnownLocation(gpsProvider)
        val networkLocation: Location? = locationManager!!.getLastKnownLocation(networkProvider)
        var lastKnownLoc: Location? = null

        finalProvider = when {
            isNetworkEnabled -> networkProvider
            else -> gpsProvider
        }

        lastKnownLoc = if (networkLocation != null && gpsLocation != null) {
            if (gpsLocation.accuracy > networkLocation.accuracy) //ne kadar küçükse o kadar accurate
                networkLocation
            else
                gpsLocation
        } else {
            networkLocation ?: gpsLocation
        }

        if (lastKnownLoc != null)
            lastKnownLocation = "${lastKnownLoc.latitude},${lastKnownLoc.longitude}"

        locationManager!!.requestLocationUpdates(
            finalProvider,
            Constants.MIN_TIME_BW_LOCATION_UPDATE,
            Constants.MIN_DISTANCE_BW_LOCATION_UPDATE,
            locationListener!!
        )
    }

}
