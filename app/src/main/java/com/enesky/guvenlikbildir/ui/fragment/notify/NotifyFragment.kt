package com.enesky.guvenlikbildir.ui.fragment.notify

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
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
import com.enesky.guvenlikbildir.extensions.*
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
            activity!!.showDialog(
                title = getString(R.string.label_calling_155),
                message = getString(R.string.label_redirecting),
                negativeButtonFunction = {
                    call(Constants.polis)
                },
                isNegativeButtonEnabled = true,
                autoInvoke = true
            )
        }

        cl_yardım.setTouchAnimation {
            activity!!.showDialog(
                title = getString(R.string.label_calling_112),
                message = getString(R.string.label_redirecting),
                negativeButtonFunction = {
                    call(Constants.acil)
                },
                isNegativeButtonEnabled = true,
                autoInvoke = true
            )
        }

        cl_iftaiye.setTouchAnimation {
            activity!!.showDialog(
                title = getString(R.string.label_calling_110),
                message = getString(R.string.label_redirecting),
                negativeButtonFunction = {
                    call(Constants.itfaiye)
                },
                isNegativeButtonEnabled = true,
                autoInvoke = true
            )
        }

        iv_safe.setTouchAnimation {
            if (selectedContactList.isNullOrEmpty())
                showNoContactsFoundDialog()
            else
               SmsReportBSDFragment.newInstance(
                   isHistory = false,
                   isSafeSms = true
               ).show(activity!!.supportFragmentManager,"")
        }

        iv_unsafe.setTouchAnimation {
            if (selectedContactList.isNullOrEmpty())
                showNoContactsFoundDialog()
            else
                SmsReportBSDFragment.newInstance(
                    isHistory = false,
                    isSafeSms = false
                ).show(activity!!.supportFragmentManager,"")
        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mainVM.getSelectedContactList().observe(viewLifecycleOwner, Observer { it ->
            selectedContactList = it
        })
    }

    private fun call(phoneNumber: String) {
        activity!!.requireCallPhonePermission {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber"))
            startActivity(intent)

            val params = Bundle().apply {
                putString("dialed_number", phoneNumber)
            }
            App.mAnalytics.logEvent("calling_event", params)
        }
    }

    private fun showNoContactsFoundDialog() {
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

    private fun showGpsNotAvailableDialog() {
        activity!!.showDialog(
            title = com.enesky.guvenlikbildir.extensions.getString(R.string.label_no_gps_connection_found),
            message = com.enesky.guvenlikbildir.extensions.getString(R.string.label_redirecting_to_gps_settings),
            negativeButtonFunction = {
                activity!!.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            },
            isNegativeButtonEnabled = true,
            autoInvoke = true
        )
    }

}