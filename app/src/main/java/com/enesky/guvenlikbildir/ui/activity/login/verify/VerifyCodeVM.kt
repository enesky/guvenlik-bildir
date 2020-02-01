package com.enesky.guvenlikbildir.ui.activity.login.verify

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.enesky.guvenlikbildir.databinding.ActivityVerifyCodeBinding
import com.enesky.guvenlikbildir.viewModel.BaseViewModel

class VerifyCodeVM : BaseViewModel() {

    private val _inputsEnabled = MutableLiveData<Boolean>().apply {
        value = true
    }
    val inputsEnabled: LiveData<Boolean> = _inputsEnabled

    fun init(binding: ActivityVerifyCodeBinding) {
        setViewDataBinding(binding)
    }

    fun setInputsEnabled(enable: Boolean) {
        _inputsEnabled.value = enable
    }

}
