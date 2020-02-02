package com.enesky.guvenlikbildir.extensions

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.enesky.guvenlikbildir.viewModel.BaseViewModelFactory

/**
* Created by Enes Kamil YILMAZ on 26.01.2020
*/

inline fun <reified T : ViewModel> Fragment.getViewModel(
    noinline creator: (() -> T)? = null): T {

    val defaultState = Bundle().apply { putInt("key", 0) }
    val factory = SavedStateViewModelFactory(this.activity!!.application, this.activity!!, defaultState)

    return if (creator == null)
        ViewModelProvider(this.activity!!, factory).get(T::class.java)
    else
        ViewModelProvider(this.activity!!, BaseViewModelFactory(creator)).get(T::class.java)
}

inline fun <reified T : ViewModel> FragmentActivity.getViewModel(
    noinline creator: (() -> T)? = null): T {

    val defaultState = Bundle().apply { putInt("key", 0) }
    val factory = SavedStateViewModelFactory(application, this, defaultState)

    return if (creator == null)
        ViewModelProvider(this, factory).get(T::class.java)
    else
        ViewModelProvider(this, BaseViewModelFactory(creator)).get(T::class.java)
}