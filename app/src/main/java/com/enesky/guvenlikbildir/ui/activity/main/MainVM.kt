package com.enesky.guvenlikbildir.ui.activity.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.enesky.guvenlikbildir.adapter.AddContactAdapter
import com.enesky.guvenlikbildir.adapter.ContactAdapter
import com.enesky.guvenlikbildir.database.AppDatabase
import com.enesky.guvenlikbildir.database.dao.ContactDao
import com.enesky.guvenlikbildir.database.dao.EarthquakeDao
import com.enesky.guvenlikbildir.database.entity.Contact
import com.enesky.guvenlikbildir.database.repo.EarthquakeRepository
import com.enesky.guvenlikbildir.database.source.EarthquakeSF
import com.enesky.guvenlikbildir.database.entity.Earthquake
import com.enesky.guvenlikbildir.database.repo.ContactRepository
import com.enesky.guvenlikbildir.databinding.ActivityMainBinding
import com.enesky.guvenlikbildir.databinding.FragmentAddContactsBinding
import com.enesky.guvenlikbildir.databinding.FragmentContactsBinding
import com.enesky.guvenlikbildir.databinding.FragmentNotifyBinding
import com.enesky.guvenlikbildir.network.EarthquakeAPI
import com.enesky.guvenlikbildir.network.ResponseHandler
import com.enesky.guvenlikbildir.viewModel.BaseViewModel
import com.hadilq.liveevent.LiveEvent

/**
 * Created by Enes Kamil YILMAZ on 25.04.2020
 */

class MainVM (appDatabase: AppDatabase) : BaseViewModel(),
    ContactAdapter.ContactListener,
    AddContactAdapter.AddContactListener {

    private val _responseHandler = ResponseHandler()
    val responseHandler: ResponseHandler = _responseHandler

    var filterText = MutableLiveData<String>().apply { value = "" }
    var minMag = MutableLiveData<Double>().apply { value = 0.0 }
    var maxMag = MutableLiveData<Double>().apply { value = 12.0 }

    val isViewsLoaded = MutableLiveData<Boolean>()
    val onClick = LiveEvent<Any>()

    var earthquakes: LiveData<PagedList<Earthquake>>

    var contactDao: ContactDao
    var contactRepository: ContactRepository
    var earthquakeDao: EarthquakeDao
    var earthquakeRepository: EarthquakeRepository
    var earthquakeSF: EarthquakeSF

    init {
        contactDao = appDatabase.contactDao()
        contactRepository = ContactRepository(contactDao)
        earthquakeDao = appDatabase.earthquakeDao()
        earthquakeRepository = EarthquakeRepository(earthquakeDao)

        earthquakeSF = EarthquakeSF(earthquakeDao)
        earthquakes = LivePagedListBuilder(earthquakeSF, 15).build()
    }

    fun init(binding: ActivityMainBinding) {
        setViewDataBinding(binding)
    }

    fun init(binding: FragmentContactsBinding) {
        setViewDataBinding(binding)
    }

    fun init(binding: FragmentAddContactsBinding) {
        setViewDataBinding(binding)
        isViewsLoaded.value = false
    }

    fun init(binding: FragmentNotifyBinding) {
        setViewDataBinding(binding)
    }

    fun getSelectedContactList(): LiveData<List<Contact>> = contactDao.getSelectedContactsFlow().asLiveData()

    fun getUnselectedContactList(): LiveData<List<Contact>> = contactDao.getUnselectedContactsFlow().asLiveData()

    fun getEarthquakeList(query: String, minMag: Double, maxMag: Double) {
        filterText.value = query
        this.minMag.value = minMag
        this.maxMag.value = maxMag

        earthquakeRepository.getEarthquakes(earthquakeSF, query, minMag, maxMag)
        earthquakes.value?.dataSource?.invalidate()
    }

    suspend fun getEarthquakes() {
        try {
            val response = EarthquakeAPI().getKandilliPost()
            if (response.isSuccessful) {
                _responseHandler.handleSuccess("Success")
                earthquakeRepository.refreshEartquakes(EarthquakeAPI.parseResponse(response.body()!!.replace("�", "I")))
            } else {
                _responseHandler.handleFailure(response)
            }
        } catch (e: Exception) {
            _responseHandler.handleFailure(e)
        }
    }

    override fun onDeleteClick(pos: Int, contact: Contact) {
        onClick.value = contact
    }

    override fun onItemClick(pos: Int) {
        onClick.value = pos
    }

    override fun onItemClick(pos: Int, contact: Contact) {
        onClick.value = pos to contact
    }

}