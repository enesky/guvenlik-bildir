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
import com.enesky.guvenlikbildir.adapter.OptionAdapter
import com.enesky.guvenlikbildir.databinding.FragmentOptionsBinding
import com.enesky.guvenlikbildir.extensions.*
import com.enesky.guvenlikbildir.others.Constants
import com.enesky.guvenlikbildir.ui.activity.login.LoginActivity
import com.enesky.guvenlikbildir.ui.dialog.AboutBSDFragment
import com.enesky.guvenlikbildir.ui.dialog.NotificationSettingsBSDFragment
import com.enesky.guvenlikbildir.ui.base.BaseFragment
import com.enesky.guvenlikbildir.ui.fragment.options.contacts.ContactsFragment
import com.enesky.guvenlikbildir.ui.fragment.options.modifySms.ModifySMSFragment
import com.enesky.guvenlikbildir.ui.fragment.options.smsReports.SmsReportHistoryFragment
import com.trendyol.medusalib.navigator.transitionanimation.TransitionAnimationType
import kotlinx.android.synthetic.main.fragment_options.*

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

        App.mAnalytics.setCurrentScreen(activity!!, "fragment", this.javaClass.simpleName)

        optionsVM.whereTo.observe(viewLifecycleOwner, Observer { whereTo ->
            when(whereTo) {
                0 -> multipleStackNavigator!!.start(ContactsFragment(), TransitionAnimationType.BOTTOM_TO_TOP)
                1 -> multipleStackNavigator!!.start(ModifySMSFragment(), TransitionAnimationType.BOTTOM_TO_TOP)
                2 -> multipleStackNavigator!!.start(SmsReportHistoryFragment(), TransitionAnimationType.BOTTOM_TO_TOP)
                3 -> NotificationSettingsBSDFragment().show(parentFragmentManager, "NotificationSettingsBSDFragment")
                //2 -> requireContext().showToast(getString(R.string.item_option_2))
                //3 -> requireContext().showToast(getString(R.string.item_option_3))
                4 -> requireActivity().openGooglePlayPage()
                5 -> requireActivity().shareGooglePlayPage()
                6 -> sendFeedback()
                7 -> requireActivity().openWebView(Constants.githubUrl)
                8 -> AboutBSDFragment().show(parentFragmentManager, "AboutBSDFragment")
                9 -> {
                    App.mAuth.signOut()
                    App.stopWorker()
                    startActivity(Intent(requireActivity(), LoginActivity::class.java))
                    requireActivity().finishAffinity()
                }
            }
        })

        optionsVM.notificationResIdLive.observe(viewLifecycleOwner, Observer {
            optionsVM.optionList.value?.get(3)?.imageId = it
            (rv_options.adapter as OptionAdapter).notifyItemChanged(3)
        })

    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        rv_options.scheduleLayoutAnimation()
    }

}