package com.enesky.guvenlikbildir.ui.fragment.latestEarthquakes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.enesky.guvenlikbildir.adapter.EarthquakeAdapter
import com.enesky.guvenlikbildir.databinding.FragmentLastestEarthquakesBinding
import com.enesky.guvenlikbildir.model.EarthquakeOA
import com.enesky.guvenlikbildir.network.Connection
import com.enesky.guvenlikbildir.network.ResponseHandler
import com.enesky.guvenlikbildir.viewModel.BaseViewModel
import com.google.android.gms.maps.model.LatLng
import com.hadilq.liveevent.LiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LatestEarthquakesVM : BaseViewModel(), EarthquakeAdapter.EarthquakeListener {

    private val _earthquakeAdapter = MutableLiveData<EarthquakeAdapter>().apply {
        value = EarthquakeAdapter(mutableListOf(), this@LatestEarthquakesVM)
    }
    val earthquakeAdapter: LiveData<EarthquakeAdapter> = _earthquakeAdapter

    private val _responseHandler = ResponseHandler()
    val responseHandler: ResponseHandler = _responseHandler

    private val _whereTo = LiveEvent<Any>()
    val whereTo: LiveEvent<Any> = _whereTo

    fun init(binding: FragmentLastestEarthquakesBinding) {
        setViewDataBinding(binding)
    }

    suspend fun getLastEarthquakes(limit: String) {
        try {
            val response = Connection().getLastEarthquakes(limit)

            if (response.isSuccessful) {
                GlobalScope.launch(Dispatchers.Main) {
                    _responseHandler.handleSuccess(response)
                    earthquakeAdapter.value!!.update(response.body()!!.result as MutableList<EarthquakeOA>)
                }
            } else {
                _responseHandler.handleFailure(response)
            }
        } catch (e: Exception) {
            _responseHandler.handleException(e)
        }
    }

    override fun onItemClick(pos: Int, earthquakeOA: EarthquakeOA) {
        //TODO: Maybe open detail???
    }

    override fun onMapClick(latlng: LatLng, header: String) {
        _whereTo.value = "${latlng.latitude},${latlng.longitude}map$header"
    }

}