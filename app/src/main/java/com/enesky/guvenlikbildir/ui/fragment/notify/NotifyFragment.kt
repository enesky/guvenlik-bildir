package com.enesky.guvenlikbildir.ui.fragment.notify

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.databinding.FragmentNotifyBinding
import com.enesky.guvenlikbildir.extensions.Constants
import com.enesky.guvenlikbildir.extensions.getViewModel
import com.enesky.guvenlikbildir.extensions.showToast
import com.enesky.guvenlikbildir.ui.fragment.BaseFragment
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.livinglifetechway.quickpermissions_kotlin.util.QuickPermissionsOptions
import kotlinx.android.synthetic.main.fragment_notify.*

class NotifyFragment : BaseFragment() {

    private lateinit var binding: FragmentNotifyBinding
    private lateinit var notifyVM: NotifyVM
    private var phoneNumber: String = Constants.polis

    // Broadcasting
    private lateinit var sentBroadcastReceiver: BroadcastReceiver
    private lateinit var deliveredBroadcastReceiver: BroadcastReceiver
    private lateinit var sentPI: PendingIntent
    private lateinit var deliveredPI: PendingIntent

    val SENT = "SMS_SENT"
    val DELIVERED = "SMS_DELIVERED"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_notify, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        notifyVM = getViewModel()
        binding.apply {
            viewModel = notifyVM
            lifecycleOwner = this@NotifyFragment
        }
        notifyVM.init(binding)

        // Receiver listener for SMS
         sentPI = PendingIntent.getBroadcast(requireContext(), 0, Intent(SENT), 0)
         deliveredPI = PendingIntent.getBroadcast(requireContext(), 0, Intent(DELIVERED), 0)
         // --When the sms has been sent
         sentBroadcastReceiver = object : BroadcastReceiver() {
             override fun onReceive(context: Context?, intent: Intent?) {
                 when (resultCode) {
                     Activity.RESULT_OK ->
                         requireContext().showToast("SMS sent success!")
                     SmsManager.RESULT_ERROR_NO_SERVICE ->
                         requireContext().showToast("No active network to send SMS.")
                     SmsManager.RESULT_ERROR_RADIO_OFF ->
                         requireContext().showToast("SMS not sent!")
                     SmsManager.RESULT_ERROR_GENERIC_FAILURE ->
                         requireContext().showToast("SMS not sent!")
                     SmsManager.RESULT_ERROR_NULL_PDU ->
                         requireContext().showToast("SMS not sent!")
                 }
             }
         }
         activity!!.registerReceiver(sentBroadcastReceiver, IntentFilter(SENT))    // register receiver
         // --When SMS has been delivered
         deliveredBroadcastReceiver = object : BroadcastReceiver() {
             override fun onReceive(context: Context?, intent: Intent?) {
                 when (resultCode) {
                     Activity.RESULT_OK ->
                         requireContext().showToast("SMS delivered.")
                     Activity.RESULT_CANCELED ->
                         requireContext().showToast("SMS not delivered.")
                 }
             }
         }
         activity!!.registerReceiver(deliveredBroadcastReceiver, IntentFilter(DELIVERED))    // register receiver

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cl_polis.setOnClickListener {
            phoneNumber = Constants.polis
            checkPhoneCallPermission()
        }

        cl_yardım.setOnClickListener {
            phoneNumber = Constants.acilYardım
            checkPhoneCallPermission()
        }

        cl_iftaiye.setOnClickListener {
            phoneNumber = Constants.itfaiye
            checkPhoneCallPermission()
        }

        iv_safe.setOnClickListener {
            openInfoCountDownDialog("1")
        }

        iv_unsafe.setOnClickListener {
            //openInfoCountDownDialog("2")

            //checkSendSMSPermission()

            methodWithPermissions()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        activity!!.unregisterReceiver(sentBroadcastReceiver)
        activity!!.unregisterReceiver(deliveredBroadcastReceiver)
    }

    fun checkPhoneCallPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.CALL_PHONE
                )) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.CALL_PHONE),
                    42)
            }
        } else {
            // Permission has already been granted
            openInfoCountDownDialog(phoneNumber)
        }
    }

    fun checkSendSMSPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.SEND_SMS
                )) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.SEND_SMS),
                    41)
            }
        } else {
            // Permission has already been granted
            try {
                val smsManager = SmsManager.getDefault()
                for (number: String in listOf("+905383115141")) {
                    smsManager.sendTextMessage(number, null, "Deneme", sentPI, deliveredPI)
                }
            } catch (e: Exception) {
                Log.d("SMSManager Exception", e.message)
                e.printStackTrace()
                requireContext().showToast("SMS Failed to send, please try again!")
            }
        }
    }

    fun methodWithPermissions() = runWithPermissions(Manifest.permission.SEND_SMS) {
        requireContext().showToast("Camera and audio recording permissions granted")
        try {
            val smsManager = SmsManager.getDefault()
            for (number: String in listOf("+905383115141")) {
                smsManager.sendTextMessage(number, null, "Deneme", sentPI, deliveredPI)
            }
        } catch (e: Exception) {
            Log.d("SMSManager Exception", e.message)
            e.printStackTrace()
            requireContext().showToast("SMS Failed to send, please try again!")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == 42) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED))
                openInfoCountDownDialog(phoneNumber)
            else
                // permission denied, boo! Disable the functionality
            return
        }
    }

}