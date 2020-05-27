package com.enesky.guvenlikbildir.ui.fragment.notify

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.enesky.guvenlikbildir.App
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.database.AppDatabase
import com.enesky.guvenlikbildir.database.entity.Contact
import com.enesky.guvenlikbildir.databinding.FragmentNotifyBinding
import com.enesky.guvenlikbildir.extensions.getViewModel
import com.enesky.guvenlikbildir.extensions.requireReadContactsPermission
import com.enesky.guvenlikbildir.extensions.setTouchAnimation
import com.enesky.guvenlikbildir.extensions.showDialog
import com.enesky.guvenlikbildir.others.Constants
import com.enesky.guvenlikbildir.ui.activity.main.MainVM
import com.enesky.guvenlikbildir.ui.base.BaseFragment
import com.enesky.guvenlikbildir.ui.dialog.SmsReportBSDFragment
import com.enesky.guvenlikbildir.ui.fragment.options.contacts.AddContactsFragment
import com.trendyol.medusalib.navigator.transitionanimation.TransitionAnimationType
import kotlinx.android.synthetic.main.fragment_notify.*

class NotifyFragment : BaseFragment() {

    private lateinit var binding: FragmentNotifyBinding
    private val mainVM by lazy {
        getViewModel {
            MainVM(AppDatabase.dbInstance!!)
        }
    }

    private var selectedContactList : List<Contact> = listOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_notify, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainVM.init(binding)
        App.mAnalytics.setCurrentScreen(activity!!, "fragment", this.javaClass.simpleName)

        cl_polis.setTouchAnimation {
            openInfoCountDownDialog(Constants.polis)
        }

        cl_yardım.setTouchAnimation {
            openInfoCountDownDialog(Constants.acilYardım)
        }

        cl_iftaiye.setTouchAnimation {
            openInfoCountDownDialog(Constants.itfaiye)
        }

        iv_safe.setTouchAnimation {
            if (selectedContactList.isNullOrEmpty())
                showInfo()
            else
                SmsReportBSDFragment.newInstance(
                    isHistory = false,
                    isSafeSms = true
                ).show(activity!!.supportFragmentManager,"SmsReportBSDFragment")
        }

        iv_unsafe.setTouchAnimation {
            if (selectedContactList.isNullOrEmpty())
                showInfo()
            else
                SmsReportBSDFragment.newInstance(
                    isHistory = false,
                    isSafeSms = false
                ).show(activity!!.supportFragmentManager,"SmsReportBSDFragment")
        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mainVM.getSelectedContactList().observe(viewLifecycleOwner, Observer { it ->
            selectedContactList = it
        })

    }

    private fun showInfo() {
        activity!!.showDialog(
            title = getString(R.string.label_no_contact_found),
            message = getString(R.string.label_pls_add_contact),
            positiveButtonText = getString(R.string.label_add_contact),
            positiveButtonFunction = {
                activity!!.requireReadContactsPermission {
                    multipleStackNavigator!!.start(AddContactsFragment(), TransitionAnimationType.BOTTOM_TO_TOP)
                }
            }
        )
    }

}