package com.enesky.guvenlikbildir.ui.activity.main

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.databinding.ActivityMainBinding
import com.enesky.guvenlikbildir.extensions.*
import com.enesky.guvenlikbildir.ui.activity.BaseActivity
import com.enesky.guvenlikbildir.ui.fragment.latestEarthquakes.LatestEarthquakesFragment
import com.enesky.guvenlikbildir.ui.fragment.notify.NotifyFragment
import com.enesky.guvenlikbildir.ui.fragment.options.OptionsFragment
import com.trendyol.medusalib.navigator.MultipleStackNavigator
import com.trendyol.medusalib.navigator.Navigator
import com.trendyol.medusalib.navigator.NavigatorConfiguration
import com.trendyol.medusalib.navigator.transaction.NavigatorTransaction
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity(), Navigator.NavigatorListener {

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
        NavigatorConfiguration(defaultNavigatorTransaction = NavigatorTransaction.SHOW_HIDE)
        //TODO: Hız için show-hide kullanıldı. Ancak ram kullanımı 250 mb ları gördü :O
    )

    //TODO: LocationManager kısmını farklı bi yapıya taşıyabilirsin?
    private var locationManager: LocationManager? = null
    private var locationListenerGPS: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            val latitude = location.latitude
            val longitude = location.longitude
            lastKnownLocation = "$latitude,$longitude"
            Log.d("LocationManager", lastKnownLocation!!)
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String?) {}
        override fun onProviderDisabled(provider: String?) {}
    }

    @SuppressLint("MissingPermission")
    fun requestLocationUpdates() {
        locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager!!.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            30000,
            10f,
            locationListenerGPS
        )
    }

    private val mainVM by lazy {
        getViewModel { MainVM() }
    }
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        if (isFirstTime) {
            requireAllPermissions()
            isFirstTime = false
        }

        requireLocationPermission { requestLocationUpdates() }

        start(savedInstanceState)
    }

    //TODO: Bu metodu mümkün olduğu kadar hafiflet
    private fun start(savedInstanceState: Bundle?) {
        //delay(25)
        binding.apply {
            viewModel = mainVM
            lifecycleOwner = this@MainActivity
        }
        mainVM.init(binding)
        navigator.initialize(savedInstanceState)

        bottom_nav.apply {
            setOnNavigationItemSelectedListener {
                when (it.itemId) {
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

            setOnNavigationItemReselectedListener { }

            selectedItemId = R.id.notify
        }

        ConnectionLiveData(this).observe(this, Observer { isOnline ->
            if (!isOnline)
                showToast("Internet bağlantısı bulunamadı.\nBazı fonksiyonlar pasif durumda olacaktır.")
        })

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

}
