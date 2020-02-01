package com.enesky.guvenlikbildir.ui.activity.main

import com.enesky.guvenlikbildir.databinding.ActivityMainBinding
import com.enesky.guvenlikbildir.viewModel.BaseViewModel

class MainActivityVM : BaseViewModel(){

    fun init(binding: ActivityMainBinding) {
        setViewDataBinding(binding)
    }

}