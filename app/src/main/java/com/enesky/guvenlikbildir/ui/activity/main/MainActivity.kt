package com.enesky.guvenlikbildir.ui.activity.main

import android.os.Bundle
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.adapter.ViewPagerAdapter
import com.enesky.guvenlikbildir.databinding.ActivityMainBinding
import com.enesky.guvenlikbildir.ui.activity.BaseActivity
import com.enesky.guvenlikbildir.ui.fragment.latestEarthquakes.LatestEarthquakesFragment
import com.enesky.guvenlikbildir.ui.fragment.notify.NotifyFragment
import com.enesky.guvenlikbildir.ui.fragment.options.OptionsFragment
import com.enesky.guvenlikbildir.utils.ConnectionLiveData
import com.enesky.guvenlikbildir.utils.getViewModel
import com.enesky.guvenlikbildir.utils.showToast
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.trendyol.medusalib.navigator.MultipleStackNavigator
import com.trendyol.medusalib.navigator.Navigator
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity(), BottomNavigationView.OnNavigationItemSelectedListener,
    Navigator.NavigatorListener, ViewPager.OnPageChangeListener,
    BottomNavigationView.OnNavigationItemReselectedListener {

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

    private val mainVM by lazy {
        getViewModel { MainActivityVM() }
    }
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main).apply {
            viewModel = mainVM
            lifecycleOwner = this@MainActivity
        }
        mainVM.init(binding)

        vp_home.adapter = ViewPagerAdapter(
            supportFragmentManager,
            listOf(LatestEarthquakesFragment(), NotifyFragment(), OptionsFragment()),
            listOf("Son Depremler", "Güvenliğini Bildir", "Seçenekler"))

        navigator.initialize(savedInstanceState)
        vp_home.addOnPageChangeListener(this)
        bottom_nav.setOnNavigationItemSelectedListener(this)
        bottom_nav.setOnNavigationItemReselectedListener(this)

        vp_home.setCurrentItem(1, false)
        //TODO: Smooth scroll hızını düşür.

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
                if (vp_home.currentItem != 0)
                    vp_home.setCurrentItem(0, false)
                return true
            }

            R.id.notify ->  {
                if (vp_home.currentItem != 1)
                    vp_home.setCurrentItem(1, false)
                return true
            }
            R.id.options -> {
                if (vp_home.currentItem != 2)
                    vp_home.setCurrentItem(2, false)
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

    override fun onPageSelected(position: Int) {
        navigator.switchTab(position)
    }

    override fun onPageScrollStateChanged(state: Int) {
        //Ignored
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        //Ignored
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        //Ignored
    }

}
