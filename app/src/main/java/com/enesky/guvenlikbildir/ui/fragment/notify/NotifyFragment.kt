package com.enesky.guvenlikbildir.ui.fragment.notify

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.ui.activity.main.MainActivity
import com.enesky.guvenlikbildir.ui.fragment.BaseFragment
import com.enesky.guvenlikbildir.utils.getColorCompat
import com.enesky.guvenlikbildir.utils.getViewModel
import com.enesky.guvenlikbildir.utils.showToast
import kotlinx.android.synthetic.main.fragment_notify.*

class NotifyFragment : BaseFragment() {

    private lateinit var notifyFragmentVM: NotifyFragmentVM

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        notifyFragmentVM = getViewModel()
        val root = inflater.inflate(R.layout.fragment_notify, container, false)

        notifyFragmentVM.text.observe(viewLifecycleOwner, Observer {
            //text_home.text = it
        })

        return root
    }

}