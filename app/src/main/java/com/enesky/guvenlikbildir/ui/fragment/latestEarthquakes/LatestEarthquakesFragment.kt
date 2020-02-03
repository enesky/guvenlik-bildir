package com.enesky.guvenlikbildir.ui.fragment.latestEarthquakes

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.databinding.FragmentLastestEarthquakesBinding
import com.enesky.guvenlikbildir.extensions.getViewModel
import com.enesky.guvenlikbildir.extensions.makeItGone
import com.enesky.guvenlikbildir.extensions.makeItVisible
import com.enesky.guvenlikbildir.extensions.showToast
import com.enesky.guvenlikbildir.network.Result
import com.enesky.guvenlikbildir.network.Status
import com.enesky.guvenlikbildir.ui.fragment.BaseFragment
import kotlinx.android.synthetic.main.fragment_lastest_earthquakes.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LatestEarthquakesFragment: BaseFragment() {

    private lateinit var binding: FragmentLastestEarthquakesBinding
    private lateinit var latestEarthquakesVM: LatestEarthquakesVM

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_lastest_earthquakes, container,false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        latestEarthquakesVM = getViewModel()
        binding.apply {
            viewModel = latestEarthquakesVM
            lifecycleOwner = this@LatestEarthquakesFragment
        }
        latestEarthquakesVM.init(binding)

        latestEarthquakesVM.responseHandler.addObserver{ _, it ->
            GlobalScope.launch {
                withContext(Dispatchers.Main) {
                    if (it != null && it is Result<*>) {
                        when (it.status) {
                            Status.SUCCESS -> requireContext().showToast(it.data.toString())
                            Status.FAILURE -> requireContext().showToast(it.data.toString())
                            Status.EXCEPTION -> requireContext().showToast(it.data.toString())
                        }
                    }
                }
            }
        }

        fab_synchronize.setOnClickListener {
            refresh()
        }

    }

    fun refresh() {
        val objectAnimator = ObjectAnimator
            .ofFloat(fab_synchronize, "rotation", 360f, 0f)
            .setDuration(700)
        objectAnimator.repeatCount = 2

        objectAnimator.start()
        pb_loading.makeItVisible()
        //TODO: new request + adapter.update

        Handler().apply {
            postDelayed({
                fab_synchronize.setImageResource(R.drawable.ic_sync)
                pb_loading.makeItGone()
            }, 2000)
        }
    }

}