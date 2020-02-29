package com.enesky.guvenlikbildir.ui.dialog

import android.app.Activity
import android.app.PendingIntent
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.telephony.SmsManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.extensions.*
import com.enesky.guvenlikbildir.model.Contact
import kotlinx.android.synthetic.main.dialog_info_count_down.*

/**
 * Created by Enes Kamil YILMAZ on 02.02.2020
 */

class InfoCountDownDialog : DialogFragment() {

    private lateinit var timer: CountDownTimer
    private var type: String = Constants.polis

    private lateinit var sentBroadcastReceiver: BroadcastReceiver
    private lateinit var deliveredBroadcastReceiver: BroadcastReceiver
    private lateinit var sentPI: PendingIntent
    private lateinit var deliveredPI: PendingIntent
    val SENT = "SMS_SENT"
    val DELIVERED = "SMS_DELIVERED"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, p0: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_info_count_down, container, false)
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        when {
            tag.equals(Constants.polis) -> {
                type = Constants.polis
                tv_dialog_title.text = getString(R.string.label_calling_155)
            }
            tag.equals(Constants.acilYardım) -> {
                type = Constants.acilYardım
                tv_dialog_title.text = getString(R.string.label_calling_112)
            }
            tag.equals(Constants.itfaiye) -> {
                type = Constants.itfaiye
                tv_dialog_title.text = getString(R.string.label_calling_110)
            }
            tag!!.contains(Constants.map) -> {
                type = Constants.map
                tv_dialog_title.text = getString(R.string.label_google_maps)
            }
            tag.equals(Constants.safeSms) -> {
                type = Constants.safeSms
                tv_dialog_title.text = getString(R.string.label_sending_sms)
            }
            tag.equals(Constants.unsafeSms) -> {
                type = Constants.unsafeSms
                tv_dialog_title.text = getString(R.string.label_sending_sms)
            }
            tag!!.contains(Constants.locationMapLink) -> {
                type = Constants.locationMapLink
                tv_dialog_title.text = getString(R.string.label_google_maps)
            }
        }

        tv_okey.setOnClickListener {
            dismiss()
        }

        startCountDown()

        //TODO: Sonuçları değiştir.
        if (tag!!.contains("Sms")) {
            sentPI = PendingIntent.getBroadcast(requireContext(), 0, Intent(SENT), 0)
            deliveredPI = PendingIntent.getBroadcast(requireContext(), 0, Intent(DELIVERED), 0)
            // --When the sms has been sent
            sentBroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    when (resultCode) {
                        Activity.RESULT_OK -> requireContext().showToast("SMS sent success!")
                        SmsManager.RESULT_ERROR_NO_SERVICE -> requireContext().showToast("No active network to send SMS.")
                        SmsManager.RESULT_ERROR_RADIO_OFF -> requireContext().showToast("SMS not sent!")
                        SmsManager.RESULT_ERROR_GENERIC_FAILURE -> requireContext().showToast("SMS not sent!")
                        SmsManager.RESULT_ERROR_NULL_PDU -> requireContext().showToast("SMS not sent!")
                    }
                }
            }
            activity!!.registerReceiver(sentBroadcastReceiver, IntentFilter(SENT))

            // --When SMS has been delivered
            deliveredBroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    when (resultCode) {
                        Activity.RESULT_OK -> requireContext().showToast("SMS delivered.")
                        Activity.RESULT_CANCELED -> requireContext().showToast("SMS not delivered.")
                    }
                }
            }
            activity!!.registerReceiver(deliveredBroadcastReceiver, IntentFilter(DELIVERED))
        }

    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (::sentBroadcastReceiver.isInitialized && ::deliveredBroadcastReceiver.isInitialized) {
            activity!!.unregisterReceiver(sentBroadcastReceiver)
            activity!!.unregisterReceiver(deliveredBroadcastReceiver)
        }
        timer.cancel()
        Log.d("InfoCountDownDialog", "onDismiss(): $type")
    }

    private fun startCountDown() {
        timer = object : CountDownTimer(3200, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tv_countdown.text = (millisUntilFinished / 1000).toString()
            }

            override fun onFinish() {
                when (type) {
                    Constants.polis, Constants.acilYardım, Constants.itfaiye -> {
                        requireContext().requireCallPhonePermission {
                            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$type"))
                            startActivity(intent)
                        }
                    }
                    Constants.map -> {
                        val splitTag = tag!!.split(delimiters = *arrayOf("map"))
                        openGoogleMaps(splitTag[1], splitTag[2])
                    }
                    Constants.locationMapLink -> {
                        openLastKnownLocation()
                    }
                    Constants.safeSms, Constants.unsafeSms -> {
                        sendSMS()
                    }
                }
                dismiss()
            }
        }.start()
    }

    private fun sendSMS() {
        requireContext().requireSendSmsPermission {
            getUsersContactList { sendIt(it) }
        }
    }

    private fun sendIt(list: Any) {
        if (list is MutableList<*>) {
            if ((list as MutableList<Contact>).isNullOrEmpty()) {
                requireContext().showToast("Sms gönderilecek kayıt bulunamadı.")
            } else {
                val text = if (type == Constants.safeSms) safeSms
                                    else unsafeSms
                text.plus("\n" + locationMapLink)
                try {
                    val smsManager = SmsManager.getDefault()
                    for (contact: Contact in list) {
                        smsManager.sendTextMessage(contact.number, null, text, sentPI, deliveredPI)
                        Log.d("Sms Sent to: ", contact.number)
                    }
                } catch (e: Exception) {
                    Log.d("SMSManager Exception", e.message!!)
                    requireContext().showToast("SMS Failed to send, please try again!")
                }
            }
        } else {
            requireContext().showToast(list.toString())
        }
    }

}