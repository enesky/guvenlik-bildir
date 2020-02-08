package com.enesky.guvenlikbildir.ui.fragment.options.modifySms

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.enesky.guvenlikbildir.databinding.FragmentModifySmsBinding
import com.enesky.guvenlikbildir.extensions.locationMapLink
import com.enesky.guvenlikbildir.extensions.safeSms
import com.enesky.guvenlikbildir.extensions.unsafeSms
import com.enesky.guvenlikbildir.viewModel.BaseViewModel

class ModifySmsVM : BaseViewModel() {

    private val _unsafeSms = MutableLiveData<String>()
    val unsafeSmsLive: LiveData<String> = _unsafeSms

    private val _safeSms = MutableLiveData<String>()
    val safeSmsLive: LiveData<String> = _safeSms

    private val _lastLocation = MutableLiveData<String>()
    val lastLocation: LiveData<String> = _lastLocation

    init {
        _unsafeSms.value = unsafeSms
        _safeSms.value = safeSms
        _lastLocation.value = locationMapLink
    }

    fun init(binding: FragmentModifySmsBinding) {
        setViewDataBinding(binding)
    }

    fun setSafeSms(sms: String) {
        _safeSms.value = sms
    }

    fun setUnsafeSms(sms: String) {
        _unsafeSms.value = sms
    }

}
