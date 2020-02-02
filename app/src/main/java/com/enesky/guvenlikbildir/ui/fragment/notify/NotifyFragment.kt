package com.enesky.guvenlikbildir.ui.fragment.notify

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.databinding.FragmentNotifyBinding
import com.enesky.guvenlikbildir.ui.activity.main.MainActivity
import com.enesky.guvenlikbildir.ui.fragment.BaseFragment
import com.enesky.guvenlikbildir.utils.getColorCompat
import com.enesky.guvenlikbildir.utils.getViewModel
import com.enesky.guvenlikbildir.utils.showToast
import kotlinx.android.synthetic.main.fragment_notify.*

class NotifyFragment: BaseFragment() {

    private lateinit var binding: FragmentNotifyBinding
    private lateinit var notifyFragmentVM: NotifyFragmentVM

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_notify, container,false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        notifyFragmentVM = getViewModel()
        binding.apply {
            viewModel = notifyFragmentVM
            lifecycleOwner = this@NotifyFragment
        }
        notifyFragmentVM.init(binding)
    }

}