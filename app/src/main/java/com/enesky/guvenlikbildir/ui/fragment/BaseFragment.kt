package com.enesky.guvenlikbildir.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.enesky.guvenlikbildir.ui.dialog.InfoCountDownDialog

abstract class BaseFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    fun callPhone(tag: String) {
        InfoCountDownDialog().show(childFragmentManager, tag)
    }

}