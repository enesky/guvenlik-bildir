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
import com.enesky.guvenlikbildir.database.AppDatabase
import com.enesky.guvenlikbildir.database.entity.Contact
import com.enesky.guvenlikbildir.database.entity.SmsReportStatus
import com.enesky.guvenlikbildir.database.repo.SmsReportRepository
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
        private const val contactTag = "contact"
        private const val contactBundle = "contactBundle"
    }

    init {
        instance = this
    }

    private lateinit var sentBroadcastReceiver: BroadcastReceiver
    private lateinit var deliveredBroadcastReceiver: BroadcastReceiver
    private lateinit var sentPI: PendingIntent
    private lateinit var deliveredPI: PendingIntent
    private lateinit var smsReportRepository: SmsReportRepository
    private var smsManager: SmsManager? = null
    private var smsApiListener: SmsApiListener? = null
    private var selectedContactList: List<Contact>? = null

    private val SENT = "SMS_SENT"
    private val DELIVERED = "SMS_DELIVERED"
    private var lastContact: Contact? = null

    fun setReceivers() {
        sentBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val statusSuccess = SmsReportStatus.SUCCESS
                        triggerListener(
                            intent = intent,
                            status = statusSuccess
                        )
                        Timber.tag("SmsAPI").d("Sms successfully sent.")
                    }
                    else -> {
                        val statusFailed = SmsReportStatus.FAILED
                        triggerListener(
                            intent = intent,
                            status = statusFailed
                        )
                        Timber.tag("SmsAPI").d("Sms sending failed.")
                    }
                }
            }
        }

        deliveredBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val statusDelivered = SmsReportStatus.DELIVERED
                        triggerListener(
                            intent = intent,
                            status = statusDelivered
                        )
                        Timber.tag("SmsAPI").d("Sms delivered.")
                    }
                    Activity.RESULT_CANCELED -> {
                        Timber.tag("SmsAPI").d("Sms not delivered.")
                    }
                }
            }
        }

        activity.registerReceiver(sentBroadcastReceiver, IntentFilter(SENT))
        activity.registerReceiver(deliveredBroadcastReceiver, IntentFilter(DELIVERED))

        smsReportRepository = SmsReportRepository(AppDatabase.dbInstance!!.smsReportDao())

        AppDatabase.dbInstance?.contactDao()?.getSelectedContactsFlow()?.asLiveData()?.
            observe(activity, Observer { it ->
                selectedContactList = it
        })

    }

    private fun setListener(smsApiListener: SmsApiListener) {
        if (!::sentBroadcastReceiver.isInitialized && !::deliveredBroadcastReceiver.isInitialized)
            setReceivers()
        if (smsManager == null)
            smsManager = SmsManager.getDefault()
        this.smsApiListener = smsApiListener
    }

    fun sendSMS(isSafe: Boolean, smsApiListener: SmsApiListener) {
        if (!selectedContactList.isNullOrEmpty())
            activity.requireSendSmsPermission {
                setListener(smsApiListener)
                sendIt(selectedContactList, isSafe)
            }
    }

    private fun sendIt(list: List<Contact>?, isSafe: Boolean) {
        if (!list.isNullOrEmpty()) {

            val text =
                if (isSafe) safeSms + "\n" + locationMapWithLink
                else unsafeSms + "\n" + locationMapWithLink

            lastContact = list.last()

            try {
                list.forEachIndexed { index, contact ->
                    triggerListener(
                        contact = contact,
                        status = SmsReportStatus.IN_QUEUE
                    )

                    val sentIntent = Intent(SENT)
                    val sentBundle = Bundle()
                    sentBundle.putParcelable(contactBundle, contact)
                    sentIntent.putExtra(contactTag, sentBundle)
                    sentPI = PendingIntent.getBroadcast(
                        activity, index, sentIntent, PendingIntent.FLAG_UPDATE_CURRENT
                    )

                    val deliverIntent = Intent(DELIVERED)
                    val deliverBundle = Bundle()
                    deliverBundle.putParcelable(contactBundle, contact)
                    deliverIntent.putExtra(contactTag, deliverBundle)
                    deliveredPI = PendingIntent.getBroadcast(
                        activity, index, deliverIntent, PendingIntent.FLAG_UPDATE_CURRENT
                    )

                    smsManager?.sendTextMessage(
                        contact.number,
                        null,
                        text,
                        sentPI,
                        deliveredPI
                    )
                    Timber.tag("Sms Sent to: ").d("%s", contact.number)
                }
            } catch (e: Exception) {
                activity.showToast("Sms gönderme işlemi başarısız!")
                Timber.tag("SmsAPI").d("Exception: %s", e.message!!)
            }
        } else {
            activity.showToast("Sms gönderilecek kayıt bulunamadı.")
            Timber.tag("SmsAPI").d("Contact list empty.")
        }
    }

    private fun triggerListener(
        intent: Intent? = null,
        contact: Contact? = null,
        status: SmsReportStatus
    ) {

        val tempContact: Contact? =
            if (intent != null) intent.getBundleExtra(contactTag)?.getParcelable(contactBundle)
            else contact

        smsReportRepository.updateReport(
            contact = tempContact,
            newStatus = status
        )
        Timber.tag("SmsAPI").d("${tempContact?.name} -> $status")

        if (smsApiListener != null) {
            smsApiListener?.onStatusChange(tempContact, status)

            if (lastContact != null &&
                status != SmsReportStatus.IN_QUEUE &&
                status != SmsReportStatus.DELIVERED &&
                lastContact == tempContact)
                processFinished()
        }

    }

    private fun processFinished() {
        smsApiListener?.processFinished()
        Timber.tag("SmsAPI").d("processFinished")
    }

    fun onDestroy() {
        if (::sentBroadcastReceiver.isInitialized && ::deliveredBroadcastReceiver.isInitialized) {
            activity.unregisterReceiver(sentBroadcastReceiver)
            activity.unregisterReceiver(deliveredBroadcastReceiver)
            smsManager = null
            smsApiListener = null
        }
    }

    interface SmsApiListener {
        fun onStatusChange(contact: Contact?, status: SmsReportStatus)
        fun processFinished()
    }


}