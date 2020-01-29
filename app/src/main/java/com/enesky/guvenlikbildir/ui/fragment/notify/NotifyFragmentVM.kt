package com.enesky.guvenlikbildir.ui.fragment.notify

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NotifyFragmentVM : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Notify Fragment"
    }
    val text: LiveData<String> = _text
}