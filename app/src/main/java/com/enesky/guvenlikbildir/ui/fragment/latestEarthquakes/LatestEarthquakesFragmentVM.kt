package com.enesky.guvenlikbildir.ui.fragment.latestEarthquakes

import com.enesky.guvenlikbildir.databinding.FragmentLastestEarthquakesBinding
import com.enesky.guvenlikbildir.viewModel.BaseViewModel

class LatestEarthquakesFragmentVM : BaseViewModel() {

    fun init(binding: FragmentLastestEarthquakesBinding) {
        setViewDataBinding(binding)
    }

}