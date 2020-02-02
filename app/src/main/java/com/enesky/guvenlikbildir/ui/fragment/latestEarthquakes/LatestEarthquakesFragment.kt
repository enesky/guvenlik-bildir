package com.enesky.guvenlikbildir.ui.fragment.latestEarthquakes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.databinding.FragmentLastestEarthquakesBinding
import com.enesky.guvenlikbildir.network.Result
import com.enesky.guvenlikbildir.network.Status
import com.enesky.guvenlikbildir.ui.fragment.BaseFragment
import com.enesky.guvenlikbildir.utils.getViewModel
import com.enesky.guvenlikbildir.utils.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LatestEarthquakesFragment: BaseFragment() {

    private lateinit var binding: FragmentLastestEarthquakesBinding
    private lateinit var latestEarthquakesFragmentVM: LatestEarthquakesFragmentVM

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_lastest_earthquakes, container,false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        latestEarthquakesFragmentVM = getViewModel()
        binding.apply {
            viewModel = latestEarthquakesFragmentVM
            lifecycleOwner = this@LatestEarthquakesFragment
        }
        latestEarthquakesFragmentVM.init(binding)

        latestEarthquakesFragmentVM.responseHandler.addObserver{_, it ->
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

    }

}