package com.enesky.guvenlikbildir.database.repo

import android.annotation.SuppressLint
import com.enesky.guvenlikbildir.database.dao.SmsReportDao
import com.enesky.guvenlikbildir.database.entity.Contact
import com.enesky.guvenlikbildir.database.entity.ContactStatus
import com.enesky.guvenlikbildir.database.entity.SmsReport
import com.enesky.guvenlikbildir.database.entity.SmsReportStatus
import com.enesky.guvenlikbildir.others.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Enes Kamil YILMAZ on 19.05.2020
 */

class SmsReportRepository(private val smsReportDao: SmsReportDao) {

    /**
     * Creates report when user approves sending sms
     */
    @SuppressLint("SimpleDateFormat")
    suspend fun createReport(
        isSafeSms: Boolean,
        contactList: List<Contact>,
        saveIt: Boolean
    ): SmsReport {

        val sentSms =
            if (isSafeSms) safeSms
            else unsafeSms

        val status =
            if (saveIt) SmsReportStatus.IN_QUEUE
            else SmsReportStatus.STAND_BY

        val currentDate: Date = Calendar.getInstance().time
        val sendingDate: String = SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT).format(currentDate)

        val smsReport = SmsReport(
            isSafeSms = isSafeSms,
            sendingDate = sendingDate,
            sentSms = sentSms + "\n" +locationMapWithLink,
            lastKnownLocation = lastKnownLocation!!
        )

        if (contactList.isNotEmpty()) {
            val contactReportList: MutableList<ContactStatus> = mutableListOf()
            contactList.forEach { contact ->
                contactReportList.add(ContactStatus(contact, status))
            }
            smsReport.contactReportList = contactReportList
        }

        if (saveIt)
            smsReportDao.insert(smsReport)

        return smsReport
    }

    /**
     * Update report when one sms status changed
     */
    fun updateReport(
        smsReport: SmsReport? = null,
        contact: Contact? = null,
        contactStatus: ContactStatus? = null,
        newStatus: SmsReportStatus
    ) {
        GlobalScope.launch(Dispatchers.Default) {
            val tempSmsReport: SmsReport = smsReport ?: smsReportDao.getAllReports().last()

            loop@ for ((index: Int, cs: ContactStatus) in tempSmsReport.contactReportList.withIndex()) {
                if (cs == contactStatus || cs.contact == contact) {
                    tempSmsReport.contactReportList[index].smsReportStatus = newStatus
                    smsReportDao.update(tempSmsReport)
                    break@loop
                }
            }
        }
    }

    /**
     * Clean up reports
     * Change in_queue and stand_by status with failure
     */
    @SuppressLint("SimpleDateFormat")
    fun cleanUpSmsReports() {
        GlobalScope.launch(Dispatchers.Default) {
            val smsReports = smsReportDao.getAllReports()
            var changed = false

            if (!smsReports.isNullOrEmpty()) {
                smsReports.forEach { smsReport ->
                    smsReport.contactReportList.forEachIndexed { index, contactStatus ->
                        if (contactStatus.smsReportStatus == SmsReportStatus.IN_QUEUE ||
                            contactStatus.smsReportStatus == SmsReportStatus.STAND_BY) {
                            smsReport.contactReportList[index].smsReportStatus = SmsReportStatus.FAILED
                            changed = true
                        }
                    }
                }
            }

            if (changed)
                smsReportDao.updateAll(smsReports)
        }
    }

}