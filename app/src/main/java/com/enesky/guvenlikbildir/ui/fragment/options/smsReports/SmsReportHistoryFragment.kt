package com.enesky.guvenlikbildir.ui.fragment.options.smsReports

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.enesky.guvenlikbildir.App
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.databinding.FragmentSmsReportHistoryBinding
import com.enesky.guvenlikbildir.extensions.getViewModel
import com.enesky.guvenlikbildir.extensions.makeItGone
import com.enesky.guvenlikbildir.extensions.makeItVisible
import com.enesky.guvenlikbildir.ui.base.BaseFragment
import com.enesky.guvenlikbildir.ui.dialog.SmsReportBSDFragment
import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper
import kotlinx.android.synthetic.main.fragment_sms_report_history.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Enes Kamil YILMAZ on 19.05.2020
 */

class SmsReportHistoryFragment : BaseFragment() {

    private lateinit var binding: FragmentSmsReportHistoryBinding
    private lateinit var smsReportVM : SmsReportVM

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sms_report_history, container, false)
        binding.lifecycleOwner = this
        smsReportVM = getViewModel()
        smsReportVM.init(binding)
        binding.viewModel = smsReportVM
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        App.mAnalytics.setCurrentScreen(activity!!, "fragment", this.javaClass.simpleName)
        pb_loading.makeItVisible()
        GravitySnapHelper(Gravity.TOP).attachToRecyclerView(rv_sms_reports)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        smsReportVM.getSmsReportHistory().observe(viewLifecycleOwner, Observer {
            GlobalScope.launch(Dispatchers.Default) {
                smsReportVM.smsReportHistoryList.postValue(it.reversed())
                smsReportVM.smsReportHistoryAdapter.value!!.update(it.reversed())
                withContext(Dispatchers.Main) {
                    pb_loading.makeItGone()

                    if (it.isNullOrEmpty()) {
                        placeholder.makeItVisible()
                        tv_size.makeItGone()
                    } else {
                        tv_size.makeItVisible()
                        placeholder.makeItGone()
                    }
                }
            }
        })

        smsReportVM.onClick.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                SmsReportBSDFragment.newInstance(
                    isHistory = true
                ).show(childFragmentManager,"SmsReportBSDFragment")
            }
        })

    }

}
