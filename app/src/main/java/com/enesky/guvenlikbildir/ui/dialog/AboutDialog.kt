package com.enesky.guvenlikbildir.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.enesky.guvenlikbildir.App

import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.databinding.DialogAboutAppBinding

/**
 * Created by Enes Kamil YILMAZ on 30.04.2020
 */

class AboutDialog : BaseBottomSheetDialogFragment() {

    private lateinit var binding: DialogAboutAppBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_about_app, container, false)
        App.mAnalytics.setCurrentScreen(activity!!, "dialog", this.javaClass.simpleName)
        return binding.root
    }
}
