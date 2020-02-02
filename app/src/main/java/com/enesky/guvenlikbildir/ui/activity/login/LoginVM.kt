package com.enesky.guvenlikbildir.ui.activity.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.enesky.guvenlikbildir.databinding.ActivityLoginBinding
import com.enesky.guvenlikbildir.viewModel.BaseViewModel

class LoginVM : BaseViewModel() {

    private val _inputsEnabled = MutableLiveData<Boolean>().apply {
        value = true
    }
    val inputsEnabled: LiveData<Boolean> = _inputsEnabled

    fun init(binding: ActivityLoginBinding) {
        setViewDataBinding(binding)
    }

    fun setInputsEnabled(enable: Boolean) {
        _inputsEnabled.value = enable
    }

}
