package com.enesky.guvenlikbildir.ui.fragment.options.modifySms

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.enesky.guvenlikbildir.R

class ModifySMSFragment: Fragment() {

    private lateinit var modifySmsVM: ModifySmsVM

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_modify_sms, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        modifySmsVM = ViewModelProviders.of(this).get(ModifySmsVM::class.java)
        // TODO: Use the ViewModel
    }

}
