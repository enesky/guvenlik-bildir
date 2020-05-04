package com.enesky.guvenlikbildir.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.Config
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.enesky.guvenlikbildir.database.entity.Earthquake
import com.enesky.guvenlikbildir.network.EarthquakeAPI
import com.enesky.guvenlikbildir.network.ResponseHandler
import com.enesky.guvenlikbildir.others.Constants
import com.enesky.guvenlikbildir.viewModel.BaseViewModel

/**
 * Created by Enes Kamil YILMAZ on 25.04.2020
 */

class EarthquakeVM(private val earthquakeRepository: EarthquakeRepository) : BaseViewModel() {

    private val _responseHandler = ResponseHandler()
    val responseHandler: ResponseHandler = _responseHandler

    var filterText = MutableLiveData<String>().apply { value = "" }
    var minMag = MutableLiveData<Double>().apply { value = 0.0 }
    var maxMag = MutableLiveData<Double>().apply { value = 12.0 }

    var earthquakes: LiveData<PagedList<Earthquake>>

    init {
        val earthquakeDao = EarthquakeDB.DB_INSTANCE?.earthquakeDao()
        earthquakes = earthquakeDao!!.getEarthquakesHappenedAtGivenLocAndBiggerThanGivenMagDsF(
            query = filterText.value!!,
            minMag = minMag.value!!,
            maxMag = maxMag.value!!).toLiveData(
            Config( pageSize =  15,
                initialLoadSizeHint = 15,
                prefetchDistance = 2,
                enablePlaceholders = true,
                maxSize = Constants.EARTHQUAKE_LIST_SIZE
            ))
    }

    fun getEarthquakeList(query: String, minMag: Double, maxMag: Double) {
        this.filterText.value = query
        this.minMag.value = minMag
        this.maxMag.value = maxMag

        //TODO: boş liste dönerse placeholder ekle

        earthquakeRepository.getEarthquakes(query, minMag, maxMag)
        earthquakes.value?.dataSource?.invalidate()
    }

    suspend fun getEarthquakes() {
        try {
            val response = EarthquakeAPI().getKandilliPost()
            if (response.isSuccessful) {
                _responseHandler.handleSuccess("Success")
                earthquakeRepository.initAllEarthquakes(EarthquakeAPI.parseResponse(response.body()!!.replace("�", "I")))
            } else {
                _responseHandler.handleFailure(response)
            }
        } catch (e: Exception) {
            _responseHandler.handleException(e)
        }
    }

}