package com.enesky.guvenlikbildir.ui.dialog

import android.app.Activity
import android.app.PendingIntent
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.telephony.SmsManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import com.enesky.guvenlikbildir.App
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.database.AppDatabase
import com.enesky.guvenlikbildir.database.entity.Contact
import com.enesky.guvenlikbildir.extensions.*
import com.enesky.guvenlikbildir.others.Constants
import kotlinx.android.synthetic.main.dialog_info_count_down.*
import timber.log.Timber

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
    var contactSize = 0
    var sentCountSuccess = 0
    var sentCountFailed = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, p0: Bundle?): View? {
        App.mAnalytics.setCurrentScreen(activity!!, "dialog", this.javaClass.simpleName)
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
            tag.equals(Constants.gpsSetting) -> {
            type = Constants.gpsSetting
            tv_dialog_title.text = getString(R.string.label_no_gps_connection_found)
            }
            tag!!.contains(Constants.locationMapLink) -> {
                type = Constants.locationMapLink
                tv_dialog_title.text = getString(R.string.label_google_maps)
            }
        }

        val params = Bundle().apply {
            putString("action_type", type)
        }
        App.mAnalytics.logEvent("InfoCountDownDialog", params)

        tv_dismiss.setOnClickListener {
            dismiss()
        }

        startCountDown()

        if (tag!!.contains("Sms")) {
            sentPI = PendingIntent.getBroadcast(requireContext(), 0, Intent(SENT), 0)
            deliveredPI = PendingIntent.getBroadcast(requireContext(), 0, Intent(DELIVERED), 0)
            sentBroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    when (resultCode) {
                        Activity.RESULT_OK -> {
                            sentCountSuccess++
                            tv_sent_success_count.text = "Başarılı : $sentCountSuccess/$contactSize"
                        }
                        else -> {
                            sentCountFailed++
                            tv_sent_fail_count.makeItVisible()
                            tv_sent_fail_count.text = "Başarısız : $sentCountFailed/$contactSize"
                        }
                    }
                    if ((sentCountSuccess + sentCountFailed) == contactSize) {
                        timer = object : CountDownTimer(2000, 1000) {
                            override fun onTick(millisUntilFinished: Long) {}
                            override fun onFinish() {

                                val params = Bundle().apply {
                                    putInt("sentCountSuccess", sentCountSuccess)
                                    putInt("sentCountFailed", sentCountFailed)
                                    putInt("successRatio", sentCountSuccess / contactSize)
                                }
                                App.mAnalytics.logEvent("InfoCountDown_Sms_Sent", params)

                                dismiss()
                            }
                        }.start()
                    }
                }
            }
            activity!!.registerReceiver(sentBroadcastReceiver, IntentFilter(SENT))

            deliveredBroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    when (resultCode) {
                        Activity.RESULT_OK -> Timber.tag("InfoCountDownDialog").d("SMS delivered.")
                        Activity.RESULT_CANCELED -> Timber.tag("InfoCountDownDialog")
                            .d("SMS not delivered.")
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
        Timber.tag("InfoCountDownDialog").d("onDismiss(): %s", type)
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

                            val params = Bundle().apply {
                                putString("Dialed_number", type)
                            }
                            App.mAnalytics.logEvent("InfoCountDown_Calling", params)
                        }

                        dismiss()
                    }
                    Constants.map -> {
                        val splitTag = tag!!.split(delimiters = *arrayOf("map"))
                        openGoogleMaps(splitTag[1], splitTag[2])
                        dismiss()
                    }
                    Constants.locationMapLink -> {
                        openLastKnownLocation()
                        dismiss()
                    }
                    Constants.safeSms, Constants.unsafeSms -> {
                        sendSMS()
                        tv_countdown.makeItGone()
                        val params = Bundle().apply {
                            putString("Sms type", type)
                        }
                        App.mAnalytics.logEvent("InfoCountDown_Send_Sms", params)
                    }
                    Constants.gpsSetting -> {
                        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                        dismiss()
                    }
                }

            }
        }.start()
    }

    private fun sendSMS() {
        val selectedContactListLiveData =
            AppDatabase.getDatabaseManager(context!!.applicationContext).contactDao().getSelectedContactsFlow().asLiveData()

        selectedContactListLiveData.observe(viewLifecycleOwner, Observer { it ->
            requireContext().requireSendSmsPermission {
                sendIt(it)
            }
        })
    }

    private fun sendIt(list: List<Contact>?) {
        if (!list.isNullOrEmpty()) {
            val text = if (type == Constants.safeSms) safeSms
                                else unsafeSms
            try {
                val smsManager = SmsManager.getDefault()
                contactSize = list.size
                tv_sent_success_count.text = "Başarılı: $sentCountSuccess/$contactSize"
                tv_sent_success_count.makeItVisible()
                tv_sent_fail_count.text = "Başarısız: $sentCountFailed/$contactSize"
                tv_sent_success_count.makeItVisible()
                for (contact: Contact in list) {
                    smsManager.sendTextMessage(
                        contact.number, null, text + locationMapWithLink,
                        sentPI, deliveredPI
                    )
                    Timber.tag("Sms Sent to: ").d("%s", contact.number)
                }
            } catch (e: Exception) {
                Timber.tag("SMSManager Exception").d("%s", e.message!!)
                requireContext().showToast("Sms gönderme işlemi başarısız!")
                pb_loading.makeItGone()
            }

        } else {
            requireContext().showToast("Sms gönderilecek kayıt bulunamadı.")
            dismiss()
        }
    }

}