package com.enesky.guvenlikbildir.ui.fragment.latestEarthquakes

import com.enesky.guvenlikbildir.databinding.FragmentLastestEarthquakesBinding
import com.enesky.guvenlikbildir.network.Connection
import com.enesky.guvenlikbildir.network.ResponseHandler
import com.enesky.guvenlikbildir.viewModel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LatestEarthquakesFragmentVM : BaseViewModel() {

    private val _responseHandler = ResponseHandler()
    val responseHandler: ResponseHandler = _responseHandler

    fun init(binding: FragmentLastestEarthquakesBinding) {
        setViewDataBinding(binding)
    }

    init {
        GlobalScope.launch {
            getLastEarthquakes("10")
        }
    }

    private suspend fun getLastEarthquakes(limit: String) {
        try {
            val response = Connection().getLastEarthquakes(limit)

            if (response.isSuccessful) {
                GlobalScope.launch(Dispatchers.Main) {
                    _responseHandler.handleSuccess(response)
                }
            } else {
                _responseHandler.handleFailure(response)
            }
        } catch (e: Exception) {
            _responseHandler.handleException(e)
        }
    }

}