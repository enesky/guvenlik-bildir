package com.enesky.guvenlikbildir.ui.fragment.options

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.enesky.guvenlikbildir.App
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.ui.activity.login.LoginActivity
import com.enesky.guvenlikbildir.ui.fragment.BaseFragment
import com.enesky.guvenlikbildir.utils.getViewModel
import kotlinx.android.synthetic.main.fragment_options.*

class OptionsFragment : BaseFragment() {

    private lateinit var optionsFragmentVM: OptionsFragmentVM

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        optionsFragmentVM = getViewModel()
        val root = inflater.inflate(R.layout.fragment_options, container, false)

        optionsFragmentVM.text.observe(viewLifecycleOwner, Observer {
            text_notifications.text = it
        })
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btn_sign_out.setOnClickListener {
            App.managerAuth.signOut()
            startActivity(Intent(activity, LoginActivity::class.java))
        }

    }

}