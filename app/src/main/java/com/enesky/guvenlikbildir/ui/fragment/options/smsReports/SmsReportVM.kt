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
import com.enesky.guvenlikbildir.others.lastKnownLocation
import com.enesky.guvenlikbildir.others.locationMapWithLink
import com.enesky.guvenlikbildir.others.safeSms
import com.enesky.guvenlikbildir.others.unsafeSms
import com.enesky.guvenlikbildir.viewModel.BaseViewModel
import com.hadilq.liveevent.LiveEvent
import timber.log.Timber

/**
 * Created by Enes Kamil YILMAZ on 19.05.2020
 */

class SmsReportVM: BaseViewModel(), SmsReportHistoryAdapter.SmsReportHistoryListener {

    var contactDao: ContactDao = AppDatabase.dbInstance!!.contactDao()
    var smsReportDao: SmsReportDao = AppDatabase.dbInstance!!.smsReportDao()
    var smsReportRepository: SmsReportRepository

    private val _smsReportHistoryAdapter = MutableLiveData<SmsReportHistoryAdapter>()
    val smsReportHistoryAdapter: LiveData<SmsReportHistoryAdapter> = _smsReportHistoryAdapter

    private val _smsReportHistoryList = MutableLiveData<List<SmsReport>>()
    val smsReportHistoryList: LiveData<List<SmsReport>> = _smsReportHistoryList

    private val _smsReportAdapter = MutableLiveData<SmsReportAdapter>()
    val smsReportAdapter: LiveData<SmsReportAdapter> = _smsReportAdapter

    private val _smsReport = MutableLiveData<SmsReport>()
    val smsReport: LiveData<SmsReport> = _smsReport

    private val _onClick = LiveEvent<SmsReport>()
    val onClick: LiveEvent<SmsReport> = _onClick

    val locationLive = MutableLiveData<String>()
    val smsLive = MutableLiveData<String>()

    val sizeTextResId = R.string.label_sms_history_count

    init {
        smsReportRepository = SmsReportRepository(smsReportDao)
        _smsReportHistoryAdapter.postValue(SmsReportHistoryAdapter(listOf(), this))
        _smsReportAdapter.postValue(SmsReportAdapter(null))
    }

    fun init(binding: FragmentSmsReportHistoryBinding) {
        setViewDataBinding(binding)
        _smsReport.postValue(null)
    }

    fun init(binding: BottomSheetSmsReportBinding) {
        setViewDataBinding(binding)
    }

    fun getSelectedContactList(): LiveData<List<Contact>> = contactDao.getSelectedContactsFlow().asLiveData()

    fun getSmsReportHistory() = smsReportDao.getAllReportsAsFlow().asLiveData()

    fun getSafeSmsList() = smsReportDao.getSafeSmsListAsFlow().asLiveData()

    fun getUnsafeSmsList()= smsReportDao.getUnsafeSmsListAsFlow().asLiveData()

    override fun onItemClick(pos: Int, smsReport: SmsReport) {
        onClick.value = smsReport
        _smsReport.value = smsReport
    }

}