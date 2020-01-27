package com.enesky.guvenlikbildir.ui.fragment.latestEarthquakes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.ui.fragment.BaseFragment
import com.enesky.guvenlikbildir.ui.login.activity.ui.dashboard.LatestEarthquakesFragmentVM
import com.enesky.guvenlikbildir.utils.getViewModel

class LatestEarthquakesFragment : BaseFragment() {

    private lateinit var latestEarthquakesFragmentVM: LatestEarthquakesFragmentVM

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        latestEarthquakesFragmentVM = getViewModel()
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)
        val textView: TextView = root.findViewById(R.id.text_dashboard)
        latestEarthquakesFragmentVM.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}