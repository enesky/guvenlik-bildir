package com.enesky.guvenlikbildir.ui.fragment.options.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.enesky.guvenlikbildir.databinding.FragmentLoginBinding
import com.enesky.guvenlikbildir.viewModel.BaseViewModel

class LoginVM : BaseViewModel() {

    private val _inputsEnabled = MutableLiveData(true)
    val inputsEnabled: LiveData<Boolean> = _inputsEnabled

    fun init(binding: FragmentLoginBinding) {
        setViewDataBinding(binding)
    }

    fun setInputsEnabled(enable: Boolean) {
        _inputsEnabled.value = enable
    }

}
