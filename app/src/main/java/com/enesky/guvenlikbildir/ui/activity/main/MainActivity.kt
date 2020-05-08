package com.enesky.guvenlikbildir.ui.activity.main

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.enesky.guvenlikbildir.App
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.database.EarthquakeDB
import com.enesky.guvenlikbildir.database.EarthquakeRepository
import com.enesky.guvenlikbildir.database.EarthquakeVM
import com.enesky.guvenlikbildir.database.dao.EarthquakeDao
import com.enesky.guvenlikbildir.databinding.ActivityMainBinding
import com.enesky.guvenlikbildir.extensions.*
import com.enesky.guvenlikbildir.network.Result
import com.enesky.guvenlikbildir.network.Status
import com.enesky.guvenlikbildir.ui.activity.BaseActivity
import com.enesky.guvenlikbildir.ui.fragment.latestEarthquakes.LatestEarthquakesFragment
import com.enesky.guvenlikbildir.ui.fragment.notify.NotifyFragment
import com.enesky.guvenlikbildir.ui.fragment.options.OptionsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseUser
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

class MainActivity : BaseActivity(), Navigator.NavigatorListener,
    BottomNavigationView.OnNavigationItemSelectedListener,
    BottomNavigationView.OnNavigationItemReselectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var earthquakeDao: EarthquakeDao

    val earthquakeVM by lazy {
        getViewModel {
            EarthquakeVM(
                EarthquakeRepository(
                    earthquakeDao = earthquakeDao
            ))
        }
    }

    private var locationManager: LocationManager? = null
    private var locationListenerGPS: LocationListener? = null

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
        NavigatorConfiguration(1,false, defaultNavigatorTransaction = NavigatorTransaction.SHOW_HIDE)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this

        earthquakeDao = EarthquakeDB.getDatabaseManager(application).earthquakeDao()

        earthquakeVM.responseHandler.addObserver { _, response ->
            GlobalScope.launch {
                withContext(Dispatchers.Main) {
                    if (response != null && response is Result<*>) {
                        when (response.status) {
                            Status.SUCCESS -> ""
                            Status.FAILURE, Status.EXCEPTION -> showToast(response.data.toString())
                        }
                    }
                }
            }
        }

        GlobalScope.launch {
            earthquakeVM.getEarthquakes()
        }

        navigator.initialize(savedInstanceState)
        bottom_nav.setOnNavigationItemReselectedListener(this)
        bottom_nav.setOnNavigationItemSelectedListener(this)
    }

    override fun onStart() {
        super.onStart()
        val currentUser: FirebaseUser? = App.mAuth.currentUser
        if (currentUser == null)
            openLoginActivity()

        if (isFirstTime) {
            requireAllPermissions()
            isFirstTime = false
        }

        requireLocationPermission { requestLocationUpdates() }
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

    @SuppressLint("MissingPermission")
    fun requestLocationUpdates() {
        locationListenerGPS = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val latitude = location.latitude
                val longitude = location.longitude
                lastKnownLocation = "$latitude,$longitude"
                Timber.tag("LocationManager").d("%s", lastKnownLocation!!)
            }
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String?) {}
            override fun onProviderDisabled(provider: String?) {}
        }

        locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager!!.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            10000,
            5f,
            locationListenerGPS!!
        )
    }

}
