package com.enesky.guvenlikbildir.viewModel

import android.util.Log
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel

/**
 * Created by Enes Kamil YILMAZ on 26.01.2020
 */

open class BaseViewModel : ViewModel(){

    init {
        Log.e(this.javaClass.simpleName, " ${this.javaClass.simpleName} created.")
    }

    private var dataViewBinding: ViewDataBinding? = null

    protected fun setViewDataBinding(viewBinding: ViewDataBinding) {
        dataViewBinding = viewBinding
    }

    override fun onCleared() {
        super.onCleared()
        Log.e(this.javaClass.simpleName, " ${this.javaClass.simpleName} destroyed.")
        dataViewBinding?.unbind()
        dataViewBinding = null
    }
}