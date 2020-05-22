package com.enesky.guvenlikbildir.others

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.telephony.SmsManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import com.enesky.guvenlikbildir.database.AppDatabase
import com.enesky.guvenlikbildir.database.entity.Contact
import com.enesky.guvenlikbildir.database.entity.SmsReportStatus
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
    }

    init {
        instance = this
    }

    private lateinit var sentBroadcastReceiver: BroadcastReceiver
    private lateinit var deliveredBroadcastReceiver: BroadcastReceiver
    private lateinit var sentPI: PendingIntent
    private lateinit var deliveredPI: PendingIntent
    private val SENT = "SMS_SENT"
    private val DELIVERED = "SMS_DELIVERED"

    fun setReceivers() {
        sentBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (resultCode) {
                    Activity.RESULT_OK -> {

                        val statusSuccess = SmsReportStatus.SUCCESS
                    }
                    else -> {

                        val statusFailed = SmsReportStatus.FAILED
                    }
                }
            }
        }

        deliveredBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (resultCode) {
                    Activity.RESULT_OK -> {

                        val statusDelivered = SmsReportStatus.DELIVERED
                        Timber.tag("InfoCountDownDialog").d("SMS delivered.")
                    }
                    Activity.RESULT_CANCELED -> {
                        Timber.tag("InfoCountDownDialog").d("SMS not delivered.")
                    }
                }
            }
        }

        sentPI = PendingIntent.getBroadcast(activity, 0, Intent(SENT), 0)
        deliveredPI = PendingIntent.getBroadcast(activity, 0, Intent(DELIVERED), 0)
        activity.registerReceiver(sentBroadcastReceiver, IntentFilter(SENT))
        activity.registerReceiver(deliveredBroadcastReceiver, IntentFilter(DELIVERED))
    }

    private fun sendIt(list: List<Contact>?, isSafe: Boolean) {
        if (!list.isNullOrEmpty()) {
            val text =
                if (isSafe) safeSms
                else unsafeSms

            try {
                val smsManager = SmsManager.getDefault()
                for (contact: Contact in list) {
                    smsManager.sendTextMessage(
                        "05383115141", null, text + locationMapWithLink,
                        sentPI, deliveredPI
                    )
                    Timber.tag("Sms Sent to: ").d("%s", contact.number)
                }
            } catch (e: Exception) {
                Timber.tag("SMSManager Exception").d("%s", e.message!!)
                activity.showToast("Sms gönderme işlemi başarısız!")
            }

        } else {
            activity.showToast("Sms gönderilecek kayıt bulunamadı.")
        }
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

    fun onDestroy() {
        if (::sentBroadcastReceiver.isInitialized && ::deliveredBroadcastReceiver.isInitialized) {
            activity.unregisterReceiver(sentBroadcastReceiver)
            activity.unregisterReceiver(deliveredBroadcastReceiver)
        }
    }

}