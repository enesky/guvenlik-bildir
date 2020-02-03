package com.enesky.guvenlikbildir.ui.activity.main

import android.os.Bundle
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.databinding.ActivityMainBinding
import com.enesky.guvenlikbildir.extensions.ConnectionLiveData
import com.enesky.guvenlikbildir.extensions.getViewModel
import com.enesky.guvenlikbildir.extensions.showToast
import com.enesky.guvenlikbildir.ui.activity.BaseActivity
import com.enesky.guvenlikbildir.ui.fragment.latestEarthquakes.LatestEarthquakesFragment
import com.enesky.guvenlikbildir.ui.fragment.notify.NotifyFragment
import com.enesky.guvenlikbildir.ui.fragment.options.OptionsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.trendyol.medusalib.navigator.MultipleStackNavigator
import com.trendyol.medusalib.navigator.Navigator
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity(), BottomNavigationView.OnNavigationItemSelectedListener,
    Navigator.NavigatorListener, BottomNavigationView.OnNavigationItemReselectedListener {

    private val rootFragmentProvider: List<() -> Fragment> = listOf(
            { LatestEarthquakesFragment() },
            { NotifyFragment() },
            { OptionsFragment() }
        )

    val navigator: MultipleStackNavigator = MultipleStackNavigator(
        supportFragmentManager,
        R.id.container,
        rootFragmentProvider,
        this
    )

    private val mainVM by lazy {
        getViewModel { MainVM() }
    }
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main).apply {
            viewModel = mainVM
            lifecycleOwner = this@MainActivity
        }
        mainVM.init(binding)

        navigator.initialize(savedInstanceState)
        bottom_nav.setOnNavigationItemSelectedListener(this)
        bottom_nav.setOnNavigationItemReselectedListener(this)

        val connectionLiveData = ConnectionLiveData(this)
        connectionLiveData.observe(this, Observer { isOnline ->
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

    override fun onNavigationItemReselected(item: MenuItem) {
        //Ignored
    }

}
