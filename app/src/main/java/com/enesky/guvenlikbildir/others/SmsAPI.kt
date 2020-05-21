package com.enesky.guvenlikbildir.others

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.telephony.SmsManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import com.enesky.guvenlikbildir.App
import com.enesky.guvenlikbildir.database.AppDatabase
import com.enesky.guvenlikbildir.database.entity.Contact
import com.enesky.guvenlikbildir.extensions.requireSendSmsPermission
import com.enesky.guvenlikbildir.extensions.showToast
import timber.log.Timber

/**
 * Created by Enes Kamil YILMAZ on 21.05.2020
 */

class SmsAPI(
    private val activity: AppCompatActivity
) {

    companion object {
        lateinit var instance: SmsAPI
            private set
        //setter is private so this way instance cannot assigned from an external class.
    }

    init {
        instance = this
    }

    private lateinit var sentBroadcastReceiver: BroadcastReceiver
    private lateinit var deliveredBroadcastReceiver: BroadcastReceiver
    private lateinit var sentPI: PendingIntent
    private lateinit var deliveredPI: PendingIntent
    val SENT = "SMS_SENT"
    val DELIVERED = "SMS_DELIVERED"
    var contactSize = 0
    var sentCountSuccess = 0
    var sentCountFailed = 0

    fun setReceivers() {
        sentPI = PendingIntent.getBroadcast(activity, 0, Intent(SENT), 0)
        deliveredPI = PendingIntent.getBroadcast(activity, 0, Intent(DELIVERED), 0)
        sentBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        sentCountSuccess++
                        //tv_sent_success_count.text = "Başarılı : $sentCountSuccess/$contactSize"
                    }
                    else -> {
                        sentCountFailed++
                        //tv_sent_fail_count.makeItVisible()
                        //tv_sent_fail_count.text = "Başarısız : $sentCountFailed/$contactSize"
                    }
                }
                if ((sentCountSuccess + sentCountFailed) == contactSize) {
                    val parameters = Bundle().apply {
                        putInt("sentCountSuccess", sentCountSuccess)
                        putInt("sentCountFailed", sentCountFailed)
                        putInt("successRatio", sentCountSuccess / contactSize)
                    }
                    App.mAnalytics.logEvent("InfoCountDown_Sms_Sent", parameters)
                }
            }
        }
        activity.registerReceiver(sentBroadcastReceiver, IntentFilter(SENT))

        deliveredBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (resultCode) {
                    Activity.RESULT_OK -> Timber.tag("InfoCountDownDialog").d("SMS delivered.")
                    Activity.RESULT_CANCELED -> Timber.tag("InfoCountDownDialog")
                        .d("SMS not delivered.")
                }
            }
        }
        activity.registerReceiver(deliveredBroadcastReceiver, IntentFilter(DELIVERED))
    }

    private fun sendSMS(isSafe: Boolean) {
        val selectedContactListLiveData =
            AppDatabase.dbInstance?.contactDao()?.getSelectedContactsFlow()?.asLiveData()

        selectedContactListLiveData?.observe(activity, Observer { it ->
            activity.requireSendSmsPermission {
                sendIt(it, isSafe)
            }
        })
    }

    private fun sendIt(list: List<Contact>?, isSafe: Boolean) {
        if (!list.isNullOrEmpty()) {
            val text = if (isSafe) safeSms
            else unsafeSms
            try {
                val smsManager = SmsManager.getDefault()
                contactSize = list.size
                //tv_sent_success_count.text = "Başarılı: $sentCountSuccess/$contactSize"
                //tv_sent_success_count.makeItVisible()
                //tv_sent_fail_count.text = "Başarısız: $sentCountFailed/$contactSize"
                //tv_sent_success_count.makeItVisible()
                for (contact: Contact in list) {
                    smsManager.sendTextMessage(
                        "", null, text + locationMapWithLink,
                        sentPI, deliveredPI
                    )
                    Timber.tag("Sms Sent to: ").d("%s", contact.number)
                }
            } catch (e: Exception) {
                Timber.tag("SMSManager Exception").d("%s", e.message!!)
                activity.showToast("Sms gönderme işlemi başarısız!")
                //pb_loading.makeItGone()
            }

        } else {
            activity.showToast("Sms gönderilecek kayıt bulunamadı.")
            //dismiss()
        }
    }

    fun onDestroy() {
        if (::sentBroadcastReceiver.isInitialized && ::deliveredBroadcastReceiver.isInitialized) {
            activity.unregisterReceiver(sentBroadcastReceiver)
            activity.unregisterReceiver(deliveredBroadcastReceiver)
        }
    }

}