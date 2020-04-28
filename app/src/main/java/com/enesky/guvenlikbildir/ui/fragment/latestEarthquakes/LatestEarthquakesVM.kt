package com.enesky.guvenlikbildir.ui.fragment.latestEarthquakes

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.enesky.guvenlikbildir.adapter.EarthquakeAdapter
import com.enesky.guvenlikbildir.adapter.EarthquakePagingAdapter
import com.enesky.guvenlikbildir.database.entity.Earthquake
import com.enesky.guvenlikbildir.databinding.FragmentLatestEarthquakesBinding
import com.enesky.guvenlikbildir.network.EarthquakeOaAPI
import com.enesky.guvenlikbildir.network.ResponseHandler
import com.enesky.guvenlikbildir.viewModel.BaseViewModel
import com.google.android.gms.maps.model.LatLng
import com.hadilq.liveevent.LiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LatestEarthquakesVM : BaseViewModel(), EarthquakePagingAdapter.EarthquakeItemListener{

    private val _earthquakeAdapter = MutableLiveData<EarthquakeAdapter>()
    val earthquakeAdapter: LiveData<EarthquakeAdapter> = _earthquakeAdapter

    private val _earthquakePagingAdapter = MutableLiveData<EarthquakePagingAdapter>()
    val earthquakePagingAdapter: LiveData<EarthquakePagingAdapter> = _earthquakePagingAdapter

    private val _responseHandler = ResponseHandler()
    val responseHandler: ResponseHandler = _responseHandler

    val whereTo = LiveEvent<Any>()
    val onClick = LiveEvent<Any>()
    val onOptionClick = LiveEvent<Any>()

    fun init(context: Context, binding: FragmentLatestEarthquakesBinding) {
        setViewDataBinding(binding)
        //_earthquakeAdapter.value = EarthquakeAdapter(context, mutableListOf(), this@LatestEarthquakesVM)

        _earthquakePagingAdapter.value = EarthquakePagingAdapter(
            context = context,
            earthquakeItemListener = this@LatestEarthquakesVM
            )
    }

    suspend fun getLastEarthquakes(limit: String) {
        try {
            val response = EarthquakeOaAPI().getLastEarthquakes(limit)

            if (response.isSuccessful) {
                GlobalScope.launch(Dispatchers.Main) {
                    _responseHandler.handleSuccess(response)
                    //earthquakeAdapter.value!!.update(response.body()!!.result as MutableList<EarthquakeOA>)
                }
            } else {
                _responseHandler.handleFailure(response)
            }
        } catch (e: Exception) {
            _responseHandler.handleException(e)
        }
    }

    override fun onItemClick(earthquake: Earthquake) {
        onClick.value = earthquake
    }

    override fun onOptionsClick(earthquake: Earthquake) {
        onOptionClick.value = earthquake
    }

    override fun onMapClick(latlng: LatLng, header: String) {
        whereTo.value = "${latlng.latitude},${latlng.longitude}map$header"
    }

}