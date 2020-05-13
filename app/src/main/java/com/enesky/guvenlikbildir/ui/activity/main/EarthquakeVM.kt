package com.enesky.guvenlikbildir.ui.activity.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.enesky.guvenlikbildir.database.AppDatabase
import com.enesky.guvenlikbildir.database.repo.EarthquakeRepository
import com.enesky.guvenlikbildir.database.source.EarthquakeSF
import com.enesky.guvenlikbildir.database.entity.Earthquake
import com.enesky.guvenlikbildir.databinding.ActivityMainBinding
import com.enesky.guvenlikbildir.network.EarthquakeAPI
import com.enesky.guvenlikbildir.network.ResponseHandler
import com.enesky.guvenlikbildir.viewModel.BaseViewModel

/**
 * Created by Enes Kamil YILMAZ on 25.04.2020
 */

class EarthquakeVM(
    private val earthquakeRepository: EarthquakeRepository
) : BaseViewModel() {

    private val _responseHandler = ResponseHandler()
    val responseHandler: ResponseHandler = _responseHandler

    var filterText = MutableLiveData<String>().apply { value = "" }
    var minMag = MutableLiveData<Double>().apply { value = 0.0 }
    var maxMag = MutableLiveData<Double>().apply { value = 12.0 }

    var earthquakeFromNotification = MutableLiveData<Earthquake>()

    var earthquakes: LiveData<PagedList<Earthquake>>
    private var earthquakeSF: EarthquakeSF

    init {
        val earthquakeDao = AppDatabase.dbInstance?.earthquakeDao()
        earthquakeSF =
            EarthquakeSF(
                earthquakeDao!!
            )
        earthquakes = LivePagedListBuilder(earthquakeSF, 15).build()
    }

    fun init(binding: ActivityMainBinding) {
        setViewDataBinding(binding)
    }

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

}