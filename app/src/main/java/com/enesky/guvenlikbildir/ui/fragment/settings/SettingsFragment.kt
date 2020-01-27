package com.enesky.guvenlikbildir.ui.fragment.settings

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
import com.enesky.guvenlikbildir.ui.login.activity.ui.notifications.SettingsFragmentVM
import com.enesky.guvenlikbildir.utils.getViewModel

class SettingsFragment : BaseFragment() {

    private lateinit var settingsFragmentVM: SettingsFragmentVM

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        settingsFragmentVM = getViewModel()
        val root = inflater.inflate(R.layout.fragment_notifications, container, false)
        val textView: TextView = root.findViewById(R.id.text_notifications)
        settingsFragmentVM.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}