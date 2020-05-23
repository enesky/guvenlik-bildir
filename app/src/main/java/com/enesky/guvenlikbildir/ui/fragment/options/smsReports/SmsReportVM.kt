package com.enesky.guvenlikbildir.ui.fragment.options.smsReports

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.enesky.guvenlikbildir.R
import com.enesky.guvenlikbildir.adapter.SmsReportAdapter
import com.enesky.guvenlikbildir.adapter.SmsReportHistoryAdapter
import com.enesky.guvenlikbildir.database.AppDatabase
import com.enesky.guvenlikbildir.database.dao.ContactDao
import com.enesky.guvenlikbildir.database.dao.SmsReportDao
import com.enesky.guvenlikbildir.database.entity.Contact
import com.enesky.guvenlikbildir.database.entity.SmsReport
import com.enesky.guvenlikbildir.database.repo.SmsReportRepository
import com.enesky.guvenlikbildir.databinding.BottomSheetSmsReportBinding
import com.enesky.guvenlikbildir.databinding.FragmentSmsReportHistoryBinding
import com.enesky.guvenlikbildir.viewModel.BaseViewModel
import com.hadilq.liveevent.LiveEvent

/**
 * Created by Enes Kamil YILMAZ on 19.05.2020
 */

class SmsReportVM: BaseViewModel(), SmsReportHistoryAdapter.SmsReportHistoryListener {

    var contactDao: ContactDao = AppDatabase.dbInstance!!.contactDao()
    var smsReportDao: SmsReportDao = AppDatabase.dbInstance!!.smsReportDao()
    var smsReportRepository: SmsReportRepository = SmsReportRepository(smsReportDao)

    private val _smsReportHistoryAdapter = MutableLiveData<SmsReportHistoryAdapter>()
    val smsReportHistoryAdapter: LiveData<SmsReportHistoryAdapter> = _smsReportHistoryAdapter

    val smsReportHistoryList = MutableLiveData<List<SmsReport>>()

    private val _smsReportAdapter = MutableLiveData<SmsReportAdapter>()
    val smsReportAdapter: LiveData<SmsReportAdapter> = _smsReportAdapter

    private val _smsReport = MutableLiveData<SmsReport>()
    val smsReport: LiveData<SmsReport> = _smsReport

    private val _onClick = LiveEvent<SmsReport>()
    val onClick: LiveEvent<SmsReport> = _onClick

    val sizeTextResId = R.string.label_sms_history_count

    fun init(binding: FragmentSmsReportHistoryBinding) {
        setViewDataBinding(binding)
        smsReportRepository.cleanUpSmsReports()
        _smsReport.postValue(null)
        _smsReportHistoryAdapter.postValue(SmsReportHistoryAdapter(listOfNotNull(), this))
    }

    fun init(binding: BottomSheetSmsReportBinding) {
        setViewDataBinding(binding)
        _smsReportAdapter.postValue(SmsReportAdapter())
    }

    fun getSelectedContactList(): LiveData<List<Contact>> = contactDao.getSelectedContactsFlow().asLiveData()

    fun getSmsReportHistory() = smsReportDao.getAllReportsAsFlow().asLiveData()

    fun getSafeSmsList() = smsReportDao.getSafeSmsListAsFlow().asLiveData()

    fun getUnsafeSmsList()= smsReportDao.getUnsafeSmsListAsFlow().asLiveData()

    fun updateSmsReport(smsReport: SmsReport) {
        _smsReport.postValue(smsReport)
    }

    override fun onItemClick(pos: Int, smsReport: SmsReport) {
        onClick.value = smsReport
        _smsReport.value = smsReport
    }

}