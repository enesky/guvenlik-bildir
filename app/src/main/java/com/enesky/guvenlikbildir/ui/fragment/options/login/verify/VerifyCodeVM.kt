package com.enesky.guvenlikbildir.ui.fragment.options.login.verify

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.enesky.guvenlikbildir.databinding.FragmentVerifyCodeBinding
import com.enesky.guvenlikbildir.viewModel.BaseViewModel

class VerifyCodeVM : BaseViewModel() {

    private val _inputsEnabled = MutableLiveData(true)
    val inputsEnabled: LiveData<Boolean> = _inputsEnabled

    fun init(binding: FragmentVerifyCodeBinding) {
        setViewDataBinding(binding)
    }

    fun setInputsEnabled(enable: Boolean) {
        _inputsEnabled.value = enable
    }

}
