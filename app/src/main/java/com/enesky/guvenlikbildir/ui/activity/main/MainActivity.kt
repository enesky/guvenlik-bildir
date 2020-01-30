package com.enesky.guvenlikbildir.ui.activity.main

import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.ConnectivityManager.CONNECTIVITY_ACTION
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.ui.activity.BaseActivity
import com.enesky.guvenlikbildir.ui.fragment.latestEarthquakes.LatestEarthquakesFragment
import com.enesky.guvenlikbildir.ui.fragment.notify.NotifyFragment
import com.enesky.guvenlikbildir.ui.fragment.options.OptionsFragment
import com.enesky.guvenlikbildir.utils.ConnectionLiveData
import com.enesky.guvenlikbildir.utils.NetworkChecker
import com.enesky.guvenlikbildir.utils.getViewModel
import com.enesky.guvenlikbildir.utils.showToast
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.trendyol.medusalib.navigator.MultipleStackNavigator
import com.trendyol.medusalib.navigator.Navigator
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity(), BottomNavigationView.OnNavigationItemSelectedListener, Navigator.NavigatorListener {

    private lateinit var networkChecker: BroadcastReceiver

    private val rootFragmentProvider: List<() -> Fragment> = listOf(
            { LatestEarthquakesFragment() },
            { NotifyFragment() },
            { OptionsFragment() }
        )

    private val navigator: MultipleStackNavigator =
        MultipleStackNavigator(
            supportFragmentManager,
            R.id.container,
            rootFragmentProvider,
            this
        )

    val mainVM by lazy {
        getViewModel { MainActivityVM() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigator.initialize(savedInstanceState)
        bottom_nav.setOnNavigationItemSelectedListener(this)
        bottom_nav.selectedItemId = R.id.notify

        val connectionLiveData = ConnectionLiveData(this)
        connectionLiveData.observe(this, Observer {
            if (it)
                showToast("Online")
            else
                showToast("Offline")
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(networkChecker)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        navigator.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onBackPressed() {
        if (navigator.canGoBack()) {
            navigator.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.latest_earthquakes -> {
                navigator.switchTab(0)
                return true
            }

            R.id.notify ->  {
                navigator.switchTab(1)
                return true
            }
            R.id.options -> {
                navigator.switchTab(2)
                return true
            }
        }
        return false
    }

    override fun onTabChanged(tabIndex: Int) {
        when (tabIndex) {
            0 -> bottom_nav.selectedItemId = R.id.latest_earthquakes
            1 -> bottom_nav.selectedItemId = R.id.notify
            2 -> bottom_nav.selectedItemId = R.id.options
        }
    }

}
