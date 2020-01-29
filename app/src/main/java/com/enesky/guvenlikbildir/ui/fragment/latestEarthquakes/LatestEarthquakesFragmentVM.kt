package com.enesky.guvenlikbildir.ui.fragment.latestEarthquakes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LatestEarthquakesFragmentVM : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is LatestEarthquakes Fragment"
    }
    val text: LiveData<String> = _text
}