package com.enesky.guvenlikbildir.ui.activity.main

import android.util.Log
import com.enesky.guvenlikbildir.databinding.ActivityMainBinding
import com.enesky.guvenlikbildir.network.EarthquakeAPI
import com.enesky.guvenlikbildir.network.ResponseHandler
import com.enesky.guvenlikbildir.viewModel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainVM : BaseViewModel() {

    private val _responseHandler = ResponseHandler()
    val responseHandler: ResponseHandler = _responseHandler

    fun init(binding: ActivityMainBinding) {
        setViewDataBinding(binding)
        GlobalScope.launch(Dispatchers.Main) {
            getEarthquakes()
        }
    }

    private suspend fun getEarthquakes() {
        try {
            val response = EarthquakeAPI().getKandilliPost()

            if (response.isSuccessful) {
                _responseHandler.handleSuccess(response)
                EarthquakeAPI.parseResponse(response.body()!!.replace("Ý", "İ"))
                //Log.i("MainVM - Response",response.body()!!)
            } else {
                _responseHandler.handleFailure(response)
                Log.i("MainVM",response.message())
            }
        } catch (e: Exception) {
            _responseHandler.handleException(e)
            Log.i("MainVM","Exception: ${e.localizedMessage}")
        }
    }

}