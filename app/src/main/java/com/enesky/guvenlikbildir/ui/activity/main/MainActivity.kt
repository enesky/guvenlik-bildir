package com.enesky.guvenlikbildir.ui.activity.main

import android.os.Bundle
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
import com.trendyol.medusalib.navigator.MultipleStackNavigator
import com.trendyol.medusalib.navigator.Navigator
import com.trendyol.medusalib.navigator.NavigatorConfiguration
import com.trendyol.medusalib.navigator.transaction.NavigatorTransaction
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : BaseActivity(), Navigator.NavigatorListener{

    private lateinit var rootFragmentProvider: List<() -> Fragment>
    lateinit var navigator: MultipleStackNavigator

    private val mainVM by lazy {
        getViewModel { MainVM() }
    }
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        //GlobalScope.launch(Dispatchers.Main) {
            start(savedInstanceState)
        //}
    }

    //TODO: Mümkün olduğu kadar hafiflet
    private fun start(savedInstanceState: Bundle?) {
        //delay(25)

        binding.apply {
            viewModel = mainVM
            lifecycleOwner = this@MainActivity
        }

        rootFragmentProvider  = listOf(
            { LatestEarthquakesFragment() },
            { NotifyFragment() },
            { OptionsFragment() }
        )

        navigator = MultipleStackNavigator(
                supportFragmentManager,
                R.id.container,
                rootFragmentProvider,
                this,
                NavigatorConfiguration(defaultNavigatorTransaction = NavigatorTransaction.SHOW_HIDE)
                //TODO: Hız için show-hide kullanıldı. Ancak ram kullanımı 250 mb ları gördü :OOO
        )

        mainVM.init(binding)

        navigator.initialize(savedInstanceState)

        bottom_nav.apply {
            //setOnNavigationItemSelectedListener(this@MainActivity)
            //setOnNavigationItemReselectedListener(this@MainActivity)

            setOnNavigationItemSelectedListener {
                when (it.itemId) {
                    R.id.latest_earthquakes -> {
                        navigator.switchTab(0)
                        true
                    }
                    R.id.notify ->  {
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

            setOnNavigationItemReselectedListener {  }

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
