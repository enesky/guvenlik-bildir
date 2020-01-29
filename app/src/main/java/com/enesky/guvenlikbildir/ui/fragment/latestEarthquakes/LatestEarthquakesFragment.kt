package com.enesky.guvenlikbildir.ui.fragment.latestEarthquakes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.ui.fragment.BaseFragment
import com.enesky.guvenlikbildir.utils.getViewModel
import kotlinx.android.synthetic.main.fragment_lastest_earthquakes.*

class LatestEarthquakesFragment : BaseFragment() {

    private lateinit var latestEarthquakesFragmentVM: LatestEarthquakesFragmentVM

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        latestEarthquakesFragmentVM = getViewModel()
        val root = inflater.inflate(R.layout.fragment_lastest_earthquakes, container, false)

        latestEarthquakesFragmentVM.text.observe(viewLifecycleOwner, Observer {
            text_dashboard.text = it
        })

        return root
    }
}