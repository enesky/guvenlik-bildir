package com.enesky.guvenlikbildir.viewModel

import android.util.Log
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * Created by Enes Kamil YILMAZ on 26.01.2020
 */

open class BaseViewModel : ViewModel(){

    private val _isOnline = MutableLiveData<Boolean>()
    val isOnline: LiveData<Boolean> = _isOnline

    init {
        Log.e(this.javaClass.simpleName, " ${this.javaClass.simpleName} created.")
        _isOnline.value = true
    }

    private var dataViewBinding: ViewDataBinding? = null

    protected fun setViewDataBinding(viewBinding: ViewDataBinding) {
        dataViewBinding = viewBinding
    }

    fun setOnline(isOnline: Boolean) {
        _isOnline.value = isOnline
    }

    override fun onCleared() {
        super.onCleared()
        Log.e(this.javaClass.simpleName, " ${this.javaClass.simpleName} destroyed.")
        dataViewBinding?.unbind()
        dataViewBinding = null
    }
}