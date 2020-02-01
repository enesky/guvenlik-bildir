package com.enesky.guvenlikbildir.ui.fragment.notify

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.enesky.guvenlikbildir.databinding.FragmentNotifyBinding
import com.enesky.guvenlikbildir.viewModel.BaseViewModel

class NotifyFragmentVM: BaseViewModel() {

    fun init(binding: FragmentNotifyBinding) {
        setViewDataBinding(binding)
    }
}