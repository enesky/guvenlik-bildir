package com.enesky.guvenlikbildir.ui.activity.main

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.ui.activity.BaseActivity
import com.enesky.guvenlikbildir.ui.fragment.latestEarthquakes.LatestEarthquakesFragment
import com.enesky.guvenlikbildir.ui.fragment.notify.NotifyFragment
import com.enesky.guvenlikbildir.ui.fragment.options.OptionsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.trendyol.medusalib.navigator.MultipleStackNavigator
import com.trendyol.medusalib.navigator.Navigator
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity(), BottomNavigationView.OnNavigationItemSelectedListener,
    Navigator.NavigatorListener {

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigator.initialize(savedInstanceState)
        navigator.start(NotifyFragment(),1)
        bottom_nav.setOnNavigationItemSelectedListener(this)
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
