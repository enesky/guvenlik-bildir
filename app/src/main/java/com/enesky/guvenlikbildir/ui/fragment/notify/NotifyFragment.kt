package com.enesky.guvenlikbildir.ui.fragment.notify

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
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
import com.enesky.guvenlikbildir.others.Constants
import com.enesky.guvenlikbildir.ui.activity.main.MainVM
import com.enesky.guvenlikbildir.ui.base.BaseFragment
import com.enesky.guvenlikbildir.ui.dialog.SmsReportBSDFragment
import com.enesky.guvenlikbildir.ui.fragment.options.contacts.AddContactsFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.trendyol.medusalib.navigator.transitionanimation.TransitionAnimationType
import kotlinx.android.synthetic.main.fragment_notify.*
import java.util.concurrent.TimeUnit

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
                ).show(parentFragmentManager,"SmsReportBSDFragment")
        }

        iv_unsafe.setTouchAnimation {
            if (selectedContactList.isNullOrEmpty())
                showInfo()
            else
                SmsReportBSDFragment.newInstance(
                    isHistory = false,
                    isSafeSms = false
                ).show(parentFragmentManager,"SmsReportBSDFragment")
        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mainVM.getSelectedContactList().observe(viewLifecycleOwner, Observer { it ->
            selectedContactList = it
        })

    }

    private fun showInfo() {
        val dialog = MaterialAlertDialogBuilder(activity!!)
            .setBackground(ContextCompat.getDrawable(activity!!, R.drawable.bg_radius))
            .setTitle(getString(R.string.label_no_contact_found))
            .setMessage(getString(R.string.label_pls_add_contact))
            .setPositiveButton(getString(R.string.label_add_contact)) { dialog, _ ->
                requireContext().requireReadContactsPermission {
                    multipleStackNavigator!!.start(AddContactsFragment(), TransitionAnimationType.BOTTOM_TO_TOP)
                }
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.label_cancel_with_sec, 3)) {
                    dialog, _ -> dialog.cancel()
            }
            .setCancelable(true)
            .create()

        dialog.setOnShowListener {
            val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            val timer = object: CountDownTimer(3200, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    negativeButton.text = getString(R.string.label_cancel_with_sec,
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished))
                }
                override fun onFinish() {
                    if (dialog.isShowing)
                        dialog.dismiss()
                }
            }
            timer.start()
        }

        dialog.show()
    }

}