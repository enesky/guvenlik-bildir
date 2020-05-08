package com.enesky.guvenlikbildir.viewModel

import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import timber.log.Timber

/**
 * Created by Enes Kamil YILMAZ on 26.01.2020
 */

open class BaseViewModel : ViewModel(){

    init {
        Timber.tag(this.javaClass.simpleName).d(this.javaClass.simpleName, " %s created.")
    }

    private var dataViewBinding: ViewDataBinding? = null

    protected fun setViewDataBinding(viewBinding: ViewDataBinding) {
        dataViewBinding = viewBinding
    }

    override fun onCleared() {
        super.onCleared()
        Timber.tag(this.javaClass.simpleName).d(this.javaClass.simpleName ,"%s destroyed.")
        dataViewBinding?.unbind()
        dataViewBinding = null
    }
}