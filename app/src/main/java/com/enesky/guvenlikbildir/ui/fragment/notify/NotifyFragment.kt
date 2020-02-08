package com.enesky.guvenlikbildir.ui.fragment.notify

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.databinding.FragmentNotifyBinding
import com.enesky.guvenlikbildir.extensions.Constants
import com.enesky.guvenlikbildir.extensions.getViewModel
import com.enesky.guvenlikbildir.ui.fragment.BaseFragment
import kotlinx.android.synthetic.main.fragment_notify.*

class NotifyFragment : BaseFragment() {

    private lateinit var binding: FragmentNotifyBinding
    private lateinit var notifyVM: NotifyVM

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_notify, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        notifyVM = getViewModel()
        binding.apply {
            viewModel = notifyVM
            lifecycleOwner = this@NotifyFragment
        }
        notifyVM.init(binding)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cl_polis.setOnClickListener {
            openInfoCountDownDialog(Constants.polis)
        }

        cl_yardım.setOnClickListener {
            openInfoCountDownDialog(Constants.acilYardım)
        }

        cl_iftaiye.setOnClickListener {
            openInfoCountDownDialog(Constants.itfaiye)
        }

        iv_safe.setOnClickListener {
            openInfoCountDownDialog(Constants.safeSms)
        }

        iv_unsafe.setOnClickListener {
            openInfoCountDownDialog(Constants.unsafeSms)
        }

    }

}