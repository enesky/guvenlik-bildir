package com.enesky.guvenlikbildir.ui.fragment.options

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.enesky.guvenlikbildir.App
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.databinding.FragmentOptionsBinding
import com.enesky.guvenlikbildir.extensions.getViewModel
import com.enesky.guvenlikbildir.ui.activity.login.LoginActivity
import com.enesky.guvenlikbildir.ui.fragment.BaseFragment
import com.enesky.guvenlikbildir.ui.fragment.options.modifySms.ModifySMSFragment
import com.trendyol.medusalib.navigator.transitionanimation.TransitionAnimationType

class OptionsFragment: BaseFragment() {

    private lateinit var binding: FragmentOptionsBinding
    private lateinit var optionsVM: OptionsVM

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_options, container,false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        optionsVM = getViewModel()
        binding.apply {
            viewModel = optionsVM
            lifecycleOwner = this@OptionsFragment
        }
        optionsVM.init(binding)

        optionsVM.whereTo.observe(viewLifecycleOwner, Observer { whereTo ->
            when(whereTo) {
                in 0..4 -> {
                    multipleStackNavigator!!.start(ModifySMSFragment(), TransitionAnimationType.BOTTOM_TO_TOP)
                }
                5 -> {
                    App.managerAuth.signOut()
                    startActivity(Intent(activity, LoginActivity::class.java))
                    activity!!.finishAffinity()
                }
            }
        })

    }

}