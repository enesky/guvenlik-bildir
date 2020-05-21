package com.enesky.guvenlikbildir.database.repo

import android.annotation.SuppressLint
import com.enesky.guvenlikbildir.database.dao.SmsReportDao
import com.enesky.guvenlikbildir.database.entity.Contact
import com.enesky.guvenlikbildir.database.entity.ContactStatus
import com.enesky.guvenlikbildir.database.entity.SmsReport
import com.enesky.guvenlikbildir.database.entity.SmsReportStatus
import com.enesky.guvenlikbildir.others.Constants
import com.enesky.guvenlikbildir.others.lastKnownLocation
import com.enesky.guvenlikbildir.others.safeSms
import com.enesky.guvenlikbildir.others.unsafeSms
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
        contactList: List<Contact>
    ): SmsReport {

        val sentSms = if (isSafeSms) safeSms
        else unsafeSms

        val contactReportList: MutableList<ContactStatus> = mutableListOf()
        contactList.forEach { contact ->
            contactReportList.add(ContactStatus(contact, SmsReportStatus.IN_QUEUE))
        }

        val currentDate: Date = Calendar.getInstance().time
        val sendingDate: String = SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT).format(currentDate)

        val smsReport = SmsReport(
            isSafeSms = isSafeSms,
            sendingDate = sendingDate,
            sentSms = sentSms + lastKnownLocation,
            lastKnownLocation = lastKnownLocation!!,
            contactReportList = contactReportList
        )

        smsReportDao.insert(smsReport)
        return smsReport
    }

    /**
     * Update report when one sms status changed
     */
    fun updateReport(
        smsReport: SmsReport,
        contactStatus: ContactStatus,
        newStatus: SmsReportStatus
    ) {
        GlobalScope.launch(Dispatchers.Default) {
            val contactStatusIndex = smsReport.contactReportList.indexOf(contactStatus)
            smsReport.contactReportList[contactStatusIndex].smsReportStatus = newStatus
            smsReportDao.update(smsReport)
        }
    }

    /**
     * Clean up reports
     */
    fun cleanUpSmsReports() {
        GlobalScope.launch(Dispatchers.Default) {
            val smsReports = smsReportDao.getAllReports()

            if (!smsReports.isNullOrEmpty()) {
                smsReports.forEach { smsReport ->
                    smsReport.contactReportList.forEachIndexed { index, contactStatus ->
                        if (contactStatus.smsReportStatus == SmsReportStatus.IN_QUEUE) {
                            smsReport.contactReportList[index].smsReportStatus = SmsReportStatus.FAILED
                        }
                    }
                }
                smsReportDao.updateAll(smsReports)
            }

        }
    }

}