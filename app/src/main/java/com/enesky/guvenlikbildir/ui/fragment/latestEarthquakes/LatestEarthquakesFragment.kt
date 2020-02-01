package com.enesky.guvenlikbildir.ui.fragment.latestEarthquakes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.databinding.FragmentLastestEarthquakesBinding
import com.enesky.guvenlikbildir.ui.fragment.BaseFragment
import com.enesky.guvenlikbildir.utils.getViewModel
import kotlinx.android.synthetic.main.fragment_lastest_earthquakes.*

class LatestEarthquakesFragment: BaseFragment() {

    private lateinit var binding: FragmentLastestEarthquakesBinding
    private lateinit var latestEarthquakesFragmentVM: LatestEarthquakesFragmentVM

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_options, container,false)
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
    }

}