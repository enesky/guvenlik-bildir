package com.enesky.guvenlikbildir.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.enesky.guvenlikbildir.App

import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.databinding.BottomSheetAboutBinding
import com.enesky.guvenlikbildir.ui.base.BaseBottomSheetDialogFragment

/**
 * Created by Enes Kamil YILMAZ on 30.04.2020
 */

class AboutBSDFragment : BaseBottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetAboutBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.bottom_sheet_about, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        App.mAnalytics.setCurrentScreen(activity!!, "bottom_sheet", this.javaClass.simpleName)
    }
}
